package com.lenaralabs.cardsreminder.feature.timeline

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.CardPaymentStatusKind
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenu
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenuItem
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

private val ItemShape = RoundedCornerShape(12.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineEventListItem(
    event: TimelineEvent,
    isMarkingPaid: Boolean,
    onOpenPayments: () -> Unit,
    onMarkPaid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val kindColors = event.kind.colors(colors)
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(ItemShape)
                .background(colors.sheetItemSurface)
                .combinedClickable(
                    onClick = onOpenPayments,
                    onLongClick = { menuExpanded = true },
                ),
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(event.card.color),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(event.kind.titleRes),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = event.card.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (event.card.lastFourDigits != "0000") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black),
                                        )
                                    }
                                }
                                Text(
                                    text = event.card.lastFourDigits,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.secondaryText,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Text(
                        text = timelineEventSubtitle(event),
                        style = MaterialTheme.typography.labelSmall,
                        color = kindColors.foreground.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                TimelineEventStatusChip(
                    status = event.status,
                    kind = event.kind,
                )
            }
        }

        if (isMarkingPaid) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(ItemShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                AppInlineLoadingIndicator(size = 32.dp)
            }
        }

        AppDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            val menuActions = buildList {
                if (event.canMarkPaid()) {
                    add(
                        TimelineMenuAction(R.string.payments_mark_paid) {
                            menuExpanded = false
                            onMarkPaid()
                        },
                    )
                }
                add(
                    TimelineMenuAction(R.string.payments_view_history) {
                        menuExpanded = false
                        onOpenPayments()
                    },
                )
            }
            menuActions.forEachIndexed { index, action ->
                AppDropdownMenuItem(
                    index = index,
                    count = menuActions.size,
                    text = { Text(stringResource(action.labelRes)) },
                    onClick = action.onClick,
                    leadingIcon = {
                        when (action.labelRes) {
                            R.string.payments_mark_paid -> Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            else -> Icon(Icons.Filled.Schedule, contentDescription = null)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun TimelineEventStatusChip(
    status: ApiCardStatus,
    kind: TimelineEventKind,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val kindColors = kind.colors(colors)
    val labelRes = when (status.kind) {
        CardPaymentStatusKind.Paid -> R.string.card_status_paid
        CardPaymentStatusKind.Overdue -> R.string.card_status_overdue
        CardPaymentStatusKind.Urgent -> R.string.card_status_urgent
        CardPaymentStatusKind.DueSoon -> R.string.card_status_due_soon
        CardPaymentStatusKind.OptimalDay -> R.string.card_status_optimal_day
        CardPaymentStatusKind.OnTrack -> R.string.card_status_on_track
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = kindColors.background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = kind.statusIcon,
                contentDescription = null,
                tint = kindColors.foreground,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(labelRes),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = kindColors.foreground,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun timelineEventSubtitle(event: TimelineEvent): String {
    return when (event.kind) {
        TimelineEventKind.Overdue -> stringResource(
            R.string.payments_days_overdue,
            event.status.daysOverdue,
        )

        TimelineEventKind.PaymentDueToday,
        TimelineEventKind.CycleEndsToday,
        -> stringResource(R.string.timeline_time_today)

        TimelineEventKind.Urgent,
        TimelineEventKind.DueSoon,
        TimelineEventKind.OnTrack,
        -> if (event.status.daysUntilPayment == 1) {
            stringResource(R.string.timeline_time_tomorrow)
        } else {
            stringResource(R.string.payments_days_until, event.status.daysUntilPayment)
        }

        TimelineEventKind.OptimalToday -> stringResource(
            R.string.payments_days_until,
            event.status.daysUntilPayment,
        )

        TimelineEventKind.Paid -> stringResource(R.string.payments_paid_this_cycle)
    }
}

private data class KindColors(
    val background: androidx.compose.ui.graphics.Color,
    val foreground: androidx.compose.ui.graphics.Color,
)

private val TimelineEventKind.statusIcon: ImageVector
    get() = when (this) {
        TimelineEventKind.Overdue -> Icons.Filled.Error
        TimelineEventKind.PaymentDueToday -> Icons.Filled.NotificationsActive
        TimelineEventKind.Urgent -> Icons.Filled.Schedule
        TimelineEventKind.DueSoon -> Icons.Filled.Event
        TimelineEventKind.OptimalToday -> Icons.Filled.AutoAwesome
        TimelineEventKind.CycleEndsToday -> Icons.Outlined.ContentCut
        TimelineEventKind.Paid -> Icons.Filled.Verified
        TimelineEventKind.OnTrack -> Icons.Filled.TaskAlt
    }

private fun TimelineEventKind.colors(
    palette: com.lenaralabs.cardsreminder.ui.theme.CardsReminderColors,
): KindColors {
    return when (this) {
        TimelineEventKind.Overdue -> KindColors(palette.redStateBackground, palette.redStateForeground)
        TimelineEventKind.PaymentDueToday,
        TimelineEventKind.Urgent,
        -> KindColors(palette.amberStateBackground, palette.amberStateForeground)

        TimelineEventKind.DueSoon,
        TimelineEventKind.OptimalToday,
        -> KindColors(palette.violetStateBackground, palette.violetStateForeground)

        TimelineEventKind.CycleEndsToday -> KindColors(
            palette.violetStateBackground.copy(alpha = 0.75f),
            palette.violetStateForeground,
        )

        TimelineEventKind.Paid -> KindColors(palette.emeraldStateBackground, palette.emeraldStateForeground)
        TimelineEventKind.OnTrack -> KindColors(palette.onTrackStateBackground, palette.onTrackStateForeground)
    }
}

private data class TimelineMenuAction(
    @param:StringRes val labelRes: Int,
    val onClick: () -> Unit,
)
