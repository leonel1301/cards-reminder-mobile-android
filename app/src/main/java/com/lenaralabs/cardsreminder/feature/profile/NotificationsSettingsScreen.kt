package com.lenaralabs.cardsreminder.feature.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.BuildConfig
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.notifications.NotificationAuthorizationStatus
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.components.AppSwitch
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    onBack: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val viewModel: NotificationsSettingsViewModel = viewModel(
        factory = NotificationsSettingsViewModel.Factory(
            pushNotificationManager = application.pushNotificationManager,
            authRepository = application.authRepository,
        ),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val firebaseIdToken by viewModel.firebaseIdToken.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.onPermissionGranted()
            viewModel.applyNotificationsPreference(enabled = true)
        } else {
            viewModel.applyNotificationsPreference(enabled = false)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun onToggleChange(enabled: Boolean) {
        if (!enabled) {
            viewModel.applyNotificationsPreference(enabled = false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                application,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                viewModel.applyNotificationsPreference(enabled = true)
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.applyNotificationsPreference(enabled = true)
        }
    }

    val statusIcon = when {
        state.isPreferenceEnabled && state.isAuthorized -> Icons.Outlined.NotificationsActive
        state.authorizationStatus == NotificationAuthorizationStatus.Denied -> Icons.Outlined.NotificationsOff
        else -> Icons.Outlined.Notifications
    }
    val statusColor = when {
        state.isPreferenceEnabled && state.isAuthorized -> colors.emeraldStateForeground
        else -> colors.secondaryText
    }
    val statusTitle = when {
        state.isPreferenceEnabled && state.isAuthorized -> stringResource(R.string.notifications_status_enabled)
        state.authorizationStatus == NotificationAuthorizationStatus.Denied -> {
            stringResource(R.string.notifications_status_denied)
        }
        else -> stringResource(R.string.notifications_status_disabled)
    }
    val statusDescription = when {
        state.isPreferenceEnabled && state.isAuthorized -> stringResource(R.string.notifications_description_enabled)
        state.authorizationStatus == NotificationAuthorizationStatus.Denied -> {
            stringResource(R.string.notifications_description_denied)
        }
        else -> stringResource(R.string.notifications_description_disabled)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.appBackground,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_cancel),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.appBackground,
                    scrolledContainerColor = colors.appBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.notifications_enable_toggle),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.notifications_toggle_footer),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                AppSwitch(
                    checked = state.isPreferenceEnabled,
                    onCheckedChange = ::onToggleChange,
                    enabled = state.authorizationStatus != NotificationAuthorizationStatus.Denied,
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.padding(4.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = statusDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                    )
                }
            }

            if (state.authorizationStatus == NotificationAuthorizationStatus.Denied) {
                TextButton(
                    onClick = viewModel::openSystemSettings,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.notifications_open_settings))
                }
            }

            if (state.isSyncing) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppInlineLoadingIndicator(size = 32.dp)
                    Text(
                        text = stringResource(R.string.notifications_syncing),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                    )
                }
            }

            state.registrationError?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.redStateForeground,
                )
            }

            if (BuildConfig.DEBUG) {
                state.fcmToken?.let { token ->
                    HorizontalDivider()
                    NotificationDebugTokenSection(
                        title = stringResource(R.string.notifications_debug_token_section),
                        token = token,
                    )
                }
                firebaseIdToken?.let { token ->
                    NotificationDebugTokenSection(
                        title = stringResource(R.string.notifications_debug_firebase_token_section),
                        token = token,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationDebugTokenSection(
    title: String,
    token: String,
) {
    val colors = MaterialTheme.cardsReminder

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        SelectionContainer {
            Text(
                text = token,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = colors.secondaryText,
            )
        }
    }
}
