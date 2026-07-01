package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.CardPaymentStatusKind
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun CardStatusBadge(
    status: ApiCardStatus,
    modifier: Modifier = Modifier,
) {
    val colors = androidx.compose.material3.MaterialTheme.cardsReminder
    val labelRes = when (status.kind) {
        CardPaymentStatusKind.Paid -> R.string.card_status_paid
        CardPaymentStatusKind.Overdue -> R.string.card_status_overdue
        CardPaymentStatusKind.Urgent -> R.string.card_status_urgent
        CardPaymentStatusKind.DueSoon -> R.string.card_status_due_soon
        CardPaymentStatusKind.OptimalDay -> R.string.card_status_optimal_day
        CardPaymentStatusKind.OnTrack -> R.string.card_status_on_track
    }

    val background = when (status.kind) {
        CardPaymentStatusKind.Paid -> colors.emeraldStateBackground
        CardPaymentStatusKind.Overdue -> colors.redStateBackground
        CardPaymentStatusKind.Urgent -> colors.amberStateBackground
        CardPaymentStatusKind.DueSoon -> colors.violetStateBackground
        CardPaymentStatusKind.OptimalDay -> colors.violetStateBackground
        CardPaymentStatusKind.OnTrack -> colors.onTrackStateBackground
    }

    val foreground = when (status.kind) {
        CardPaymentStatusKind.Paid -> colors.emeraldStateForeground
        CardPaymentStatusKind.Overdue -> colors.redStateForeground
        CardPaymentStatusKind.Urgent -> colors.amberStateForeground
        CardPaymentStatusKind.DueSoon -> colors.violetStateForeground
        CardPaymentStatusKind.OptimalDay -> colors.violetStateForeground
        CardPaymentStatusKind.OnTrack -> colors.onTrackStateForeground
    }

    Text(
        text = stringResource(labelRes),
        modifier = modifier
            .background(background, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = foreground,
    )
}
