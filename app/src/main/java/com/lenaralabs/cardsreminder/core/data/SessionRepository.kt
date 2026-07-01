package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.core.notifications.PushNotificationManager
import kotlinx.coroutines.runBlocking

class SessionRepository(
    private val authRepository: AuthRepository,
    private val cardsRepository: CardsRepository,
    private val paymentsRepository: PaymentsRepository,
    private val ownersRepository: OwnersRepository,
    private val userRepository: UserRepository,
    private val feedbackRepository: FeedbackRepository,
    private val pushNotificationManager: PushNotificationManager,
) {
    fun signOut() {
        runBlocking {
            pushNotificationManager.unregisterFromBackend()
            userRepository.resetSession()
        }
        authRepository.signOut()
        resetAll()
    }

    fun resetAll() {
        cardsRepository.resetSession()
        paymentsRepository.resetSession()
        ownersRepository.resetSession()
        feedbackRepository.resetSession()
    }
}
