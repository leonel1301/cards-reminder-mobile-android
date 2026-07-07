package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenaralabs.cardsreminder.ui.theme.adaptedCardAccent
import com.lenaralabs.cardsreminder.ui.theme.isDarkTheme

data class CardBarDisplay(
    val cardId: String,
    val color: Color,
    val showBar: Boolean,
    val isPeriodStart: Boolean,
    val isPeriodEnd: Boolean,
    val showPaymentPin: Boolean,
    val barHighlighted: Boolean,
    val pinHighlighted: Boolean,
    val isDimmed: Boolean,
)

@Composable
fun DayCell(
    day: Int,
    isToday: Boolean,
    bars: List<CardBarDisplay>,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .then(
                    when {
                        isToday -> Modifier.background(Color(0xFF007AFF), CircleShape)
                        isSelected -> Modifier
                            .border(1.5.dp, colorScheme.primary, CircleShape)
                            .background(colorScheme.primaryContainer.copy(alpha = 0.45f), CircleShape)
                        else -> Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isToday -> Color.White
                    isSelected -> colorScheme.onPrimaryContainer
                    else -> colorScheme.onBackground
                },
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            bars.forEach { bar ->
                BarRow(bar)
            }
        }
    }
}

@Composable
private fun BarRow(bar: CardBarDisplay) {
    val darkTheme = isDarkTheme()
    val accentColor = bar.color.adaptedCardAccent(darkTheme)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (bar.showBar) {
            val shape = periodBarShape(bar.isPeriodStart, bar.isPeriodEnd)
            val barColor = accentColor.copy(alpha = barOpacity(bar))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(
                        start = if (bar.isPeriodStart) 2.dp else 0.dp,
                        end = if (bar.isPeriodEnd) 2.dp else 0.dp,
                    )
                    .clip(shape)
                    .background(barColor)
                    .then(
                        if (bar.barHighlighted) {
                            Modifier.border(1.5.dp, accentColor.copy(alpha = 0.95f), shape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }

        if (bar.showPaymentPin) {
            val pinSize = if (bar.pinHighlighted) 8.dp else 6.dp
            val pinAlpha = if (bar.isDimmed) 0.3f else 1f

            Box(
                modifier = Modifier
                    .size(pinSize)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = pinAlpha)),
            )
        }
    }
}

private fun barOpacity(bar: CardBarDisplay): Float {
    if (bar.isDimmed) return 0.15f
    if (bar.barHighlighted) return 0.85f
    return 0.45f
}

private fun periodBarShape(isStart: Boolean, isEnd: Boolean) = RoundedCornerShape(
    topStart = if (isStart) 4.dp else 0.dp,
    bottomStart = if (isStart) 4.dp else 0.dp,
    topEnd = if (isEnd) 4.dp else 0.dp,
    bottomEnd = if (isEnd) 4.dp else 0.dp,
)
