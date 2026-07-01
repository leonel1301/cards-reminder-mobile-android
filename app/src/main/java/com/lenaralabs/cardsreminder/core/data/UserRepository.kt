package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiTerms
import com.lenaralabs.cardsreminder.core.model.ApiUser
import com.lenaralabs.cardsreminder.core.model.hasAcceptedTerms
import com.lenaralabs.cardsreminder.core.network.ApiException
import com.lenaralabs.cardsreminder.core.network.ApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class UserAuthSessionState(
    val user: ApiUser? = null,
    val requiredTermsVersion: String? = null,
    val loadedFirebaseUid: String? = null,
    val hasResolvedTermsStatus: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val needsTermsAcceptance: Boolean
        get() {
            val profile = user ?: return false
            val version = requiredTermsVersion ?: return false
            return !profile.hasAcceptedTerms(version)
        }
}

class UserRepository(
    private val apiService: ApiService,
    private val userSessionCache: UserSessionCache,
) {
    private val _sessionState = MutableStateFlow(UserAuthSessionState())
    val sessionState: StateFlow<UserAuthSessionState> = _sessionState.asStateFlow()

    private val _user = MutableStateFlow<ApiUser?>(null)
    val user: StateFlow<ApiUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun refreshCurrentUser(firebaseUid: String) {
        _sessionState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        _isLoading.value = true
        _errorMessage.value = null

        try {
            coroutineScope {
                val termsDeferred = async {
                    apiService.getDecoded<ApiTerms>(path = "/terms", authenticated = false)
                }
                val userDeferred = async {
                    apiService.getDecoded<ApiUser>(path = "/me")
                }
                applySession(
                    user = userDeferred.await(),
                    requiredTermsVersion = termsDeferred.await().termsVersion,
                    firebaseUid = firebaseUid,
                )
            }
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            val message = error.localizedMessage
            _errorMessage.value = message
            val cached = userSessionCache.load(firebaseUid)
            if (cached != null) {
                applySession(
                    user = cached.user,
                    requiredTermsVersion = cached.requiredTermsVersion,
                    firebaseUid = firebaseUid,
                )
            } else {
                _sessionState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = message,
                        hasResolvedTermsStatus = true,
                    )
                }
            }
        } finally {
            _isLoading.value = false
            _sessionState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun fetchProfile() {
        if (_user.value != null && _sessionState.value.loadedFirebaseUid != null) {
            return
        }
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val profile: ApiUser = apiService.getDecoded("/me")
            _user.value = profile
            _sessionState.update { state ->
                state.copy(user = profile)
            }
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun acceptTerms(): Boolean {
        _sessionState.update {
            it.copy(isLoading = true, errorMessage = null)
        }
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val user: ApiUser = apiService.patchDecoded("/me/accept-terms")
            val requiredVersion = _sessionState.value.requiredTermsVersion
                ?: apiService.getDecoded<ApiTerms>(path = "/terms", authenticated = false).termsVersion
            val firebaseUid = _sessionState.value.loadedFirebaseUid ?: return false

            applySession(
                user = user,
                requiredTermsVersion = requiredVersion,
                firebaseUid = firebaseUid,
            )
            !_sessionState.value.needsTermsAcceptance
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
            false
        } catch (error: Throwable) {
            val message = error.localizedMessage
            _errorMessage.value = message
            _sessionState.update { it.copy(errorMessage = message) }
            false
        } finally {
            _isLoading.value = false
            _sessionState.update { it.copy(isLoading = false) }
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            apiService.delete("/me")
            resetSession()
            Result.success(Unit)
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
            Result.failure(error)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun resetSession() {
        _user.value = null
        _errorMessage.value = null
        _isLoading.value = false
        _sessionState.value = UserAuthSessionState()
        userSessionCache.clear()
    }

    private suspend fun applySession(
        user: ApiUser,
        requiredTermsVersion: String,
        firebaseUid: String,
    ) {
        userSessionCache.save(
            user = user,
            requiredTermsVersion = requiredTermsVersion,
            firebaseUid = firebaseUid,
        )
        _user.value = user
        _errorMessage.value = null
        _sessionState.value = UserAuthSessionState(
            user = user,
            requiredTermsVersion = requiredTermsVersion,
            loadedFirebaseUid = firebaseUid,
            hasResolvedTermsStatus = true,
            isLoading = false,
            errorMessage = null,
        )
    }
}
