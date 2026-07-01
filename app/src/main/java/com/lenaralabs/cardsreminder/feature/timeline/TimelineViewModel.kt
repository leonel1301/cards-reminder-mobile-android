package com.lenaralabs.cardsreminder.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.data.CardsRepository
import com.lenaralabs.cardsreminder.core.data.PaymentsRepository
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.BestForPurchase
import com.lenaralabs.cardsreminder.core.model.DashboardCardEntry
import com.lenaralabs.cardsreminder.core.model.DashboardSummary
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TimelineUiState(
    val headerDate: String = DateFormatUtils.formatTimelineHeaderDate(),
    val dashboardCards: List<DashboardCardEntry> = emptyList(),
    val summary: DashboardSummary? = null,
    val bestForPurchase: BestForPurchase? = null,
    val sections: List<TimelineSection> = emptyList(),
    val featuredEntry: DashboardCardEntry? = null,
    val feeling: DashboardFeeling? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasCachedDashboard: Boolean = false,
    val paymentsCard: ApiCard? = null,
    val showFeelingSheet: Boolean = false,
    val pendingQuickPayCard: ApiCard? = null,
    val markingPaidCardId: String? = null,
    val initialLoadComplete: Boolean = false,
) {
    val isInitialLoading: Boolean
        get() = isLoading && !initialLoadComplete

    val isPullRefreshing: Boolean
        get() = isLoading && initialLoadComplete

    val showEmptyState: Boolean
        get() = dashboardCards.isEmpty() && !isInitialLoading

    val showAllGood: Boolean
        get() = sections.isEmpty() && featuredEntry == null && dashboardCards.isNotEmpty()
}

class TimelineViewModel(
    private val paymentsRepository: PaymentsRepository,
    private val cardsRepository: CardsRepository,
) : ViewModel() {

    private val paymentsCard = MutableStateFlow<ApiCard?>(null)
    private val showFeelingSheet = MutableStateFlow(false)
    private val pendingQuickPayCard = MutableStateFlow<ApiCard?>(null)
    private val markingPaidCardId = MutableStateFlow<String?>(null)
    private val initialLoadComplete = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        refresh()
        viewModelScope.launch {
            combine(
                combine(
                    paymentsRepository.dashboardCards,
                    paymentsRepository.summary,
                    paymentsRepository.bestForPurchase,
                    paymentsRepository.isLoadingDashboard,
                    paymentsRepository.errorMessage,
                ) { cards, summary, best, loading, error ->
                    DashboardSnapshot(cards, summary, best, loading, error)
                },
                combine(
                    paymentsCard,
                    showFeelingSheet,
                    pendingQuickPayCard,
                    markingPaidCardId,
                    initialLoadComplete,
                ) { payCard, feeling, quickPay, markingId, loadComplete ->
                    UiSnapshot(payCard, feeling, quickPay, markingId, loadComplete)
                },
            ) { dashboard, ui ->
                buildUiState(dashboard, ui)
            }.collect { state ->
                _uiState.value = state
                if (!state.isLoading) {
                    initialLoadComplete.value = true
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            coroutineScope {
                val cardsJob = async { cardsRepository.fetchCards(silentUnlessEmpty = false) }
                val dashboardJob = async {
                    paymentsRepository.fetchDashboard(silentUnlessEmpty = false)
                }
                cardsJob.await()
                dashboardJob.await()
            }
        }
    }

    fun openPayments(card: ApiCard) {
        paymentsCard.value = card
    }

    fun closePayments() {
        paymentsCard.value = null
    }

    fun openFeelingSheet() {
        showFeelingSheet.value = true
    }

    fun closeFeelingSheet() {
        showFeelingSheet.value = false
    }

    fun requestQuickMarkPaid(card: ApiCard) {
        pendingQuickPayCard.value = card
    }

    fun dismissQuickMarkPaid() {
        pendingQuickPayCard.value = null
    }

    fun confirmQuickMarkPaid() {
        val card = pendingQuickPayCard.value ?: return
        pendingQuickPayCard.value = null
        markingPaidCardId.value = card.id
        viewModelScope.launch {
            paymentsRepository.markAsPaid(card.id)
                .onSuccess { response ->
                    cardsRepository.updateCardLocally(response.card)
                }
            markingPaidCardId.value = null
        }
    }

    private fun buildUiState(
        dashboard: DashboardSnapshot,
        ui: UiSnapshot,
    ): TimelineUiState {
        val buildResult = TimelineEventBuilder.build(
            entries = dashboard.cards,
            excludingCardId = dashboard.best?.cardId,
        )
        val featured = dashboard.best?.cardId?.let { id ->
            dashboard.cards.firstOrNull { it.card.id == id }
        }
        val feeling = dashboard.summary?.let { DashboardFeeling(it) }

        return TimelineUiState(
            headerDate = DateFormatUtils.formatTimelineHeaderDate(),
            dashboardCards = dashboard.cards,
            summary = dashboard.summary,
            bestForPurchase = dashboard.best,
            sections = buildResult.sections,
            featuredEntry = featured,
            feeling = feeling,
            isLoading = dashboard.loading,
            errorMessage = dashboard.error,
            hasCachedDashboard = dashboard.cards.isNotEmpty() || dashboard.summary != null,
            paymentsCard = ui.paymentsCard,
            showFeelingSheet = ui.showFeelingSheet,
            pendingQuickPayCard = ui.pendingQuickPay,
            markingPaidCardId = ui.markingPaidId,
            initialLoadComplete = ui.initialLoadComplete,
        )
    }

    private data class DashboardSnapshot(
        val cards: List<DashboardCardEntry>,
        val summary: DashboardSummary?,
        val best: BestForPurchase?,
        val loading: Boolean,
        val error: String?,
    )

    private data class UiSnapshot(
        val paymentsCard: ApiCard?,
        val showFeelingSheet: Boolean,
        val pendingQuickPay: ApiCard?,
        val markingPaidId: String?,
        val initialLoadComplete: Boolean,
    )

    class Factory(
        private val paymentsRepository: PaymentsRepository,
        private val cardsRepository: CardsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineViewModel(paymentsRepository, cardsRepository) as T
        }
    }
}
