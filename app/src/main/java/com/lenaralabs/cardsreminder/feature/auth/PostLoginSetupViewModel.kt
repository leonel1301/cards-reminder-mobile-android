package com.lenaralabs.cardsreminder.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.data.UserRepository
import kotlinx.coroutines.launch

class PostLoginSetupViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    val sessionState = userRepository.sessionState

    fun acceptTerms() {
        viewModelScope.launch {
            userRepository.acceptTerms()
        }
    }

    class Factory(
        private val userRepository: UserRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PostLoginSetupViewModel(userRepository) as T
        }
    }
}
