package com.lenaralabs.cardsreminder.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import com.lenaralabs.cardsreminder.core.data.OnboardingPreferences
import com.lenaralabs.cardsreminder.core.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RootViewModel(
    private val onboardingPreferences: OnboardingPreferences,
    authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _showSplash = MutableStateFlow(true)
    val showSplash: StateFlow<Boolean> = _showSplash.asStateFlow()

    val hasCompletedOnboarding: StateFlow<Boolean> = onboardingPreferences.hasCompletedOnboarding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val isSignedIn: StateFlow<Boolean> = authRepository.isSignedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = authRepository.currentUser.value != null,
        )

    val sessionState = userRepository.sessionState

    fun onSplashFinished() {
        _showSplash.value = false
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingPreferences.setOnboardingCompleted(true)
        }
    }

    fun refreshUserSession(firebaseUid: String) {
        viewModelScope.launch {
            userRepository.refreshCurrentUser(firebaseUid)
        }
    }

    class Factory(
        private val onboardingPreferences: OnboardingPreferences,
        private val authRepository: AuthRepository,
        private val userRepository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RootViewModel(
                onboardingPreferences = onboardingPreferences,
                authRepository = authRepository,
                userRepository = userRepository,
            ) as T
        }
    }
}
