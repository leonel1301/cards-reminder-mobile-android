package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiPayment(
    val id: String,
    @SerialName("card_id") val cardId: String,
    @SerialName("cycle_end") val cycleEnd: String,
    @SerialName("paid_at") val paidAt: String,
    val notes: String? = null,
)

@Serializable
data class ApiCardStatus(
    val status: String,
    @SerialName("cycle_start") val cycleStart: String,
    @SerialName("cycle_end") val cycleEnd: String,
    @SerialName("payment_due_date") val paymentDueDate: String,
    @SerialName("days_until_payment") val daysUntilPayment: Int,
    @SerialName("days_overdue") val daysOverdue: Int,
    @SerialName("optimal_purchase_day") val optimalPurchaseDay: Int,
    @SerialName("is_optimal_purchase_day") val isOptimalPurchaseDay: Boolean,
    @SerialName("is_paid_this_cycle") val isPaidThisCycle: Boolean,
) {
    val kind: CardPaymentStatusKind
        get() = CardPaymentStatusKind.fromRaw(status)
}

enum class CardPaymentStatusKind(val rawValue: String) {
    Paid("paid"),
    Overdue("overdue"),
    Urgent("urgent"),
    DueSoon("due_soon"),
    OptimalDay("optimal_day"),
    OnTrack("on_track"),
    ;

    companion object {
        fun fromRaw(raw: String): CardPaymentStatusKind {
            return entries.firstOrNull { it.rawValue == raw } ?: OnTrack
        }
    }
}

@Serializable
data class ApiCardCycle(
    val start: String,
    val end: String,
    @SerialName("payment_due") val paymentDue: String,
)

@Serializable
data class CardPaymentsResponse(
    val card: ApiCard,
    val payments: List<ApiPayment>,
)

@Serializable
data class MarkPaidRequest(
    val notes: String? = null,
)

@Serializable
data class MarkPaidResponse(
    val card: ApiCard,
    val status: ApiCardStatus,
    @SerialName("optimal_purchase_days") val optimalPurchaseDays: List<String>,
)

@Serializable
data class CurrentCycleResponse(
    val card: ApiCard,
    val cycle: ApiCardCycle,
    val status: ApiCardStatus,
)

@Serializable
data class OptimalPurchaseDaysResponse(
    val card: ApiCard,
    val cycle: ApiCardCycle,
    @SerialName("optimal_purchase_days") val optimalPurchaseDays: List<String>,
)

@Serializable
data class DashboardCardEntry(
    val card: ApiCard,
    val status: ApiCardStatus,
)

@Serializable
data class DashboardSummary(
    val total: Int,
    val overdue: Int,
    val urgent: Int,
    @SerialName("due_soon") val dueSoon: Int,
    val paid: Int,
    @SerialName("optimal_day") val optimalDay: Int,
    @SerialName("on_track") val onTrack: Int,
) {
    val hasAttentionItems: Boolean
        get() = overdue > 0 || urgent > 0 || dueSoon > 0
}

@Serializable
data class DashboardResponse(
    val cards: List<DashboardCardEntry>,
    val summary: DashboardSummary,
    @SerialName("best_for_purchase") val bestForPurchase: BestForPurchase? = null,
)

@Serializable
data class BestForPurchase(
    @SerialName("card_id") val cardId: String,
    val why: String,
)
