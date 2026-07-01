package com.lenaralabs.cardsreminder.core.util

import androidx.compose.ui.graphics.Color

fun String.toComposeColor(): Color {
    val sanitized = filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    if (sanitized.isEmpty()) return Color.Gray

    val value = sanitized.toLongOrNull(16) ?: return Color.Gray

    return when (sanitized.length) {
        6 -> Color(
            red = ((value shr 16) and 0xFF) / 255f,
            green = ((value shr 8) and 0xFF) / 255f,
            blue = (value and 0xFF) / 255f,
        )

        8 -> Color(
            red = ((value shr 24) and 0xFF) / 255f,
            green = ((value shr 16) and 0xFF) / 255f,
            blue = ((value shr 8) and 0xFF) / 255f,
            alpha = (value and 0xFF) / 255f,
        )

        else -> Color.Gray
    }
}

fun Color.isLightForegroundPreferred(): Boolean {
    val luminance = (0.299f * red) + (0.587f * green) + (0.114f * blue)
    return luminance > 0.62f
}
