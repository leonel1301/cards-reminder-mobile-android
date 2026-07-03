package com.lenaralabs.cardsreminder.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.data.CardsRepository
import com.lenaralabs.cardsreminder.core.model.ApiCard
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalendarUiState(
    val year: Int,
    val month: Int,
    val selection: CalendarSelection? = null,
    val activeCards: List<ApiCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val initialLoadComplete: Boolean = false,
    val isPullRefreshing: Boolean = false,
    val daysInMonth: Int = CalendarBillingLogic.daysInMonth(year, month),
    val calendarDays: List<Int?> = CalendarBillingLogic.generateCalendarDays(year, month),
    val monthYearTitle: String = CalendarBillingLogic.formatMonthYear(year, month),
    val relevantPeriods: List<BillingPeriodInstance> = emptyList(),
    val visibleBillingPeriods: List<BillingPeriodInstance> = emptyList(),
    val visiblePayments: List<BillingPeriodInstance> = emptyList(),
    val periodsByCardId: Map<String, List<BillingPeriodInstance>> = emptyMap(),
) {
    val isInitialLoading: Boolean
        get() = !initialLoadComplete
}

private fun buildCalendarUiState(
    year: Int,
    month: Int,
    selection: CalendarSelection?,
    activeCards: List<ApiCard>,
    isLoading: Boolean,
    errorMessage: String?,
    initialLoadComplete: Boolean,
    isPullRefreshing: Boolean,
): CalendarUiState {
    val relevantPeriods = CalendarBillingLogic.periodsRelevantToMonth(activeCards, year, month)
    return CalendarUiState(
        year = year,
        month = month,
        selection = selection,
        activeCards = activeCards,
        isLoading = isLoading,
        errorMessage = errorMessage,
        initialLoadComplete = initialLoadComplete,
        isPullRefreshing = isPullRefreshing,
        daysInMonth = CalendarBillingLogic.daysInMonth(year, month),
        calendarDays = CalendarBillingLogic.generateCalendarDays(year, month),
        monthYearTitle = CalendarBillingLogic.formatMonthYear(year, month),
        relevantPeriods = relevantPeriods,
        visibleBillingPeriods = CalendarBillingLogic.billingPeriodsVisibleInMonth(relevantPeriods, year, month),
        visiblePayments = CalendarBillingLogic.paymentsInMonth(relevantPeriods, year, month),
        periodsByCardId = relevantPeriods.groupBy { it.cardId },
    )
}

