package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.util.toComposeColor
import com.lenaralabs.cardsreminder.ui.theme.adaptedCardAccent
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import com.lenaralabs.cardsreminder.ui.theme.isDarkTheme

@Composable
fun CalendarMonthInsightsBar(
    insights: CalendarMonthInsights,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val darkTheme = isDarkTheme()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InsightChip(
            label = stringResource(R.string.calendar_insights_payments, insights.paymentsThisMonth),
            modifier = Modifier.weight(1f),
        )

        insights.nextPayment?.let { payment ->
            val subtitle = when (payment.daysUntil) {
                0 -> stringResource(R.string.calendar_insights_payment_today, payment.cardName)
                null -> stringResource(
                    R.string.calendar_insights_payment_on,
                    payment.cardName,
                    payment.paymentDateLabel,
                )
                in 1..Int.MAX_VALUE -> stringResource(
                    R.string.calendar_insights_payment_in_days,
                    payment.daysUntil,
                    payment.cardName,
                )
                else -> stringResource(
                    R.string.calendar_insights_payment_overdue,
                    payment.cardName,
                    payment.paymentDateLabel,
                )
            }

            Row(
                modifier = Modifier
                    .weight(1.4f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.sheetItemSurface)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val paymentColor = payment.cardColorHex.toComposeColor().adaptedCardAccent(darkTheme)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(paymentColor),
                )
                Text(
                    text = subtitle,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                )
                if (payment.isPaid) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.emeraldStateForeground,
                        )
                        Text(
                            text = stringResource(R.string.calendar_insights_paid),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.emeraldStateForeground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.sheetItemSurface)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun CalendarDayDetailPanel(
    day: Int,
    events: List<CalendarDayEvent>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.sheetItemSurface.copy(alpha = 0.85f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.calendar_day_detail_title, day),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.calendar_clear_selection),
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.calendar_day_detail_empty),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        } else {
            events.forEach { event ->
                CalendarDayEventRow(event = event)
            }
        }
    }
}

@Composable
private fun CalendarDayEventRow(event: CalendarDayEvent) {
    val colors = MaterialTheme.cardsReminder
    val darkTheme = isDarkTheme()
    val cardColor = event.cardColorHex.toComposeColor().adaptedCardAccent(darkTheme)
    val title = when (event.type) {
        CalendarDayEventType.Payment -> stringResource(R.string.calendar_day_event_payment, event.cardName)
        CalendarDayEventType.PeriodStart -> stringResource(R.string.calendar_day_event_period_start, event.cardName)
        CalendarDayEventType.PeriodEnd -> stringResource(R.string.calendar_day_event_period_end, event.cardName)
        CalendarDayEventType.InPeriod -> stringResource(R.string.calendar_day_event_in_period, event.cardName)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when (event.type) {
            CalendarDayEventType.Payment -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cardColor),
                )
            }
            else -> {
                val barShape = RoundedCornerShape(2.dp)
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(5.dp)
                        .clip(barShape)
                        .background(cardColor.copy(alpha = 0.75f)),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (event.type == CalendarDayEventType.Payment && event.label != event.periodLabel) {
                Text(
                    text = event.periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                )
            } else if (event.type != CalendarDayEventType.Payment) {
                Text(
                    text = event.periodLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                )
            }
        }
    }
}

@Composable
fun CalendarMiniLegend(
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MiniLegendItem(
            label = stringResource(R.string.calendar_mini_legend_period),
            indicator = {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.secondaryText.copy(alpha = 0.55f)),
                )
            },
        )
        MiniLegendItem(
            label = stringResource(R.string.calendar_mini_legend_payment),
            indicator = {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.secondaryText.copy(alpha = 0.75f)),
                )
            },
        )
    }
}

@Composable
private fun MiniLegendItem(
    label: String,
    indicator: @Composable () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        indicator()
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.secondaryText,
        )
    }
}
