package com.lenaralabs.cardsreminder.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = MaterialTheme.cardsReminder
    val scheme = MaterialTheme.colorScheme

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = scheme.onPrimary,
            checkedTrackColor = scheme.primary,
            uncheckedThumbColor = colors.switchUncheckedThumb,
            uncheckedTrackColor = colors.switchUncheckedTrack,
            disabledCheckedThumbColor = colors.switchDisabledThumb,
            disabledCheckedTrackColor = colors.switchDisabledTrack,
            disabledUncheckedThumbColor = colors.switchDisabledThumb,
            disabledUncheckedTrackColor = colors.switchDisabledTrack,
        ),
    )
}
