package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.height(52.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .then(
                    if (isToday) {
                        Modifier.background(Color(0xFF007AFF), CircleShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isToday) Color.White else MaterialTheme.colorScheme.onBackground,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (bar.showBar) {
            val shape = periodBarShape(bar.isPeriodStart, bar.isPeriodEnd)
            val barColor = bar.color.copy(alpha = barOpacity(bar))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .padding(
                        start = if (bar.isPeriodStart) 2.dp else 0.dp,
                        end = if (bar.isPeriodEnd) 2.dp else 0.dp,
                    )
                    .clip(shape)
                    .background(barColor)
                    .then(
                        if (bar.barHighlighted) {
                            Modifier.border(1.5.dp, bar.color, shape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }

        if (bar.showPaymentPin) {
            val pinSize = if (bar.pinHighlighted) 10.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(pinSize)
                    .shadow(
                        elevation = if (bar.pinHighlighted) 3.dp else 0.dp,
                        shape = CircleShape,
                        spotColor = bar.color.copy(alpha = 0.45f),
                    )
                    .clip(CircleShape)
                    .background(bar.color.copy(alpha = if (bar.isDimmed) 0.25f else 1f))
                    .border(
                        width = if (bar.pinHighlighted) 2.dp else 1.5.dp,
                        color = Color.White,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

private fun barOpacity(bar: CardBarDisplay): Float {
    if (bar.isDimmed) return 0.12f
    if (bar.barHighlighted) return 0.75f
    return 0.35f
}

private fun periodBarShape(isStart: Boolean, isEnd: Boolean) = RoundedCornerShape(
    topStart = if (isStart) 2.5.dp else 0.dp,
    bottomStart = if (isStart) 2.5.dp else 0.dp,
    topEnd = if (isEnd) 2.5.dp else 0.dp,
    bottomEnd = if (isEnd) 2.5.dp else 0.dp,
)
