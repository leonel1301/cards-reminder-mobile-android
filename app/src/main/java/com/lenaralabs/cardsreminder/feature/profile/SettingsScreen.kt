package com.lenaralabs.cardsreminder.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.data.ThemeMode
import com.lenaralabs.cardsreminder.core.util.AppLinks
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDeletingAccount: Boolean,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as CardsReminderApp
    val themeMode by application.themePreferences.themeMode.collectAsStateWithLifecycle(
        initialValue = ThemeMode.SYSTEM,
    )
    val colors = MaterialTheme.cardsReminder
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    var showTheme by rememberSaveable { mutableStateOf(false) }

    if (showTheme) {
        ThemeSettingsScreen(
            onBack = { showTheme = false },
        )
        return
    }

    if (showNotifications) {
        NotificationsSettingsScreen(
            onBack = { showNotifications = false },
        )
        return
    }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_account_confirm_title)) },
            text = { Text(stringResource(R.string.delete_account_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccount()
                    },
                    enabled = !isDeletingAccount,
                ) {
                    Text(
                        text = stringResource(R.string.action_delete_account),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.appBackground,
        contentWindowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Top + WindowInsetsSides.Horizontal,
        ),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_settings_title)) },
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
                .verticalScroll(rememberScrollState()),
        ) {
            ProfileSectionHeader(
                title = stringResource(R.string.section_account),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            ProfileActionRow(
                title = stringResource(R.string.action_sign_out),
                icon = Icons.Outlined.Logout,
                onClick = onSignOut,
            )
            ProfileActionRow(
                title = stringResource(R.string.action_delete_account),
                icon = Icons.Outlined.DeleteForever,
                onClick = { showDeleteConfirm = true },
                isDestructive = true,
            )

            ProfileSectionHeader(
                title = stringResource(R.string.section_preferences),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            ProfileActionRow(
                title = stringResource(R.string.action_notifications),
                icon = Icons.Outlined.Notifications,
                onClick = { showNotifications = true },
            )
            ProfileActionRow(
                title = stringResource(R.string.action_theme),
                icon = Icons.Outlined.DarkMode,
                value = themeModeLabel(themeMode),
                onClick = { showTheme = true },
            )

            ProfileSectionHeader(
                title = stringResource(R.string.section_other),
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            )
            ProfileActionRow(
                title = stringResource(R.string.action_privacy_policy),
                icon = Icons.Outlined.PrivacyTip,
                onClick = { openUrl(AppLinks.PRIVACY) },
            )
            ProfileActionRow(
                title = stringResource(R.string.action_terms_of_service),
                icon = Icons.Outlined.Description,
                onClick = { openUrl(AppLinks.TERMS) },
            )
        }
    }
}

@Composable
private fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}
