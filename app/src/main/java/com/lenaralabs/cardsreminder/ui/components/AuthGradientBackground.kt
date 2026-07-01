package com.lenaralabs.cardsreminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lenaralabs.cardsreminder.ui.theme.AuthBackgroundLight
import com.lenaralabs.cardsreminder.ui.theme.BrandPrimaryDark
import com.lenaralabs.cardsreminder.ui.theme.BrandPrimaryLight

@Composable
fun AuthGradientBackground(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val baseColor = if (isDarkTheme) Color(0xFF0D1021) else AuthBackgroundLight
    val brandPrimary = if (isDarkTheme) BrandPrimaryDark else BrandPrimaryLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        baseColor,
                        brandPrimary.copy(alpha = if (isDarkTheme) 0.22f else 0.16f),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
            ),
        content = content,
    )
}
