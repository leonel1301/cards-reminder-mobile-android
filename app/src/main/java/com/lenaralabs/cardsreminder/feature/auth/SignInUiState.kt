package com.lenaralabs.cardsreminder.feature.auth

data class SignInUiState(
    val isSigningIn: Boolean = false,
    val errorMessage: String? = null,
)
