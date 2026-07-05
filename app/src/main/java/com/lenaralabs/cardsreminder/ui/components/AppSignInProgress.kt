@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.lenaralabs.cardsreminder.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppExpressiveLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    trackHeight: Dp = 5.dp,
) {
    LinearWavyProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = color,
        trackColor = trackColor,
        stroke = expressiveLinearStroke(trackHeight),
        trackStroke = expressiveLinearStroke(trackHeight = 4.dp),
    )
}

@Composable
fun AppCircularLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    size: Dp = 48.dp,
) {
    CircularWavyProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        trackColor = trackColor,
    )
}

@Composable
private fun expressiveLinearStroke(trackHeight: Dp): Stroke {
    val density = LocalDensity.current
    return with(density) {
        Stroke(
            width = trackHeight.toPx(),
            cap = StrokeCap.Round,
        )
    }
}
