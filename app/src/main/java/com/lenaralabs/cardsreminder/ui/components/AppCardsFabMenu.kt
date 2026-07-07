package com.lenaralabs.cardsreminder.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.lenaralabs.cardsreminder.R

@Composable
fun AppCardsFabMenu(
    onAddCard: () -> Unit,
    onEnableReminders: () -> Unit,
    modifier: Modifier = Modifier,
    showRemindersAction: Boolean = true,
    containerColor: Color,
    contentColor: Color,
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = fabMenuExpanded) {
        fabMenuExpanded = false
    }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            ToggleFloatingActionButton(
                checked = fabMenuExpanded,
                onCheckedChange = { fabMenuExpanded = it },
                containerColor = { containerColor },
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                    }
                }
                Icon(
                    painter = rememberVectorPainter(imageVector),
                    contentDescription = stringResource(
                        if (fabMenuExpanded) R.string.action_cancel else R.string.action_add_card,
                    ),
                    tint = contentColor,
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                fabMenuExpanded = false
                onAddCard()
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(R.string.action_add_card)) },
        )

        if (showRemindersAction) {
            FloatingActionButtonMenuItem(
                onClick = {
                    fabMenuExpanded = false
                    onEnableReminders()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                    )
                },
                text = { Text(stringResource(R.string.card_create_reminders_enable)) },
            )
        }
    }
}
