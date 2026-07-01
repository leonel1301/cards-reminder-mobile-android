package com.lenaralabs.cardsreminder.feature.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.feature.cards.CardStatusBadge
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

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
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onOpenPayments,
                    onLongClick = { menuExpanded = true },
                ),
            colors = ListItemDefaults.colors(containerColor = colors.sheetItemSurface),
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(kindColors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = event.kind.icon,
                        contentDescription = null,
                        tint = kindColors.foreground,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(event.kind.titleRes),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = timelineEventSubtitle(event),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.secondaryText,
                        )
                    }
                    CardStatusBadge(status = event.status)
                }
            },
            supportingContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = event.card.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                    )
                    Text(
                        text = event.card.maskedNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = colors.secondaryText,
                        maxLines = 1,
                    )
                }
            },
        )

        if (isMarkingPaid) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            if (event.canMarkPaid()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.payments_mark_paid)) },
                    onClick = {
                        menuExpanded = false
                        onMarkPaid()
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.payments_view_history)) },
                onClick = {
                    menuExpanded = false
                    onOpenPayments()
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Schedule, contentDescription = null)
                },
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

private val TimelineEventKind.icon: ImageVector
    get() = when (this) {
        TimelineEventKind.Overdue -> Icons.Outlined.Warning
        TimelineEventKind.PaymentDueToday -> Icons.Outlined.NotificationsActive
        TimelineEventKind.Urgent -> Icons.Outlined.Schedule
        TimelineEventKind.DueSoon -> Icons.Outlined.CalendarMonth
        TimelineEventKind.OptimalToday -> Icons.Outlined.AutoAwesome
        TimelineEventKind.CycleEndsToday -> Icons.Outlined.ContentCut
        TimelineEventKind.Paid -> Icons.Outlined.Verified
        TimelineEventKind.OnTrack -> Icons.Filled.CheckCircle
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
