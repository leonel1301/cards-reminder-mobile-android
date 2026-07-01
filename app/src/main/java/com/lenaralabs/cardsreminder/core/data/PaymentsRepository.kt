package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.BestForPurchase
import com.lenaralabs.cardsreminder.core.model.CardPaymentsResponse
import com.lenaralabs.cardsreminder.core.model.CurrentCycleResponse
import com.lenaralabs.cardsreminder.core.model.DashboardCardEntry
import com.lenaralabs.cardsreminder.core.model.DashboardResponse
import com.lenaralabs.cardsreminder.core.model.DashboardSummary
import com.lenaralabs.cardsreminder.core.model.MarkPaidRequest
import com.lenaralabs.cardsreminder.core.model.MarkPaidResponse
import com.lenaralabs.cardsreminder.core.model.OptimalPurchaseDaysResponse
import com.lenaralabs.cardsreminder.core.network.ApiException
import com.lenaralabs.cardsreminder.core.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PaymentsRepository(
    private val apiService: ApiService,
) {
    private val _statusByCardId = MutableStateFlow<Map<String, ApiCardStatus>>(emptyMap())
    val statusByCardId: StateFlow<Map<String, ApiCardStatus>> = _statusByCardId.asStateFlow()

    private val _summary = MutableStateFlow<DashboardSummary?>(null)
    val summary: StateFlow<DashboardSummary?> = _summary.asStateFlow()

    private val _dashboardCards = MutableStateFlow<List<DashboardCardEntry>>(emptyList())
    val dashboardCards: StateFlow<List<DashboardCardEntry>> = _dashboardCards.asStateFlow()

    private val _bestForPurchase = MutableStateFlow<BestForPurchase?>(null)
    val bestForPurchase: StateFlow<BestForPurchase?> = _bestForPurchase.asStateFlow()

    private val _isLoadingDashboard = MutableStateFlow(false)
    val isLoadingDashboard: StateFlow<Boolean> = _isLoadingDashboard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val hasCachedDashboard: Boolean
        get() = _summary.value != null || _statusByCardId.value.isNotEmpty()

    fun statusFor(cardId: String): ApiCardStatus? = _statusByCardId.value[cardId]

    suspend fun fetchDashboard(silentUnlessEmpty: Boolean = true) {
        _isLoadingDashboard.value = true
        if (!silentUnlessEmpty || !hasCachedDashboard) {
            _errorMessage.value = null
        }

        try {
            val response: DashboardResponse = apiService.getDecoded("/dashboard")
            _summary.value = response.summary
            _dashboardCards.value = response.cards
            _bestForPurchase.value = response.bestForPurchase
            _statusByCardId.value = response.cards.associate { it.card.id to it.status }
            _errorMessage.value = null
        } catch (error: ApiException.NotAuthenticated) {
            resetSession()
        } catch (error: Throwable) {
            if (!silentUnlessEmpty || !hasCachedDashboard) {
                _errorMessage.value = error.localizedMessage
            }
        } finally {
            _isLoadingDashboard.value = false
        }
    }

    suspend fun fetchCurrentCycle(cardId: String): CurrentCycleResponse? {
        return runCatching {
            apiService.getDecoded<CurrentCycleResponse>("/cards/$cardId/current-cycle")
        }.getOrNull()?.also { response ->
            _statusByCardId.update { it + (cardId to response.status) }
        }
    }

    suspend fun fetchOptimalPurchaseDays(cardId: String): OptimalPurchaseDaysResponse? {
        return runCatching {
            apiService.getDecoded<OptimalPurchaseDaysResponse>("/cards/$cardId/optimal-purchase-days")
        }.getOrNull()
    }

    suspend fun fetchPayments(cardId: String): CardPaymentsResponse? {
        return runCatching {
            apiService.getDecoded<CardPaymentsResponse>("/cards/$cardId/payments")
        }.getOrNull()
    }

    suspend fun markAsPaid(cardId: String, notes: String? = null): Result<MarkPaidResponse> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val response: MarkPaidResponse = apiService.postDecoded(
                path = "/cards/$cardId/payments",
                body = MarkPaidRequest(notes = notes),
            )
            _statusByCardId.update { it + (cardId to response.status) }
            fetchDashboard(silentUnlessEmpty = false)
            Result.success(response)
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

    fun resetSession() {
        _statusByCardId.value = emptyMap()
        _summary.value = null
        _dashboardCards.value = emptyList()
        _bestForPurchase.value = null
        _errorMessage.value = null
        _isLoading.value = false
        _isLoadingDashboard.value = false
    }
}
