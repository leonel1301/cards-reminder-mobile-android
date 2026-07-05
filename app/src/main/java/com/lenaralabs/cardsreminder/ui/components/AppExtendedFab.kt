package com.lenaralabs.cardsreminder.ui.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun AppExtendedFab(
    onClick: () -> Unit,
    icon: ImageVector,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    iconContentDescription: String? = null,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        expanded = expanded,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
            )
        },
        text = text,
    )
}
