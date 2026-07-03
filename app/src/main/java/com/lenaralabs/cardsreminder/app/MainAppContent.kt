package com.lenaralabs.cardsreminder.app

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.analytics.AnalyticsScreens
import com.lenaralabs.cardsreminder.feature.calendar.CalendarScreen
import com.lenaralabs.cardsreminder.feature.cards.CardsScreen
import com.lenaralabs.cardsreminder.feature.profile.ProfileScreen
import com.lenaralabs.cardsreminder.feature.timeline.TimelineScreen
import com.lenaralabs.cardsreminder.ui.animation.AppMotion
import com.lenaralabs.cardsreminder.ui.theme.CardsreminderTheme
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

private enum class AppTab(
    @param:StringRes val labelRes: Int,
) {
    Today(R.string.tab_timeline),
    Calendar(R.string.tab_calendar),
    Cards(R.string.tab_cards),
    Profile(R.string.tab_profile),
}

private fun AppTab.icon(isDarkTheme: Boolean): ImageVector = when (this) {
    AppTab.Today -> if (isDarkTheme) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny
    AppTab.Calendar -> Icons.Outlined.CalendarMonth
    AppTab.Cards -> Icons.Outlined.CreditCard
    AppTab.Profile -> Icons.Outlined.AccountCircle
}

private fun AppTab.analyticsScreenName(): String = when (this) {
    AppTab.Today -> AnalyticsScreens.TIMELINE
    AppTab.Calendar -> AnalyticsScreens.CALENDAR
    AppTab.Cards -> AnalyticsScreens.CARDS
    AppTab.Profile -> AnalyticsScreens.PROFILE
}

@Composable
fun MainAppContent() {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Today) }
    val analyticsTracker = (LocalContext.current.applicationContext as CardsReminderApp).analyticsTracker

    LaunchedEffect(selectedTab) {
        analyticsTracker.logScreenView(selectedTab.analyticsScreenName())
    }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val colors = MaterialTheme.cardsReminder

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.appBackground,
        bottomBar = {
            NavigationBar(
                containerColor = colors.appBackground,
                tonalElevation = 0.dp,
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        icon = {
                            Icon(
                                imageVector = tab.icon(isDarkTheme),
                                contentDescription = stringResource(tab.labelRes),
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            transitionSpec = { AppMotion.navFadeIn() togetherWith AppMotion.navFadeOut() },
            label = "mainTab",
        ) { tab ->
            when (tab) {
                AppTab.Today -> TimelineScreen(modifier = Modifier.fillMaxSize())
                AppTab.Calendar -> CalendarScreen(modifier = Modifier.fillMaxSize())
                AppTab.Cards -> CardsScreen(modifier = Modifier.fillMaxSize())
                AppTab.Profile -> ProfileScreen(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainAppContentPreview() {
    CardsreminderTheme {
        MainAppContent()
    }
}
