package com.lenaralabs.cardsreminder.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lenaralabs.cardsreminder.core.data.CardsRepository
import com.lenaralabs.cardsreminder.core.data.OwnersRepository
import com.lenaralabs.cardsreminder.core.data.PaymentsRepository
import com.lenaralabs.cardsreminder.core.notifications.PushNotificationManager
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.ApiOwner
import com.lenaralabs.cardsreminder.core.model.CreateCardRequest
import com.lenaralabs.cardsreminder.core.model.DashboardSummary
import com.lenaralabs.cardsreminder.core.model.UpdateCardRequest
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CardsSheet {
    data object None : CardsSheet()
    data object Create : CardsSheet()
    data class Edit(val card: ApiCard) : CardsSheet()
    data class Payments(val card: ApiCard) : CardsSheet()
}

data class CardFormState(
    val name: String = "",
    val lastFourDigits: String = "",
    val issuer: String = "",
    val billingCycleDay: Int = 1,
    val paymentDueDay: Int = 1,
    val selectedColorHex: String = CardPaletteOption.DEFAULT_HEX,
    val notes: String = "",
    val isActive: Boolean = true,
    val selectedOwnerId: String? = null,
    val isDirty: Boolean = false,
) {
    val periodStartPreview: Int
        get() = if (billingCycleDay >= 31) 1 else billingCycleDay + 1

    val canSave: Boolean
        get() = name.trim().isNotEmpty() &&
            (lastFourDigits.isEmpty() || lastFourDigits.length == 4)

    val resolvedLastFourDigits: String
        get() = lastFourDigits.ifEmpty { "0000" }

    val showLastFourValidation: Boolean
        get() = lastFourDigits.isNotEmpty() && lastFourDigits.length < 4
}

data class CardsUiState(
    val cards: List<ApiCard> = emptyList(),
    val dashboardSummary: DashboardSummary? = null,
    val cardStatuses: Map<String, ApiCardStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val activeSheet: CardsSheet = CardsSheet.None,
    val expandedMenuCardId: String? = null,
    val pendingDeleteCard: ApiCard? = null,
    val pendingQuickPayCard: ApiCard? = null,
    val deletingCardId: String? = null,
    val markingPaidCardId: String? = null,
    val formState: CardFormState = CardFormState(),
    val showDiscardFormDialog: Boolean = false,
    val showRemindersPrompt: Boolean = false,
    val showRemindersLaterInfo: Boolean = false,
    val showLastFourHelp: Boolean = false,
    val showBillingDayHelp: Boolean = false,
    val showPaymentDayHelp: Boolean = false,
    val initialLoadComplete: Boolean = false,
    val isPullRefreshing: Boolean = false,
) {
    val isInitialLoading: Boolean
        get() = !initialLoadComplete && isSaving
}

