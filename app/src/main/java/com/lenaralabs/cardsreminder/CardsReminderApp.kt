package com.lenaralabs.cardsreminder

import android.app.Application
import com.lenaralabs.cardsreminder.core.analytics.AnalyticsTracker
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.core.data.CardsRepository
import com.lenaralabs.cardsreminder.core.data.DevicesRepository
import com.lenaralabs.cardsreminder.core.data.FeedbackRepository
import com.lenaralabs.cardsreminder.core.data.NotificationPreferences
import com.lenaralabs.cardsreminder.core.data.OnboardingPreferences
import com.lenaralabs.cardsreminder.core.data.ThemePreferences
import com.lenaralabs.cardsreminder.core.data.OwnersRepository
import com.lenaralabs.cardsreminder.core.data.PaymentsRepository
import com.lenaralabs.cardsreminder.core.data.SessionRepository
import com.lenaralabs.cardsreminder.core.data.UserRepository
import com.lenaralabs.cardsreminder.core.data.UserSessionCache
import com.lenaralabs.cardsreminder.core.network.ApiService
import com.lenaralabs.cardsreminder.core.notifications.NotificationChannels
import com.lenaralabs.cardsreminder.core.notifications.PushNotificationManager

class CardsReminderApp : Application() {

    lateinit var analyticsTracker: AnalyticsTracker
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var apiService: ApiService
        private set

    lateinit var cardsRepository: CardsRepository
        private set

    lateinit var paymentsRepository: PaymentsRepository
        private set

    lateinit var ownersRepository: OwnersRepository
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var feedbackRepository: FeedbackRepository
        private set

    lateinit var devicesRepository: DevicesRepository
        private set

    lateinit var sessionRepository: SessionRepository
        private set

    lateinit var pushNotificationManager: PushNotificationManager
        private set

    lateinit var notificationPreferences: NotificationPreferences
        private set

    lateinit var onboardingPreferences: OnboardingPreferences
        private set

    lateinit var themePreferences: ThemePreferences
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        analyticsTracker = AnalyticsTracker(this)
        authRepository = AuthRepository(analyticsTracker)
        apiService = ApiService(authRepository)
        cardsRepository = CardsRepository(apiService, analyticsTracker)
        paymentsRepository = PaymentsRepository(apiService, analyticsTracker)
        ownersRepository = OwnersRepository(apiService)
        userRepository = UserRepository(
            apiService = apiService,
            userSessionCache = UserSessionCache(this),
        )
        feedbackRepository = FeedbackRepository(apiService)
        devicesRepository = DevicesRepository(apiService)
        notificationPreferences = NotificationPreferences(this)
        onboardingPreferences = OnboardingPreferences(this)
        themePreferences = ThemePreferences(this)
        pushNotificationManager = PushNotificationManager(
            context = this,
            authRepository = authRepository,
            devicesRepository = devicesRepository,
            notificationPreferences = notificationPreferences,
            analyticsTracker = analyticsTracker,
        )
        sessionRepository = SessionRepository(
            authRepository = authRepository,
            cardsRepository = cardsRepository,
            paymentsRepository = paymentsRepository,
            ownersRepository = ownersRepository,
            userRepository = userRepository,
            feedbackRepository = feedbackRepository,
            pushNotificationManager = pushNotificationManager,
        )
        pushNotificationManager.initialize()
    }
}
