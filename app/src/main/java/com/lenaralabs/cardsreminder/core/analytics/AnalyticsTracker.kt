package com.lenaralabs.cardsreminder.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsScreens {
    const val SIGN_IN = "Sign In"
    const val ONBOARDING = "Onboarding"
    const val POST_LOGIN_SETUP = "Post Login Setup"
    const val TIMELINE = "Timeline"
    const val CALENDAR = "Calendar"
    const val CARDS = "Cards"
    const val PROFILE = "Profile"
}

class AnalyticsTracker(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    fun logScreenView(screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    fun logCardCreated() {
        firebaseAnalytics.logEvent(EVENT_CARD_CREATED, null)
    }

    fun logNotificationEnabled() {
        firebaseAnalytics.logEvent(EVENT_NOTIFICATION_ENABLED, null)
    }

    fun logPaymentCompleted() {
        firebaseAnalytics.logEvent(EVENT_PAYMENT_COMPLETED, null)
    }

    fun logPurchaseDayChecked() {
        firebaseAnalytics.logEvent(EVENT_PURCHASE_DAY_CHECKED, null)
    }

    private companion object {
        const val EVENT_CARD_CREATED = "card_created"
        const val EVENT_NOTIFICATION_ENABLED = "notification_enabled"
        const val EVENT_PAYMENT_COMPLETED = "payment_completed"
        const val EVENT_PURCHASE_DAY_CHECKED = "purchase_day_checked"
    }
}
