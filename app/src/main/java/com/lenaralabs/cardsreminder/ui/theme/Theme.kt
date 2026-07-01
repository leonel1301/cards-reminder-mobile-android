package com.lenaralabs.cardsreminder.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = AppPrimaryLight,
    onPrimary = OnAppPrimaryLight,
    primaryContainer = AppPrimaryContainerLight,
    onPrimaryContainer = OnAppPrimaryContainerLight,
    background = AppBackgroundLight,
    onBackground = Color(0xFF1A1F36),
    surface = AppBackgroundLight,
    onSurface = Color(0xFF1A1F36),
    outline = DefaultBorderLight,
    surfaceVariant = HeaderSurfaceLight,
    onSurfaceVariant = SecondaryTextLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppPrimaryDark,
    onPrimary = OnAppPrimaryDark,
    primaryContainer = AppPrimaryContainerDark,
    onPrimaryContainer = OnAppPrimaryContainerDark,
    background = AppBackgroundDark,
    onBackground = Color(0xFFF8F9FA),
    surface = CardSurfaceDark,
    onSurface = Color(0xFFF8F9FA),
    onSurfaceVariant = SecondaryTextDark,
    outline = DefaultBorderDark,
    surfaceVariant = HeaderSurfaceDark,
)

@Composable
fun CardsreminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val cardsReminderColors = if (darkTheme) DarkCardsReminderColors else LightCardsReminderColors

    CompositionLocalProvider(LocalCardsReminderColors provides cardsReminderColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