class CalendarViewModel(
    private val cardsRepository: CardsRepository,
) : ViewModel() {

    private val today = Calendar.getInstance()
    private val initialYear = today.get(Calendar.YEAR)
    private val initialMonth = today.get(Calendar.MONTH) + 1

    private val displayedMonth = MutableStateFlow(initialYear to initialMonth)
    private val selection = MutableStateFlow<CalendarSelection?>(null)
    private val initialLoadComplete = MutableStateFlow(false)
    private val isPullRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<CalendarUiState> = combine(
        combine(
            displayedMonth,
            selection,
            cardsRepository.cards,
            cardsRepository.isLoading,
            cardsRepository.errorMessage,
        ) { monthPair, currentSelection, cards, isLoading, errorMessage ->
            CalendarDataSnapshot(monthPair, currentSelection, cards, isLoading, errorMessage)
        },
        initialLoadComplete,
        isPullRefreshing,
    ) { data, loadComplete, pullRefreshing ->
        buildCalendarUiState(
            year = data.monthPair.first,
            month = data.monthPair.second,
            selection = data.selection,
            activeCards = data.cards.filter { it.isActive },
            isLoading = data.isLoading,
            errorMessage = data.errorMessage,
            initialLoadComplete = loadComplete,
            isPullRefreshing = pullRefreshing,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(year = initialYear, month = initialMonth),
    )

    init {
        viewModelScope.launch {
            if (cardsRepository.cards.value.isEmpty()) {
                cardsRepository.fetchCards(silentUnlessEmpty = false)
            }
            initialLoadComplete.value = true
        }
    }

    fun refresh() {
        viewModelScope.launch {
            isPullRefreshing.value = true
            try {
                cardsRepository.fetchCards(silentUnlessEmpty = false)
            } finally {
                isPullRefreshing.value = false
            }
        }
    }

    fun changeMonth(delta: Int) {
        val (year, month) = displayedMonth.value
        displayedMonth.value = CalendarBillingLogic.addMonths(year, month, delta)
        selection.value = null
    }

    fun onSelectionChange(newSelection: CalendarSelection?) {
        selection.value = newSelection
    }

    fun barDisplaysByDay(state: CalendarUiState): Map<Int, List<CardBarDisplay>> {
        return (1..state.daysInMonth).associateWith { day ->
            barDisplaysForDay(day, state)
        }
    }

    fun barDisplaysForDay(day: Int, state: CalendarUiState): List<CardBarDisplay> {
        return state.activeCards.map { card ->
            val cardPeriods = state.periodsByCardId[card.id].orEmpty()
            val activePeriod = cardPeriods.firstOrNull { period ->
                CalendarBillingLogic.dayInPeriod(period, state.year, state.month, day)
            }
            val paymentPeriod = cardPeriods.firstOrNull { period ->
                CalendarBillingLogic.isPaymentDay(period, state.year, state.month, day)
            }

            val barPeriodId = activePeriod?.id
            val paymentPeriodId = paymentPeriod?.id

            CardBarDisplay(
                cardId = card.id,
                color = card.color,
                showBar = activePeriod != null,
                isPeriodStart = activePeriod?.let {
                    CalendarBillingLogic.isPeriodSegmentStart(it, state.year, state.month, day)
                } ?: false,
                isPeriodEnd = activePeriod?.let {
                    CalendarBillingLogic.isPeriodSegmentEnd(
                        it,
                        state.year,
                        state.month,
                        day,
                        state.daysInMonth,
                    )
                } ?: false,
                showPaymentPin = paymentPeriod != null,
                barHighlighted = isBarHighlighted(barPeriodId, card.id, state.selection),
                pinHighlighted = isPinHighlighted(paymentPeriodId, card.id, state.selection),
                isDimmed = isDimmed(
                    barPeriodId = barPeriodId,
                    paymentPeriodId = paymentPeriodId,
                    cardId = card.id,
                    showBar = activePeriod != null,
                    showPin = paymentPeriod != null,
                    selection = state.selection,
                ),
            )
        }
    }

    fun isToday(day: Int, state: CalendarUiState): Boolean {
        return today.get(Calendar.YEAR) == state.year &&
            today.get(Calendar.MONTH) + 1 == state.month &&
            today.get(Calendar.DAY_OF_MONTH) == day
    }

    private fun isBarHighlighted(
        periodId: String?,
        cardId: String,
        selection: CalendarSelection?,
    ): Boolean {
        return when (val current = selection) {
            is CalendarSelection.Card -> current.cardId == cardId
            is CalendarSelection.BillingPeriod -> current.periodId == periodId
            else -> false
        }
    }

    private fun isPinHighlighted(
        periodId: String?,
        cardId: String,
        selection: CalendarSelection?,
    ): Boolean {
        return when (val current = selection) {
            is CalendarSelection.Card -> current.cardId == cardId
            is CalendarSelection.Payment -> current.periodId == periodId
            else -> false
        }
    }

    private fun isDimmed(
        barPeriodId: String?,
        paymentPeriodId: String?,
        cardId: String,
        showBar: Boolean,
        showPin: Boolean,
        selection: CalendarSelection?,
    ): Boolean {
        if (selection == null) return false

        return when (selection) {
            is CalendarSelection.Card -> selection.cardId != cardId && (showBar || showPin)
            is CalendarSelection.BillingPeriod -> {
                when {
                    showBar -> barPeriodId != selection.periodId
                    showPin -> true
                    else -> false
                }
            }

            is CalendarSelection.Payment -> {
                when {
                    showPin -> paymentPeriodId != selection.periodId
                    showBar -> true
                    else -> false
                }
            }
        }
    }

    class Factory(
        private val cardsRepository: CardsRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(cardsRepository) as T
        }
    }

    private data class CalendarDataSnapshot(
        val monthPair: Pair<Int, Int>,
        val selection: CalendarSelection?,
        val cards: List<ApiCard>,
        val isLoading: Boolean,
        val errorMessage: String?,
    )
}
