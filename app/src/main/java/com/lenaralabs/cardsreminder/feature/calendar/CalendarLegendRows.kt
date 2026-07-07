@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
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
import com.lenaralabs.cardsreminder.ui.theme.adaptedCardAccent
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import com.lenaralabs.cardsreminder.ui.theme.isDarkTheme

@Composable
fun CalendarLegendRows(
    cards: List<ApiCard>,
    billingPeriods: List<BillingPeriodInstance>,
    payments: List<BillingPeriodInstance>,
    selection: CalendarSelection?,
    onSelectionChange: (CalendarSelection?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isDarkTheme()

    Column(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LegendSection(title = stringResource(R.string.calendar_legend_cards)) {
            ConnectedLegendGroup(itemCount = cards.size) { index ->
                val card = cards[index]
                val isSelected = selection is CalendarSelection.Card &&
                    (selection as CalendarSelection.Card).cardId == card.id
                CardLegendToggle(
                    card = card,
                    isSelected = isSelected,
                    isDarkTheme = darkTheme,
                    shapes = connectedLegendShapes(index = index, count = cards.size),
                    onClick = {
                        toggleSelection(CalendarSelection.Card(card.id), selection, onSelectionChange)
                    },
                )
            }
        }

        LegendSection(title = stringResource(R.string.calendar_legend_billing_periods)) {
            ConnectedLegendGroup(itemCount = billingPeriods.size) { index ->
                val period = billingPeriods[index]
                val isSelected = selection is CalendarSelection.BillingPeriod &&
                    (selection as CalendarSelection.BillingPeriod).periodId == period.id
                PeriodLegendToggle(
                    isSelected = isSelected,
                    shapes = connectedLegendShapes(index = index, count = billingPeriods.size),
                    onClick = {
                        toggleSelection(
                            CalendarSelection.BillingPeriod(period.id),
                            selection,
                            onSelectionChange,
                        )
                    },
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val periodColor = period.cardColorHex.toComposeColor().adaptedCardAccent(darkTheme)
                            val barShape = RoundedCornerShape(2.dp)
                            Box(
                                modifier = Modifier
                                    .width(28.dp)
                                    .height(5.dp)
                                    .clip(barShape)
                                    .background(periodColor.copy(alpha = 0.75f)),
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
                            color = MaterialTheme.cardsReminder.secondaryText,
                        )
                    }
                }
            }
        }

        LegendSection(title = stringResource(R.string.calendar_legend_payments)) {
            ConnectedLegendGroup(itemCount = payments.size) { index ->
                val period = payments[index]
                val isSelected = selection is CalendarSelection.Payment &&
                    (selection as CalendarSelection.Payment).periodId == period.id
                PeriodLegendToggle(
                    isSelected = isSelected,
                    shapes = connectedLegendShapes(index = index, count = payments.size),
                    onClick = {
                        toggleSelection(
                            CalendarSelection.Payment(period.id),
                            selection,
                            onSelectionChange,
                        )
                    },
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val paymentColor = period.cardColorHex.toComposeColor().adaptedCardAccent(darkTheme)
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(paymentColor),
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
                            color = MaterialTheme.cardsReminder.secondaryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectedLegendGroup(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int) -> Unit,
) {
    if (itemCount == 0) return

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        verticalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        repeat(itemCount) { index ->
            itemContent(index)
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
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = colors.secondaryText,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun CardLegendToggle(
    card: ApiCard,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    shapes: androidx.compose.material3.ToggleButtonShapes,
    onClick: () -> Unit,
) {
    val cardColor = card.color.adaptedCardAccent(isDarkTheme)
    val dotShape = CircleShape

    ToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick() },
        shapes = shapes,
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = MaterialTheme.cardsReminder.sheetItemSurface,
            contentColor = MaterialTheme.colorScheme.onBackground,
            checkedContainerColor = cardColor.copy(alpha = 0.14f),
            checkedContentColor = cardColor,
        ),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(dotShape)
                    .background(cardColor),
            )
            Text(
                text = card.name,
                style = MaterialTheme.typography.labelLargeEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PeriodLegendToggle(
    isSelected: Boolean,
    shapes: androidx.compose.material3.ToggleButtonShapes,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    ToggleButton(
        checked = isSelected,
        onCheckedChange = { onClick() },
        shapes = shapes,
        colors = ToggleButtonDefaults.toggleButtonColors(
            containerColor = MaterialTheme.cardsReminder.sheetItemSurface,
            contentColor = MaterialTheme.colorScheme.onBackground,
            checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
    ) {
        content()
    }
}

@Composable
private fun connectedLegendShapes(
    index: Int,
    count: Int,
): androidx.compose.material3.ToggleButtonShapes {
    return when {
        count == 1 -> ToggleButtonDefaults.shapesFor(ButtonDefaults.MinHeight)
        index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
        index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
    }
}

private fun toggleSelection(
    newSelection: CalendarSelection,
    currentSelection: CalendarSelection?,
    onSelectionChange: (CalendarSelection?) -> Unit,
) {
    onSelectionChange(if (currentSelection == newSelection) null else newSelection)
}
