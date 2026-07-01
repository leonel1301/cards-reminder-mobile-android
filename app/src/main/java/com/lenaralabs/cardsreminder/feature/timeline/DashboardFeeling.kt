package com.lenaralabs.cardsreminder.feature.timeline

import androidx.annotation.StringRes
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.DashboardSummary

enum class DashboardFeelingKind {
    RedZone,
    CrunchTime,
    Countdown,
    PrimeWindow,
    Cruising,
    ClearBooks,
    BlankSlate,
}

data class DashboardFeeling(
    val kind: DashboardFeelingKind,
    val summary: DashboardSummary,
) {
    constructor(summary: DashboardSummary) : this(
        kind = resolveKind(summary),
        summary = summary,
    )

    @get:StringRes
    val wordRes: Int
        get() = when (kind) {
            DashboardFeelingKind.RedZone -> R.string.finance_feeling_red_zone
            DashboardFeelingKind.CrunchTime -> R.string.finance_feeling_crunch
            DashboardFeelingKind.Countdown -> R.string.finance_feeling_countdown
            DashboardFeelingKind.PrimeWindow -> R.string.finance_feeling_prime
            DashboardFeelingKind.Cruising -> R.string.finance_feeling_cruising
            DashboardFeelingKind.ClearBooks -> R.string.finance_feeling_clear
            DashboardFeelingKind.BlankSlate -> R.string.finance_feeling_blank
        }

    @get:StringRes
    val headlineRes: Int
        get() = when (kind) {
            DashboardFeelingKind.RedZone -> R.string.finance_feeling_headline_red_zone
            DashboardFeelingKind.CrunchTime -> R.string.finance_feeling_headline_crunch
            DashboardFeelingKind.Countdown -> R.string.finance_feeling_headline_countdown
            DashboardFeelingKind.PrimeWindow -> R.string.finance_feeling_headline_prime
            DashboardFeelingKind.Cruising -> R.string.finance_feeling_headline_cruising
            DashboardFeelingKind.ClearBooks -> R.string.finance_feeling_headline_clear
            DashboardFeelingKind.BlankSlate -> R.string.finance_feeling_headline_blank
        }

    val usesAttentionPulse: Boolean
        get() = when (kind) {
            DashboardFeelingKind.RedZone,
            DashboardFeelingKind.CrunchTime,
            DashboardFeelingKind.Countdown,
            -> true
            else -> false
        }

    fun reasonLineResIds(): List<Int> {
        val lines = buildList {
            if (summary.overdue > 0) add(R.string.dashboard_overdue_count)
            if (summary.urgent > 0) add(R.string.dashboard_urgent_count)
            if (summary.dueSoon > 0) add(R.string.dashboard_due_soon_count)
            if (summary.optimalDay > 0) add(R.string.timeline_summary_optimal_count)
            if (summary.paid > 0) add(R.string.timeline_summary_paid_count)
            if (summary.onTrack > 0) add(R.string.finance_feeling_on_track_count)
        }
        return if (lines.isEmpty()) listOf(R.string.finance_feeling_why_blank) else lines
    }

    fun reasonLineArg(index: Int): Int {
        return when (reasonLineResIds()[index]) {
            R.string.dashboard_overdue_count -> summary.overdue
            R.string.dashboard_urgent_count -> summary.urgent
            R.string.dashboard_due_soon_count -> summary.dueSoon
            R.string.timeline_summary_optimal_count -> summary.optimalDay
            R.string.timeline_summary_paid_count -> summary.paid
            R.string.finance_feeling_on_track_count -> summary.onTrack
            else -> 0
        }
    }

    companion object {
        private fun resolveKind(summary: DashboardSummary): DashboardFeelingKind {
            return when {
                summary.total == 0 -> DashboardFeelingKind.BlankSlate
                summary.overdue > 0 -> DashboardFeelingKind.RedZone
                summary.urgent > 0 -> DashboardFeelingKind.CrunchTime
                summary.dueSoon > 0 -> DashboardFeelingKind.Countdown
                summary.optimalDay > 0 -> DashboardFeelingKind.PrimeWindow
                summary.paid == summary.total -> DashboardFeelingKind.ClearBooks
                else -> DashboardFeelingKind.Cruising
            }
        }
    }
}
