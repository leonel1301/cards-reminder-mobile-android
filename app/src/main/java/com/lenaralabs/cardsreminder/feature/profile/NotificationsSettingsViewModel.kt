package com.lenaralabs.cardsreminder.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.BuildConfig
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.core.notifications.PushNotificationManager
import com.lenaralabs.cardsreminder.core.notifications.PushNotificationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsSettingsViewModel(
    private val pushNotificationManager: PushNotificationManager,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val state: StateFlow<PushNotificationState> = pushNotificationManager.state

    private val _firebaseIdToken = MutableStateFlow<String?>(null)
    val firebaseIdToken: StateFlow<String?> = _firebaseIdToken.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        pushNotificationManager.refreshAuthorizationStatus()
        viewModelScope.launch {
            pushNotificationManager.handleUserSessionChange(userSwitched = false)
            refreshDebugTokens()
        }
    }

    private suspend fun refreshDebugTokens() {
        if (!BuildConfig.DEBUG) return
        _firebaseIdToken.value = authRepository.getIdToken()
    }

    fun applyNotificationsPreference(enabled: Boolean) {
        viewModelScope.launch {
            pushNotificationManager.applyNotificationsPreference(enabled)
        }
    }

    fun onPermissionGranted() {
        viewModelScope.launch {
            pushNotificationManager.onPermissionGranted()
        }
    }

    fun openSystemSettings() {
        pushNotificationManager.openSystemSettings()
    }

    class Factory(
        private val pushNotificationManager: PushNotificationManager,
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationsSettingsViewModel(
                pushNotificationManager = pushNotificationManager,
                authRepository = authRepository,
            ) as T
        }
    }
}
