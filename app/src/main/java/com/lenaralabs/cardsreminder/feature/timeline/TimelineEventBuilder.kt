package com.lenaralabs.cardsreminder.feature.timeline

import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.CardPaymentStatusKind
import com.lenaralabs.cardsreminder.core.model.DashboardCardEntry
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils

object TimelineEventBuilder {
    fun build(
        entries: List<DashboardCardEntry>,
        excludingCardId: String? = null,
    ): TimelineBuildResult {
        val events = entries
            .filter { it.card.isActive }
            .filter { entry -> excludingCardId == null || entry.card.id != excludingCardId }
            .map { makeEvent(it) }
            .sortedBy { it.sortOrder }

        val attention = events.filter {
            when (it.kind) {
                TimelineEventKind.Overdue,
                TimelineEventKind.PaymentDueToday,
                TimelineEventKind.Urgent,
                TimelineEventKind.DueSoon,
                -> true
                else -> false
            }
        }

        val recommended = events.filter {
            when (it.kind) {
                TimelineEventKind.OptimalToday,
                TimelineEventKind.CycleEndsToday,
                -> true
                else -> false
            }
        }

        val allClear = events.filter {
            when (it.kind) {
                TimelineEventKind.Paid,
                TimelineEventKind.OnTrack,
                -> true
                else -> false
            }
        }

        val sections = buildList {
            if (attention.isNotEmpty()) {
                add(
                    TimelineSection(
                        id = "attention",
                        titleRes = R.string.timeline_section_attention,
                        events = attention,
                    ),
                )
            }
            if (recommended.isNotEmpty()) {
                add(
                    TimelineSection(
                        id = "recommended",
                        titleRes = R.string.timeline_section_recommended,
                        events = recommended,
                    ),
                )
            }
            if (allClear.isNotEmpty()) {
                add(
                    TimelineSection(
                        id = "all_clear",
                        titleRes = R.string.timeline_section_all_clear,
                        events = allClear,
                    ),
                )
            }
        }

        return TimelineBuildResult(sections = sections)
    }

    private fun makeEvent(entry: DashboardCardEntry): TimelineEvent {
        val kind = resolveKind(entry.status)
        return TimelineEvent(
            id = "${entry.card.id}-${kind.name}",
            card = entry.card,
            status = entry.status,
            kind = kind,
            sortOrder = sortOrder(kind, entry.status),
        )
    }

    private fun resolveKind(status: ApiCardStatus): TimelineEventKind {
        if (status.isPaidThisCycle || status.kind == CardPaymentStatusKind.Paid) {
            return TimelineEventKind.Paid
        }
        if (status.daysOverdue > 0 || status.kind == CardPaymentStatusKind.Overdue) {
            return TimelineEventKind.Overdue
        }
        if (status.daysUntilPayment == 0) {
            return TimelineEventKind.PaymentDueToday
        }
        if (status.kind == CardPaymentStatusKind.Urgent) {
            return TimelineEventKind.Urgent
        }
        if (status.kind == CardPaymentStatusKind.DueSoon) {
            return TimelineEventKind.DueSoon
        }
        if (status.isOptimalPurchaseDay || status.kind == CardPaymentStatusKind.OptimalDay) {
            return TimelineEventKind.OptimalToday
        }
        if (DateFormatUtils.isToday(status.cycleEnd)) {
            return TimelineEventKind.CycleEndsToday
        }
        return TimelineEventKind.OnTrack
    }

    private fun sortOrder(kind: TimelineEventKind, status: ApiCardStatus): Int {
        return when (kind) {
            TimelineEventKind.Overdue -> 0 - status.daysOverdue
            TimelineEventKind.PaymentDueToday -> 1_000
            TimelineEventKind.Urgent -> 2_000 + status.daysUntilPayment
            TimelineEventKind.DueSoon -> 3_000 + status.daysUntilPayment
            TimelineEventKind.OptimalToday -> 4_000 - status.daysUntilPayment
            TimelineEventKind.CycleEndsToday -> 5_000
            TimelineEventKind.Paid -> 8_000
            TimelineEventKind.OnTrack -> 9_000 + status.daysUntilPayment
        }
    }
}
