package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.data.CardsRepository
import com.lenaralabs.cardsreminder.core.data.PaymentsRepository
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardCycle
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.ApiPayment
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardPaymentsBottomSheet(
    card: ApiCard,
    paymentsRepository: PaymentsRepository,
    cardsRepository: CardsRepository,
    onDismissRequest: () -> Unit,
    onMarkPaidSuccess: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val colors = MaterialTheme.cardsReminder

    var isLoading by remember { mutableStateOf(true) }
    var isMarkingPaid by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<ApiCardStatus?>(null) }
    var currentCycle by remember { mutableStateOf<ApiCardCycle?>(null) }
    var optimalDays by remember { mutableStateOf<List<String>>(emptyList()) }
    var payments by remember { mutableStateOf<List<ApiPayment>>(emptyList()) }
    var paymentNotes by remember { mutableStateOf("") }
    var showMarkPaidConfirmation by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    suspend fun loadData() {
        isLoading = true
        loadError = null
        coroutineScope {
            val cycleJob = async { paymentsRepository.fetchCurrentCycle(card.id) }
            val optimalJob = async { paymentsRepository.fetchOptimalPurchaseDays(card.id) }
            val paymentsJob = async { paymentsRepository.fetchPayments(card.id) }

            val cycleResponse = cycleJob.await()
            val optimalResponse = optimalJob.await()
            val paymentsResponse = paymentsJob.await()

            if (cycleResponse == null && optimalResponse == null && paymentsResponse == null) {
                loadError = "error"
            } else {
                cycleResponse?.let {
                    status = it.status
                    currentCycle = it.cycle
                    cardsRepository.updateCardLocally(it.card)
                }
                optimalResponse?.let {
                    optimalDays = it.optimalPurchaseDays
                    cardsRepository.updateCardLocally(it.card)
                }
                paymentsResponse?.let {
                    payments = it.payments
                    cardsRepository.updateCardLocally(it.card)
                }
            }
        }
        isLoading = false
    }

    LaunchedEffect(card.id) {
        loadData()
    }

    val showsDistinctCurrentCycle = remember(status, currentCycle) {
        val s = status
        val c = currentCycle
        if (s == null || c == null) false
        else !isSameCycle(s.cycleStart, s.cycleEnd, c.start, c.end)
    }

    if (showMarkPaidConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showMarkPaidConfirmation = false },
            title = { Text(stringResource(R.string.payments_quick_confirm_title)) },
            text = {
                Text(
                    if (status != null) {
                        stringResource(
                            R.string.payments_quick_confirm_message,
                            DateFormatUtils.formatDateRange(status!!.cycleStart, status!!.cycleEnd),
                        )
                    } else {
                        stringResource(R.string.payments_quick_confirm_message_fallback)
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMarkPaidConfirmation = false
                        isMarkingPaid = true
                        scope.launch {
                            paymentsRepository.markAsPaid(
                                cardId = card.id,
                                notes = paymentNotes.trim().ifEmpty { null },
                            ).onSuccess { response ->
                                cardsRepository.updateCardLocally(response.card)
                                isMarkingPaid = false
                                onMarkPaidSuccess()
                            }.onFailure { error ->
                                isMarkingPaid = false
                                loadError = error.localizedMessage
                            }
                        }
                    },
                ) {
                    Text(stringResource(R.string.payments_mark_paid))
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkPaidConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = colors.appBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.action_cancel))
                }
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Box(modifier = Modifier.size(48.dp))
            }

            if (isLoading && status == null) {
                AppInlineLoadingIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(48.dp),
                )
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    loadError?.let {
                        Text(
                            text = stringResource(R.string.error_invalid_response),
                            color = colors.redStateForeground,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    status?.let { cardStatus ->
                        PendingPaymentSection(
                            status = cardStatus,
                            showsDistinctCurrentCycle = showsDistinctCurrentCycle,
                            paymentNotes = paymentNotes,
                            onPaymentNotesChange = { paymentNotes = it },
                            isMarkingPaid = isMarkingPaid,
                            onMarkPaidClick = { showMarkPaidConfirmation = true },
                        )
                    }

                    if (showsDistinctCurrentCycle) {
                        currentCycle?.let { cycle ->
                            CurrentCycleSection(cycle = cycle)
                        }
                    }

                    if (optimalDays.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = stringResource(R.string.payments_optimal_days_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                if (status?.isOptimalPurchaseDay == true) {
                                    OptimalDayTag(
                                        label = stringResource(R.string.payments_optimal_today),
                                        highlighted = true,
                                    )
                                }
                                optimalDays.forEach { day ->
                                    OptimalDayTag(
                                        label = DateFormatUtils.formatShortDate(day),
                                        highlighted = false,
                                    )
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.payments_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (payments.isEmpty() && !isLoading) {
                            Text(
                                text = stringResource(R.string.payments_history_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.secondaryText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                            )
                        } else {
                            payments.forEach { payment ->
                                PaymentRow(payment = payment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingPaymentSection(
    status: ApiCardStatus,
    showsDistinctCurrentCycle: Boolean,
    paymentNotes: String,
    onPaymentNotesChange: (String) -> Unit,
    isMarkingPaid: Boolean,
    onMarkPaidClick: () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    val markPaidButtonColors = ButtonDefaults.buttonColors(
        containerColor = colors.primaryAction,
        contentColor = androidx.compose.ui.graphics.Color.White,
        disabledContainerColor = colors.primaryAction.copy(alpha = 0.6f),
        disabledContentColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.payments_pending_payment_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = DateFormatUtils.formatDateRange(status.cycleStart, status.cycleEnd),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                )
            }
            CardStatusBadge(status = status)
        }

        SheetItemCard {
            StatusDetailRow(
                label = stringResource(R.string.payments_due_date),
                value = DateFormatUtils.formatShortDate(status.paymentDueDate),
            )

            when {
                status.isPaidThisCycle -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = colors.emeraldStateForeground,
                        )
                        Text(
                            text = stringResource(R.string.payments_paid_this_cycle),
                            modifier = Modifier.padding(start = 8.dp),
                            color = colors.emeraldStateForeground,
                        )
                    }
                }

                status.daysOverdue > 0 -> {
                    Text(
                        text = stringResource(R.string.payments_days_overdue, status.daysOverdue),
                        color = colors.redStateForeground,
                    )
                }

                status.daysUntilPayment > 0 -> {
                    Text(
                        text = stringResource(R.string.payments_days_until, status.daysUntilPayment),
                        color = colors.secondaryText,
                    )
                }
            }

            if (!status.isPaidThisCycle) {
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.payments_mark_section_title),
                    fontWeight = FontWeight.SemiBold,
                )
                if (showsDistinctCurrentCycle) {
                    Text(
                        text = stringResource(
                            R.string.payments_mark_cycle_hint,
                            DateFormatUtils.formatDateRange(status.cycleStart, status.cycleEnd),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                    )
                }
                OutlinedTextField(
                    value = paymentNotes,
                    onValueChange = onPaymentNotesChange,
                    label = { Text(stringResource(R.string.payments_notes_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = onMarkPaidClick,
                        enabled = !isMarkingPaid,
                        colors = markPaidButtonColors,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isMarkingPaid) {
                            AppInlineLoadingIndicator(
                                size = 24.dp,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                        } else {
                            Text(stringResource(R.string.payments_mark_paid))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentCycleSection(cycle: ApiCardCycle) {
    val colors = MaterialTheme.cardsReminder

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            Text(
                text = stringResource(R.string.payments_current_cycle_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = DateFormatUtils.formatDateRange(cycle.start, cycle.end),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        }

        SheetItemCard {
            StatusDetailRow(
                label = stringResource(R.string.payments_due_date),
                value = DateFormatUtils.formatShortDate(cycle.paymentDue),
            )
            Text(
                text = stringResource(R.string.payments_current_cycle_footer),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        }
    }
}

@Composable
private fun SheetItemCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.sheetItemSurface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = { content() },
    )
}

@Composable
private fun OptimalDayTag(
    label: String,
    highlighted: Boolean,
) {
    val colors = MaterialTheme.cardsReminder
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (highlighted) {
            colors.primaryAction.copy(alpha = 0.12f)
        } else {
            colors.violetStateBackground
        },
        contentColor = if (highlighted) {
            colors.primaryAction
        } else {
            colors.violetStateForeground
        },
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

@Composable
private fun PaymentRow(payment: ApiPayment) {
    val colors = MaterialTheme.cardsReminder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.sheetItemSurface, RoundedCornerShape(10.dp))
            .padding(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = DateFormatUtils.formatShortDate(payment.cycleEnd),
                fontWeight = FontWeight.Medium,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            Text(
                text = DateFormatUtils.formatShortDateTime(payment.paidAt),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        }
        payment.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        }
    }
}

@Composable
private fun StatusDetailRow(label: String, value: String) {
    val colors = MaterialTheme.cardsReminder
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, color = colors.secondaryText)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
        Text(text = value, fontWeight = FontWeight.Medium)
    }
}

private fun isSameCycle(start: String, end: String, otherStart: String, otherEnd: String): Boolean {
    return DateFormatUtils.formatShortDate(start) == DateFormatUtils.formatShortDate(otherStart) &&
        DateFormatUtils.formatShortDate(end) == DateFormatUtils.formatShortDate(otherEnd)
}
