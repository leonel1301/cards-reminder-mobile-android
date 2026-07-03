package com.lenaralabs.cardsreminder.core.notifications

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.lenaralabs.cardsreminder.core.analytics.AnalyticsTracker
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.core.data.DevicesRepository
import com.lenaralabs.cardsreminder.core.data.NotificationPreferences
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class NotificationAuthorizationStatus {
    Granted,
    Denied,
}

data class PushNotificationState(
    val authorizationStatus: NotificationAuthorizationStatus = NotificationAuthorizationStatus.Denied,
    val isPreferenceEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val registrationError: String? = null,
    val fcmToken: String? = null,
) {
    val isAuthorized: Boolean
        get() = authorizationStatus == NotificationAuthorizationStatus.Granted
}

class PushNotificationManager(
    private val context: Context,
    private val authRepository: AuthRepository,
    private val devicesRepository: DevicesRepository,
    private val notificationPreferences: NotificationPreferences,
    private val analyticsTracker: AnalyticsTracker,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow(PushNotificationState())
    val state: StateFlow<PushNotificationState> = _state.asStateFlow()

    private var fcmToken: String? = null

    fun initialize() {
        scope.launch {
            refreshAuthorizationStatus()
            refreshPreference()
        }
        scope.launch {
            runCatching {
                updateFcmToken(FirebaseMessaging.getInstance().token.await())
            }
        }
    }

    fun refreshAuthorizationStatus() {
        val granted = areSystemNotificationsEnabled()
        _state.update {
            it.copy(
                authorizationStatus = if (granted) {
                    NotificationAuthorizationStatus.Granted
                } else {
                    NotificationAuthorizationStatus.Denied
                },
            )
        }
    }

    suspend fun refreshAndCheckFullyEnabled(): Boolean {
        refreshAuthorizationStatus()
        refreshPreference()
        val current = _state.value
        return current.isPreferenceEnabled && current.isAuthorized
    }

    suspend fun handleUserSessionChange(userSwitched: Boolean) {
        _state.update { it.copy(registrationError = null) }
        if (userSwitched) {
            unregisterFromBackend()
        }
        refreshAuthorizationStatus()
        refreshPreference()
        if (!_state.value.isAuthorized && _state.value.isPreferenceEnabled) {
            setPreferenceEnabled(false)
        }
        syncDeviceWithBackendIfNeeded()
    }

    suspend fun applyNotificationsPreference(enabled: Boolean) {
        _state.update { it.copy(registrationError = null) }
        if (enabled) {
            if (!areSystemNotificationsEnabled()) {
                setPreferenceEnabled(false)
                return
            }
            setPreferenceEnabled(true)
            analyticsTracker.logNotificationEnabled()
            ensureFcmToken()
            syncDeviceWithBackendIfNeeded()
        } else {
            setPreferenceEnabled(false)
            unregisterFromBackend()
        }
    }

    suspend fun onPermissionGranted() {
        refreshAuthorizationStatus()
        if (_state.value.isPreferenceEnabled) {
            ensureFcmToken()
            syncDeviceWithBackendIfNeeded()
        }
    }

    fun updateFcmToken(token: String?) {
        if (token.isNullOrBlank()) return
        fcmToken = token
        _state.update { it.copy(fcmToken = token) }
        scope.launch {
            syncDeviceWithBackendIfNeeded()
        }
    }

    fun openSystemSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    suspend fun unregisterFromBackend() {
        val token = fcmToken ?: return
        _state.update { it.copy(isSyncing = true) }
        try {
            devicesRepository.unregister(token)
        } catch (error: Throwable) {
            if (!error.isCancellation()) {
                _state.update { it.copy(registrationError = error.localizedMessage) }
            }
        } finally {
            _state.update { it.copy(isSyncing = false) }
        }
    }

    private suspend fun refreshPreference() {
        val uid = authRepository.currentUser.value?.uid
        val enabled = notificationPreferences.isEnabled(uid)
        _state.update { it.copy(isPreferenceEnabled = enabled) }
    }

    private suspend fun setPreferenceEnabled(enabled: Boolean) {
        val uid = authRepository.currentUser.value?.uid
        notificationPreferences.setEnabled(uid, enabled)
        _state.update { it.copy(isPreferenceEnabled = enabled) }
    }

    private suspend fun ensureFcmToken() {
        if (!fcmToken.isNullOrBlank()) return
        runCatching {
            updateFcmToken(FirebaseMessaging.getInstance().token.await())
        }.onFailure { error ->
            _state.update { it.copy(registrationError = error.localizedMessage) }
        }
    }

    private suspend fun syncDeviceWithBackendIfNeeded() {
        if (!_state.value.isPreferenceEnabled) return
        if (authRepository.currentUser.value == null) return
        if (!areSystemNotificationsEnabled()) return

        val token = fcmToken ?: run {
            ensureFcmToken()
            fcmToken
        } ?: return

        _state.update { it.copy(isSyncing = true, registrationError = null) }
        try {
            devicesRepository.register(
                fcmToken = token,
                language = backendLanguageCode(),
                timezone = TimeZone.getDefault().id,
            )
        } catch (error: Throwable) {
            if (!error.isCancellation()) {
                _state.update { it.copy(registrationError = error.localizedMessage) }
            }
        } finally {
            _state.update { it.copy(isSyncing = false) }
        }
    }

    private fun areSystemNotificationsEnabled(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun backendLanguageCode(): String {
        return Locale.getDefault().language.ifBlank { "es" }
    }

    private fun Throwable.isCancellation(): Boolean {
        return this is kotlinx.coroutines.CancellationException
    }
}
