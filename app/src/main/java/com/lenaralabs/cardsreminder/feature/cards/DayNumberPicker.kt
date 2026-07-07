package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayNumberPicker(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    displayText: String? = null,
    notSetLabel: String? = null,
    onShowHelp: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.cardsReminder
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExpandedChange(!expanded) },
            ) {
                OutlinedTextField(
                    value = displayText ?: value.toString(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(label) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.rotate(if (expanded) 180f else 0f),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = colorScheme.onBackground,
                        disabledLabelColor = colors.secondaryText,
                        disabledBorderColor = colors.defaultBorder,
                        disabledTrailingIconColor = colors.secondaryText,
                    ),
                )
            }
            if (onShowHelp != null) {
                IconButton(onClick = onShowHelp) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.sheetItemSurface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                notSetLabel?.let { clearLabel ->
                    val isNotSetSelected = value == 0
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isNotSetSelected) {
                                    colorScheme.primaryContainer
                                } else {
                                    colorScheme.surface.copy(alpha = 0f)
                                },
                            )
                            .clickable {
                                onValueChange(0)
                                onExpandedChange(false)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = clearLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isNotSetSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isNotSetSelected) {
                                colorScheme.onPrimaryContainer
                            } else {
                                colorScheme.onBackground
                            },
                        )
                    }
                }

                (1..31).toList().chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        week.forEach { day ->
                            val isSelected = value == day && value != 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) {
                                            colorScheme.primaryContainer
                                        } else {
                                            colorScheme.surface.copy(alpha = 0f)
                                        },
                                    )
                                    .clickable {
                                        onValueChange(day)
                                        onExpandedChange(false)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        colorScheme.onPrimaryContainer
                                    } else {
                                        colorScheme.onBackground
                                    },
                                )
                            }
                        }
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
