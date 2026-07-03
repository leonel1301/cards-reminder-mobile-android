package com.lenaralabs.cardsreminder.feature.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.DashboardSummary
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

private data class SummaryChip(
    val label: String,
    val foreground: Color,
    val background: Color,
)

@Composable
fun TimelineSummaryStrip(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val chips = buildList {
        if (summary.overdue > 0) {
            add(
                SummaryChip(
                    label = stringResource(R.string.dashboard_overdue_count, summary.overdue),
                    foreground = colors.redStateForeground,
                    background = colors.redStateBackground,
                ),
            )
        }
        if (summary.urgent > 0) {
            add(
                SummaryChip(
                    label = stringResource(R.string.dashboard_urgent_count, summary.urgent),
                    foreground = colors.amberStateForeground,
                    background = colors.amberStateBackground,
                ),
            )
        }
        if (summary.dueSoon > 0) {
            add(
                SummaryChip(
                    label = stringResource(R.string.dashboard_due_soon_count, summary.dueSoon),
                    foreground = colors.violetStateForeground,
                    background = colors.violetStateBackground,
                ),
            )
        }
        if (summary.optimalDay > 0) {
            add(
                SummaryChip(
                    label = stringResource(R.string.timeline_summary_optimal_count, summary.optimalDay),
                    foreground = colors.violetStateForeground,
                    background = colors.violetStateBackground,
                ),
            )
        }
        if (summary.paid > 0 && isEmpty()) {
            add(
                SummaryChip(
                    label = stringResource(R.string.timeline_summary_paid_count, summary.paid),
                    foreground = colors.emeraldStateForeground,
                    background = colors.emeraldStateBackground,
                ),
            )
        }
    }

    if (chips.isEmpty()) return

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(chips) { index, chip ->
            SmoothReveal(
                visible = true,
                index = index,
                style = RevealStyle.Section,
            ) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    enabled = false,
                    label = { Text(chip.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chip.background.copy(alpha = 0.85f),
                        selectedLabelColor = chip.foreground,
                        disabledSelectedContainerColor = chip.background.copy(alpha = 0.85f),
                        disabledLabelColor = chip.foreground,
                    ),
                )
            }
        }
    }
}
