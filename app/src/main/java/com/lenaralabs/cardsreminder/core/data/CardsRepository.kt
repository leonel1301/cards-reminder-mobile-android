package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.CreateCardRequest
import com.lenaralabs.cardsreminder.core.model.UpdateCardRequest
import com.lenaralabs.cardsreminder.core.network.ApiException
import com.lenaralabs.cardsreminder.core.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CardsRepository(
    private val apiService: ApiService,
) {
    private val _cards = MutableStateFlow<List<ApiCard>>(emptyList())
    val cards: StateFlow<List<ApiCard>> = _cards.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val activeCards: List<ApiCard>
        get() = _cards.value.filter { it.isActive }

    suspend fun fetchCards(silentUnlessEmpty: Boolean = true) {
        _isLoading.value = true
        if (!silentUnlessEmpty || _cards.value.isEmpty()) {
            _errorMessage.value = null
        }

        try {
            val cardsList: List<ApiCard> = apiService.getDecoded("/cards")
            _cards.value = cardsList.sortedBy { it.name.lowercase() }
            _errorMessage.value = null
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            if (!silentUnlessEmpty || _cards.value.isEmpty()) {
                _errorMessage.value = error.localizedMessage
            }
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createCard(request: CreateCardRequest): Result<ApiCard> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val card: ApiCard = apiService.postDecoded("/cards", request)
            _cards.update { current ->
                (current + card).sortedBy { it.name.lowercase() }
            }
            Result.success(card)
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

    suspend fun updateCard(id: String, request: UpdateCardRequest): Result<ApiCard> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val card: ApiCard = apiService.patchDecoded("/cards/$id", request)
            _cards.update { current ->
                val updated = current.map { if (it.id == id) card else it }
                if (updated.any { it.id == id }) updated.sortedBy { it.name.lowercase() }
                else (updated + card).sortedBy { it.name.lowercase() }
            }
            Result.success(card)
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

    suspend fun deleteCard(id: String): Result<Unit> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            apiService.delete("/cards/$id")
            _cards.update { current -> current.filterNot { it.id == id } }
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

    fun updateCardLocally(card: ApiCard) {
        _cards.update { current ->
            current.map { if (it.id == card.id) card else it }
                .sortedBy { it.name.lowercase() }
        }
    }

    fun resetSession() {
        _cards.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
    }

    fun clearError() {
        _errorMessage.update { null }
    }
}
