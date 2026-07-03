package com.lenaralabs.cardsreminder.core.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.lenaralabs.cardsreminder.CardsReminderApp

@Composable
fun TrackScreen(screenName: String) {
    val analyticsTracker = (LocalContext.current.applicationContext as CardsReminderApp).analyticsTracker
    LaunchedEffect(screenName) {
        analyticsTracker.logScreenView(screenName)
    }
}
