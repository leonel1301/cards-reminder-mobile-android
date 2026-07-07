package com.lenaralabs.cardsreminder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

private val DarkThemeAccentLift = Color(0xFFB0B0B5)
private const val DarkSwatchLuminanceThreshold = 0.22f
private const val DarkSwatchLiftFraction = 0.58f

fun Color.isDarkCardSwatch(): Boolean = luminance() < DarkSwatchLuminanceThreshold

/**
 * Lifts very dark card swatches in dark theme so accents stay visible without
 * per-day borders that break continuous period bars in the calendar.
 */
fun Color.adaptedCardAccent(isDarkTheme: Boolean): Color {
    if (!isDarkTheme || !isDarkCardSwatch()) return this
    return lerp(this, DarkThemeAccentLift, DarkSwatchLiftFraction)
}

@Composable
fun isDarkTheme(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f
