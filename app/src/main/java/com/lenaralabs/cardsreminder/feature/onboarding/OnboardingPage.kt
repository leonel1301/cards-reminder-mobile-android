package com.lenaralabs.cardsreminder.feature.onboarding

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.lenaralabs.cardsreminder.R

enum class OnboardingPageStyle {
    Primary,
    Amber,
    Violet,
    Emerald,
}

data class OnboardingPage(
    val icon: ImageVector,
    val style: OnboardingPageStyle,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
)

object OnboardingPages {
    val all = listOf(
        OnboardingPage(
            icon = Icons.Outlined.CreditCard,
            style = OnboardingPageStyle.Primary,
            titleRes = R.string.onboarding_welcome_title,
            subtitleRes = R.string.onboarding_welcome_subtitle,
        ),
        OnboardingPage(
            icon = Icons.Outlined.WbSunny,
            style = OnboardingPageStyle.Amber,
            titleRes = R.string.onboarding_today_title,
            subtitleRes = R.string.onboarding_today_subtitle,
        ),
        OnboardingPage(
            icon = Icons.Outlined.CalendarMonth,
            style = OnboardingPageStyle.Violet,
            titleRes = R.string.onboarding_calendar_title,
            subtitleRes = R.string.onboarding_calendar_subtitle,
        ),
        OnboardingPage(
            icon = Icons.Outlined.Group,
            style = OnboardingPageStyle.Primary,
            titleRes = R.string.onboarding_owners_title,
            subtitleRes = R.string.onboarding_owners_subtitle,
        ),
        OnboardingPage(
            icon = Icons.Outlined.NotificationsActive,
            style = OnboardingPageStyle.Emerald,
            titleRes = R.string.onboarding_control_title,
            subtitleRes = R.string.onboarding_control_subtitle,
        ),
    )
}
