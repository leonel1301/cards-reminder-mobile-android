package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.DashboardSummary
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun DashboardSummaryBanner(
    summary: DashboardSummary,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val parts = buildList {
        if (summary.overdue > 0) {
            add(stringResource(R.string.dashboard_overdue_count, summary.overdue))
        }
        if (summary.urgent > 0) {
            add(stringResource(R.string.dashboard_urgent_count, summary.urgent))
        }
        if (summary.dueSoon > 0) {
            add(stringResource(R.string.dashboard_due_soon_count, summary.dueSoon))
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.amberStateBackground.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = colors.amberStateForeground,
        )
        Text(
            text = parts.joinToString(" · "),
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}
