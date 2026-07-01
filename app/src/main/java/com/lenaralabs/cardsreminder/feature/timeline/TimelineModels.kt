package com.lenaralabs.cardsreminder.feature.timeline

import androidx.annotation.StringRes
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus

enum class TimelineEventKind {
    Overdue,
    PaymentDueToday,
    Urgent,
    DueSoon,
    OptimalToday,
    CycleEndsToday,
    Paid,
    OnTrack,
    ;

    @get:StringRes
    val titleRes: Int
        get() = when (this) {
            Overdue -> R.string.timeline_event_overdue_title
            PaymentDueToday -> R.string.timeline_event_payment_today_title
            Urgent -> R.string.timeline_event_urgent_title
            DueSoon -> R.string.timeline_event_due_soon_title
            OptimalToday -> R.string.timeline_event_optimal_title
            CycleEndsToday -> R.string.timeline_event_cycle_end_title
            Paid -> R.string.timeline_event_paid_title
            OnTrack -> R.string.timeline_event_on_track_title
        }
}

data class TimelineEvent(
    val id: String,
    val card: ApiCard,
    val status: ApiCardStatus,
    val kind: TimelineEventKind,
    val sortOrder: Int,
)

data class TimelineSection(
    val id: String,
    @StringRes val titleRes: Int,
    val events: List<TimelineEvent>,
)

data class TimelineBuildResult(
    val sections: List<TimelineSection>,
)

fun TimelineEvent.canMarkPaid(): Boolean {
    return card.isActive &&
        !status.isPaidThisCycle &&
        (
            kind == TimelineEventKind.Overdue ||
                kind == TimelineEventKind.PaymentDueToday ||
                kind == TimelineEventKind.Urgent ||
                kind == TimelineEventKind.DueSoon
            )
}
