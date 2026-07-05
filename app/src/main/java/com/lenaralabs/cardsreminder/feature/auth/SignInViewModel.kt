package com.lenaralabs.cardsreminder.feature.auth

import android.content.Context
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignInViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(
        context: Context,
        credentialManager: CredentialManager,
        webClientId: String,
        noGoogleAccountMessage: String,
        signInFailedMessage: String,
    ) {
        viewModelScope.launch {
            _uiState.value = SignInUiState(isSigningIn = true)

            val credentialResult = runCatching {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                credentialManager.getCredential(
                    context = context,
                    request = request,
                )
            }

            credentialResult.onFailure { error ->
                onCredentialFailed(
                    error = error,
                    noGoogleAccountMessage = noGoogleAccountMessage,
                    signInFailedMessage = signInFailedMessage,
                )
                return@launch
            }

            handleCredential(
                credential = credentialResult.getOrThrow().credential,
                signInFailedMessage = signInFailedMessage,
            )
        }
    }

    private suspend fun handleCredential(
        credential: Credential,
        signInFailedMessage: String,
    ) {
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            _uiState.value = SignInUiState(
                errorMessage = signInFailedMessage,
            )
            return
        }

        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

        authRepository.signInWithGoogle(idToken)
            .onSuccess {
                _uiState.value = SignInUiState()
            }
            .onFailure { error ->
                _uiState.value = SignInUiState(
                    errorMessage = error.localizedMessage ?: signInFailedMessage,
                )
            }
    }

    private fun onCredentialFailed(
        error: Throwable,
        noGoogleAccountMessage: String,
        signInFailedMessage: String,
    ) {
        when (error) {
            is GetCredentialCancellationException -> {
                _uiState.value = SignInUiState()
            }

            is NoCredentialException -> {
                _uiState.value = SignInUiState(
                    errorMessage = noGoogleAccountMessage,
                )
            }

            is GetCredentialException -> {
                _uiState.value = SignInUiState(
                    errorMessage = error.errorMessage?.toString()
                        ?: error.localizedMessage
                        ?: signInFailedMessage,
                )
            }

            else -> {
                _uiState.value = SignInUiState(
                    errorMessage = error.localizedMessage ?: signInFailedMessage,
                )
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SignInViewModel(authRepository) as T
        }
    }
}
