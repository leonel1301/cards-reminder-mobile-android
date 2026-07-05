package com.lenaralabs.cardsreminder.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lenaralabs.cardsreminder.ui.components.AppLoadingIndicator
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun SessionLoadingScreen(
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.appBackground),
        contentAlignment = Alignment.Center,
    ) {
        AppLoadingIndicator(color = colors.primaryAction)
    }
}
