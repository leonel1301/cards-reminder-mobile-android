package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiFeedback
import com.lenaralabs.cardsreminder.core.model.CreateFeedbackRequest
import com.lenaralabs.cardsreminder.core.model.UpdateFeedbackRequest
import com.lenaralabs.cardsreminder.core.network.ApiException
import com.lenaralabs.cardsreminder.core.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedbackRepository(
    private val apiService: ApiService,
) {
    private val _feedbacks = MutableStateFlow<List<ApiFeedback>>(emptyList())
    val feedbacks: StateFlow<List<ApiFeedback>> = _feedbacks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun fetchFeedbacks() {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val list: List<ApiFeedback> = apiService.getDecoded("/me/feedback")
            _feedbacks.value = list.sortedByDescending { it.createdAt }
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createFeedback(request: CreateFeedbackRequest): Result<ApiFeedback> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val feedback: ApiFeedback = apiService.postDecoded("/feedback", request)
            _feedbacks.value = (_feedbacks.value + feedback).sortedByDescending { it.createdAt }
            Result.success(feedback)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateFeedback(id: String, request: UpdateFeedbackRequest): Result<ApiFeedback> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val feedback: ApiFeedback = apiService.patchDecoded("/feedback/$id", request)
            _feedbacks.value = _feedbacks.value
                .map { if (it.id == id) feedback else it }
                .sortedByDescending { it.createdAt }
            Result.success(feedback)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun deleteFeedback(id: String): Result<Unit> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            apiService.delete("/feedback/$id")
            _feedbacks.value = _feedbacks.value.filterNot { it.id == id }
            Result.success(Unit)
        } catch (error: Throwable) {
            _errorMessage.value = error.localizedMessage
            Result.failure(error)
        } finally {
            _isLoading.value = false
        }
    }

    fun resetSession() {
        _feedbacks.value = emptyList()
        _errorMessage.value = null
        _isLoading.value = false
    }
}
