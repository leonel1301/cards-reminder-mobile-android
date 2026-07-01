package com.lenaralabs.cardsreminder.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun TimelineFeaturedCard(
    card: ApiCard,
    status: ApiCardStatus,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.violetStateBackground.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.violetStateBackground.copy(alpha = 0.95f),
                            colors.violetStateBackground.copy(alpha = 0.55f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = colors.violetStateForeground.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = colors.violetStateForeground,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.timeline_featured_spending_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.violetStateForeground,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(width = 56.dp, height = 36.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = card.color,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreditCard,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = card.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = card.maskedNumber,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = colors.secondaryText,
                    )
                    Text(
                        text = stringResource(R.string.payments_days_until, status.daysUntilPayment),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = colors.violetStateForeground,
                    )
                }
            }
        }
    }
}

@Composable
fun TimelinePurchaseInsightRow(
    why: String,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val annotatedText = buildInsightAnnotatedString(why, colors)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.sheetItemSurface, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(50),
            color = colors.violetStateBackground,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = colors.violetStateForeground,
                modifier = Modifier.padding(5.dp),
            )
        }

        Text(
            text = annotatedText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
        )
    }
}

private val datePattern = Regex("""\d{1,2}/\d{1,2}/\d{4}""")

private fun buildInsightAnnotatedString(
    why: String,
    colors: com.lenaralabs.cardsreminder.ui.theme.CardsReminderColors,
) = buildAnnotatedString {
    if (why.isBlank()) return@buildAnnotatedString

    var lastIndex = 0
    val matches = datePattern.findAll(why).toList()
    val bodyStyle = SpanStyle(color = colors.secondaryText)

    if (matches.isEmpty()) {
        withStyle(bodyStyle) {
            append(why)
        }
        return@buildAnnotatedString
    }

    matches.forEach { match ->
        if (match.range.first > lastIndex) {
            withStyle(bodyStyle) {
                append(why.substring(lastIndex, match.range.first))
            }
        }
        withStyle(
            SpanStyle(
                background = colors.violetStateBackground,
                color = colors.violetStateForeground,
                fontWeight = FontWeight.SemiBold,
            ),
        ) {
            append(match.value)
        }
        lastIndex = match.range.last + 1
    }

    if (lastIndex < why.length) {
        withStyle(bodyStyle) {
            append(why.substring(lastIndex))
        }
    }
}
