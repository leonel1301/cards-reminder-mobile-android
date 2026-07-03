package com.lenaralabs.cardsreminder.ui.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale

private const val PRESS_SCALE = 0.97f
private const val PRESS_ALPHA = 0.92f
private const val PRESS_DURATION_MS = 160

fun Modifier.pressScaleEffect(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val isPressed = enabled && pressed
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESS_SCALE else 1f,
        animationSpec = tween(PRESS_DURATION_MS),
        label = "pressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) PRESS_ALPHA else 1f,
        animationSpec = tween(PRESS_DURATION_MS),
        label = "pressAlpha",
    )
    scale(scale).alpha(alpha)
}