class CardsViewModel(
    private val cardsRepository: CardsRepository,
    private val paymentsRepository: PaymentsRepository,
    private val ownersRepository: OwnersRepository,
    private val pushNotificationManager: PushNotificationManager,
) : ViewModel() {

    private val expandedMenuCardId = MutableStateFlow<String?>(null)
    private val activeSheet = MutableStateFlow<CardsSheet>(CardsSheet.None)
    private val pendingDeleteCard = MutableStateFlow<ApiCard?>(null)
    private val pendingQuickPayCard = MutableStateFlow<ApiCard?>(null)
    private val deletingCardId = MutableStateFlow<String?>(null)
    private val markingPaidCardId = MutableStateFlow<String?>(null)
    private val formState = MutableStateFlow(CardFormState())
    private val showDiscardFormDialog = MutableStateFlow(false)
    private val showRemindersPrompt = MutableStateFlow(false)
    private val showRemindersLaterInfo = MutableStateFlow(false)
    private val showLastFourHelp = MutableStateFlow(false)
    private val showBillingDayHelp = MutableStateFlow(false)
    private val showPaymentDayHelp = MutableStateFlow(false)
    private val initialLoadComplete = MutableStateFlow(false)
    private val isPullRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow(CardsUiState())
    val uiState: StateFlow<CardsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadInitialData()
            initialLoadComplete.value = true
        }
        viewModelScope.launch {
            combine(
                combine(
                    cardsRepository.cards,
                    cardsRepository.isLoading,
                    cardsRepository.errorMessage,
                    paymentsRepository.summary,
                    paymentsRepository.statusByCardId,
                ) { cards, cardsLoading, cardsError, summary, statuses ->
                    DataSnapshot(cards, cardsLoading, cardsError, summary, statuses)
                },
                combine(
                    paymentsRepository.errorMessage,
                    paymentsRepository.isLoading,
                    expandedMenuCardId,
                    activeSheet,
                    pendingDeleteCard,
                ) { paymentsError, paymentsLoading, menuId, sheet, deleteCard ->
                    UiSnapshot1(paymentsError, paymentsLoading, menuId, sheet, deleteCard)
                },
                combine(
                    pendingQuickPayCard,
                    deletingCardId,
                    markingPaidCardId,
                    formState,
                    showDiscardFormDialog,
                ) { quickPayCard, deletingId, markingId, form, discardDialog ->
                    UiSnapshot2(quickPayCard, deletingId, markingId, form, discardDialog)
                },
                combine(
                    combine(
                        showRemindersPrompt,
                        showRemindersLaterInfo,
                        showLastFourHelp,
                    ) { remindersPrompt, remindersLater, lastFourHelp ->
                        Triple(remindersPrompt, remindersLater, lastFourHelp)
                    },
                    combine(
                        showBillingDayHelp,
                        showPaymentDayHelp,
                        initialLoadComplete,
                    ) { billingHelp, paymentHelp, loadComplete ->
                        Triple(billingHelp, paymentHelp, loadComplete)
                    },
                ) { helpFlags, loadFlags ->
                    UiSnapshot3(
                        remindersPrompt = helpFlags.first,
                        remindersLater = helpFlags.second,
                        lastFourHelp = helpFlags.third,
                        billingDayHelp = loadFlags.first,
                        paymentDayHelp = loadFlags.second,
                        initialLoadComplete = loadFlags.third,
                    )
                },
                isPullRefreshing,
            ) { data, ui1, ui2, ui3, pullRefreshing ->
                buildUiState(
                    cards = data.cards,
                    cardsLoading = data.cardsLoading,
                    cardsError = data.cardsError,
                    summary = data.summary,
                    statuses = data.statuses,
                    paymentsError = ui1.paymentsError,
                    paymentsLoading = ui1.paymentsLoading,
                    menuId = ui1.menuId,
                    sheet = ui1.sheet,
                    deleteCard = ui1.deleteCard,
                    quickPayCard = ui2.quickPayCard,
                    deletingId = ui2.deletingId,
                    markingId = ui2.markingId,
                    form = ui2.form,
                    discardDialog = ui2.discardDialog,
                    remindersPrompt = ui3.remindersPrompt,
                    remindersLater = ui3.remindersLater,
                    lastFourHelp = ui3.lastFourHelp,
                    billingDayHelp = ui3.billingDayHelp,
                    paymentDayHelp = ui3.paymentDayHelp,
                    initialLoadComplete = ui3.initialLoadComplete,
                    isPullRefreshing = pullRefreshing,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private suspend fun loadInitialData() {
        coroutineScope {
            val cardsJob = async {
                if (cardsRepository.cards.value.isEmpty()) {
                    cardsRepository.fetchCards(silentUnlessEmpty = false)
                }
            }
            val dashboardJob = async {
                if (!paymentsRepository.hasCachedDashboard) {
                    paymentsRepository.fetchDashboard(silentUnlessEmpty = false)
                }
            }
            cardsJob.await()
            dashboardJob.await()
        }
    }

    private data class DataSnapshot(
        val cards: List<ApiCard>,
        val cardsLoading: Boolean,
        val cardsError: String?,
        val summary: DashboardSummary?,
        val statuses: Map<String, ApiCardStatus>,
    )

    private data class UiSnapshot1(
        val paymentsError: String?,
        val paymentsLoading: Boolean,
        val menuId: String?,
        val sheet: CardsSheet,
        val deleteCard: ApiCard?,
    )

    private data class UiSnapshot2(
        val quickPayCard: ApiCard?,
        val deletingId: String?,
        val markingId: String?,
        val form: CardFormState,
        val discardDialog: Boolean,
    )

    private data class UiSnapshot3(
        val remindersPrompt: Boolean,
        val remindersLater: Boolean,
        val lastFourHelp: Boolean,
        val billingDayHelp: Boolean,
        val paymentDayHelp: Boolean,
        val initialLoadComplete: Boolean,
    )

    private fun buildUiState(
        cards: List<ApiCard>,
        cardsLoading: Boolean,
        cardsError: String?,
        summary: DashboardSummary?,
        statuses: Map<String, ApiCardStatus>,
        paymentsError: String?,
        paymentsLoading: Boolean,
        menuId: String?,
        sheet: CardsSheet,
        deleteCard: ApiCard?,
        quickPayCard: ApiCard?,
        deletingId: String?,
        markingId: String?,
        form: CardFormState,
        discardDialog: Boolean,
        remindersPrompt: Boolean,
        remindersLater: Boolean,
        lastFourHelp: Boolean,
        billingDayHelp: Boolean,
        paymentDayHelp: Boolean,
        initialLoadComplete: Boolean,
        isPullRefreshing: Boolean,
    ): CardsUiState {
        val visibleError = when {
            cardsError != null && cards.isEmpty() -> cardsError
            paymentsError != null && summary == null && statuses.isEmpty() -> paymentsError
            else -> null
        }

        return CardsUiState(
            cards = cards,
            dashboardSummary = summary,
            cardStatuses = statuses,
            isLoading = cardsLoading && cards.isEmpty(),
            isSaving = cardsLoading || paymentsLoading,
            errorMessage = visibleError,
            activeSheet = sheet,
            expandedMenuCardId = menuId,
            pendingDeleteCard = deleteCard,
            pendingQuickPayCard = quickPayCard,
            deletingCardId = deletingId,
            markingPaidCardId = markingId,
            formState = form,
            showDiscardFormDialog = discardDialog,
            showRemindersPrompt = remindersPrompt,
            showRemindersLaterInfo = remindersLater,
            showLastFourHelp = lastFourHelp,
            showBillingDayHelp = billingDayHelp,
            showPaymentDayHelp = paymentDayHelp,
            initialLoadComplete = initialLoadComplete,
            isPullRefreshing = isPullRefreshing,
        )
    }

    val owners = ownersRepository.owners

    fun refresh() {
        viewModelScope.launch {
            isPullRefreshing.value = true
            try {
                coroutineScope {
                    val cardsJob = async { cardsRepository.fetchCards(silentUnlessEmpty = false) }
                    val dashboardJob = async { paymentsRepository.fetchDashboard(silentUnlessEmpty = false) }
                    cardsJob.await()
                    dashboardJob.await()
                }
            } finally {
                isPullRefreshing.value = false
            }
        }
    }

    fun setMenuExpanded(cardId: String?, expanded: Boolean) {
        expandedMenuCardId.value = if (expanded) cardId else null
    }

    fun openCreate() {
        formState.value = CardFormState(
            selectedOwnerId = ownersRepository.selfOwner?.id,
        )
        activeSheet.value = CardsSheet.Create
        viewModelScope.launch { ownersRepository.fetchOwners() }
    }

    fun openEdit(card: ApiCard) {
        formState.value = CardFormState(
            name = card.name,
            lastFourDigits = if (card.lastFourDigits == "0000") "" else card.lastFourDigits,
            issuer = card.issuer.orEmpty(),
            billingCycleDay = card.billingCycleDay,
            paymentDueDay = card.paymentDueDay,
            selectedColorHex = CardPaletteOption.matching(card.colorHex),
            notes = card.notes.orEmpty(),
            isActive = card.isActive,
            selectedOwnerId = card.ownerId,
        )
        activeSheet.value = CardsSheet.Edit(card)
        viewModelScope.launch { ownersRepository.fetchOwners() }
    }

    fun openPayments(card: ApiCard) {
        activeSheet.value = CardsSheet.Payments(card)
    }

    fun requestDismissSheet(): Boolean {
        if (formState.value.isDirty &&
            (activeSheet.value is CardsSheet.Create || activeSheet.value is CardsSheet.Edit)
        ) {
            showDiscardFormDialog.value = true
            return false
        }
        return true
    }

    fun onFormSheetDismissed() {
        dismissSheet()
    }

    fun confirmDiscardForm() {
        showDiscardFormDialog.value = false
        dismissSheet()
    }

    fun dismissDiscardDialog() {
        showDiscardFormDialog.value = false
    }

    fun dismissSheet() {
        activeSheet.value = CardsSheet.None
    }

    fun closeSheetCompletely() {
        activeSheet.value = CardsSheet.None
    }

    fun updateForm(transform: (CardFormState) -> CardFormState) {
        formState.update { current ->
            transform(current.copy(isDirty = true))
        }
    }

    fun sanitizeLastFour(input: String) {
        val sanitized = input.filter { it.isDigit() }.take(4)
        updateForm { it.copy(lastFourDigits = sanitized) }
    }

    fun requestDelete(card: ApiCard) {
        pendingDeleteCard.value = card
    }

    fun dismissDeleteDialog() {
        pendingDeleteCard.value = null
    }

    fun confirmDelete() {
        val card = pendingDeleteCard.value ?: return
        pendingDeleteCard.value = null
        viewModelScope.launch {
            deletingCardId.value = card.id
            delay(220)
            cardsRepository.deleteCard(card.id)
                .onSuccess {
                    deletingCardId.value = null
                    paymentsRepository.fetchDashboard(silentUnlessEmpty = false)
                    if (activeSheet.value is CardsSheet.Edit) {
                        closeSheetCompletely()
                    }
                }
                .onFailure {
                    deletingCardId.value = null
                }
        }
    }

    fun requestQuickPay(card: ApiCard) {
        pendingQuickPayCard.value = card
    }

    fun dismissQuickPayDialog() {
        pendingQuickPayCard.value = null
    }

    fun quickPayConfirmationMessage(card: ApiCard): String {
        val status = paymentsRepository.statusFor(card.id) ?: return ""
        val period = DateFormatUtils.formatDateRange(status.cycleStart, status.cycleEnd)
        return period
    }

    fun confirmQuickPay() {
        val card = pendingQuickPayCard.value ?: return
        pendingQuickPayCard.value = null
        viewModelScope.launch {
            markingPaidCardId.value = card.id
            paymentsRepository.markAsPaid(card.id)
                .onSuccess { response ->
                    cardsRepository.updateCardLocally(response.card)
                }
            markingPaidCardId.value = null
        }
    }

    fun saveForm() {
        val form = formState.value
        if (!form.canSave) return

        val trimmedName = form.name.trim()
        val trimmedIssuer = form.issuer.trim()
        val trimmedNotes = form.notes.trim()
        val colorHex = CardPaletteOption.normalize(form.selectedColorHex)

        viewModelScope.launch {
            when (val sheet = activeSheet.value) {
                is CardsSheet.Create -> {
                    val request = CreateCardRequest(
                        name = trimmedName,
                        lastFourDigits = form.resolvedLastFourDigits,
                        issuer = trimmedIssuer.ifEmpty { null },
                        billingCycleDay = form.billingCycleDay,
                        paymentDueDay = form.paymentDueDay,
                        colorHex = colorHex,
                        notes = trimmedNotes.ifEmpty { null },
                        ownerId = form.selectedOwnerId,
                    )
                    cardsRepository.createCard(request)
                        .onSuccess {
                            paymentsRepository.fetchDashboard(silentUnlessEmpty = false)
                            formState.value = CardFormState()
                            closeSheetCompletely()
                            if (!pushNotificationManager.refreshAndCheckFullyEnabled()) {
                                showRemindersPrompt.value = true
                            }
                        }
                }

                is CardsSheet.Edit -> {
                    val request = UpdateCardRequest(
                        name = trimmedName,
                        lastFourDigits = form.resolvedLastFourDigits,
                        issuer = trimmedIssuer.ifEmpty { null },
                        billingCycleDay = form.billingCycleDay,
                        paymentDueDay = form.paymentDueDay,
                        colorHex = colorHex,
                        notes = trimmedNotes.ifEmpty { null },
                        isActive = form.isActive,
                        ownerId = form.selectedOwnerId ?: sheet.card.ownerId,
                    )
                    cardsRepository.updateCard(sheet.card.id, request)
                        .onSuccess {
                            paymentsRepository.fetchDashboard(silentUnlessEmpty = false)
                            closeSheetCompletely()
                        }
                }

                else -> Unit
            }
        }
    }

    fun deleteFromForm() {
        val sheet = activeSheet.value
        if (sheet is CardsSheet.Edit) {
            requestDelete(sheet.card)
        }
    }

    fun dismissRemindersPrompt() {
        showRemindersPrompt.value = false
        showRemindersLaterInfo.value = true
    }

    fun dismissRemindersLaterInfo() {
        showRemindersLaterInfo.value = false
    }

    fun completeCreateAfterReminders() {
        showRemindersPrompt.value = false
    }

    fun enableRemindersFromPrompt() {
        viewModelScope.launch {
            pushNotificationManager.applyNotificationsPreference(enabled = true)
            completeCreateAfterReminders()
        }
    }

    fun showLastFourHelp() {
        showLastFourHelp.value = true
    }

    fun dismissLastFourHelp() {
        showLastFourHelp.value = false
    }

    fun showBillingDayHelp() {
        showBillingDayHelp.value = true
    }

    fun dismissBillingDayHelp() {
        showBillingDayHelp.value = false
    }

    fun showPaymentDayHelp() {
        showPaymentDayHelp.value = true
    }

    fun dismissPaymentDayHelp() {
        showPaymentDayHelp.value = false
    }

    fun ownerDisplayName(owner: ApiOwner, selfFormat: String): String {
        return ownersRepository.ownerDisplayName(owner, selfFormat)
    }

    class Factory(
        private val cardsRepository: CardsRepository,
        private val paymentsRepository: PaymentsRepository,
        private val ownersRepository: OwnersRepository,
        private val pushNotificationManager: PushNotificationManager,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardsViewModel(
                cardsRepository = cardsRepository,
                paymentsRepository = paymentsRepository,
                ownersRepository = ownersRepository,
                pushNotificationManager = pushNotificationManager,
            ) as T
        }
    }
}
