package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.util.toComposeColor
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun CalendarLegendRows(
    cards: List<ApiCard>,
    billingPeriods: List<BillingPeriodInstance>,
    payments: List<BillingPeriodInstance>,
    selection: CalendarSelection?,
    onSelectionChange: (CalendarSelection?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LegendSection(title = stringResource(R.string.calendar_legend_cards)) {
            cards.forEach { card ->
                CardTag(
                    card = card,
                    isSelected = selection is CalendarSelection.Card &&
                        (selection as CalendarSelection.Card).cardId == card.id,
                    onClick = { toggleSelection(CalendarSelection.Card(card.id), selection, onSelectionChange) },
                )
            }
        }

        LegendSection(title = stringResource(R.string.calendar_legend_billing_periods)) {
            billingPeriods.forEach { period ->
                SelectableChip(
                    isSelected = selection is CalendarSelection.BillingPeriod &&
                        (selection as CalendarSelection.BillingPeriod).periodId == period.id,
                    onClick = {
                        toggleSelection(CalendarSelection.BillingPeriod(period.id), selection, onSelectionChange)
                    },
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(period.cardColorHex.toComposeColor().copy(alpha = 0.75f)),
                            )
                            Text(
                                text = period.periodLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        Text(
                            text = period.cardName,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                        )
                    }
                }
            }
        }

        LegendSection(title = stringResource(R.string.calendar_legend_payments)) {
            payments.forEach { period ->
                SelectableChip(
                    isSelected = selection is CalendarSelection.Payment &&
                        (selection as CalendarSelection.Payment).periodId == period.id,
                    onClick = {
                        toggleSelection(CalendarSelection.Payment(period.id), selection, onSelectionChange)
                    },
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(period.cardColorHex.toComposeColor())
                                    .border(1.dp, Color.White, CircleShape),
                            )
                            Text(
                                text = stringResource(R.string.payment_summary, period.paymentDateLabel),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        Text(
                            text = stringResource(
                                R.string.payment_detail_format,
                                stringResource(R.string.payment_summary, period.paymentDateLabel),
                                period.periodLabel,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder

    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.secondaryText,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun CardTag(
    card: ApiCard,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    val cardColor = card.color
    val tagShape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .clip(tagShape)
            .background(colors.calendarTagSurface)
            .border(
                width = 1.dp,
                color = if (isSelected) cardColor.copy(alpha = 0.55f) else Color.Transparent,
                shape = tagShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(cardColor),
        )
        Text(
            text = card.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) cardColor else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SelectableChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    val chipShape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(colors.calendarTagSurface)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = chipShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        content()
    }
}

private fun toggleSelection(
    newSelection: CalendarSelection,
    currentSelection: CalendarSelection?,
    onSelectionChange: (CalendarSelection?) -> Unit,
) {
    onSelectionChange(if (currentSelection == newSelection) null else newSelection)
}
