package com.lenaralabs.cardsreminder.feature.calendar

import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.model.CardPaymentStatusKind
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class BillingPeriodInstance(
    val id: String,
    val cardName: String,
    val cardColorHex: String,
    val cardId: String,
    val startYear: Int,
    val startMonth: Int,
    val startDay: Int,
    val endYear: Int,
    val endMonth: Int,
    val endDay: Int,
    val paymentYear: Int,
    val paymentMonth: Int,
    val paymentDay: Int,
) {
    val periodLabel: String
        get() = CalendarBillingLogic.formatPeriodRange(
            startYear = startYear,
            startMonth = startMonth,
            startDay = startDay,
            endYear = endYear,
            endMonth = endMonth,
            endDay = endDay,
        )

    val paymentDateLabel: String
        get() = CalendarBillingLogic.formatDayMonth(
            year = paymentYear,
            month = paymentMonth,
            day = paymentDay,
        )

    fun overlapsBillingMonth(year: Int, month: Int): Boolean {
        if (startYear == year && startMonth == month) return true
        if (endYear == year && endMonth == month) return true
        return false
    }

    fun hasPaymentInMonth(year: Int, month: Int): Boolean {
        return paymentYear == year && paymentMonth == month
    }
}

sealed class CalendarSelection {
    data class Card(val cardId: String) : CalendarSelection()
    data class BillingPeriod(val periodId: String) : CalendarSelection()
    data class Payment(val periodId: String) : CalendarSelection()
}

data class CalendarMonthInsights(
    val paymentsThisMonth: Int = 0,
    val billingPeriodsThisMonth: Int = 0,
    val nextPayment: NextPaymentInfo? = null,
    val isViewingCurrentMonth: Boolean = true,
)

data class NextPaymentInfo(
    val cardId: String,
    val cardName: String,
    val cardColorHex: String,
    val paymentDay: Int,
    val paymentDateLabel: String,
    val daysUntil: Int?,
    val isPaid: Boolean,
)

enum class CalendarDayEventType(val sortOrder: Int) {
    Payment(0),
    PeriodStart(1),
    PeriodEnd(2),
    InPeriod(3),
}

data class CalendarDayEvent(
    val type: CalendarDayEventType,
    val cardName: String,
    val cardColorHex: String,
    val label: String,
    val periodLabel: String,
)

object CalendarBillingLogic {
    private val locale: Locale get() = Locale.getDefault()

    fun daysInMonth(year: Int, month: Int): Int {
        val calendar = calendarFor(year, month, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun generateCalendarDays(year: Int, month: Int): List<Int?> {
        val calendar = calendarFor(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val leadingBlanks = calendar.get(Calendar.DAY_OF_WEEK) - 1

        return buildList {
            repeat(leadingBlanks) { add(null) }
            for (day in 1..daysInMonth) {
                add(day)
            }
        }
    }

    fun weekdaySymbols(): List<String> {
        val calendar = Calendar.getInstance()
        val symbols = DateFormatSymbols.getInstance(locale).shortWeekdays
        val startIndex = calendar.firstDayOfWeek

        return buildList {
            for (offset in 0 until 7) {
                val index = (startIndex + offset - 1) % 7 + 1
                add(symbols[index].take(2))
            }
        }
    }

    fun addMonths(year: Int, month: Int, delta: Int): Pair<Int, Int> {
        val calendar = calendarFor(year, month, 1)
        calendar.add(Calendar.MONTH, delta)
        return calendar.get(Calendar.YEAR) to (calendar.get(Calendar.MONTH) + 1)
    }

    fun makePeriod(card: ApiCard, startYear: Int, startMonth: Int): BillingPeriodInstance {
        val (endYear, endMonth) = addMonths(startYear, startMonth, 1)
        val (paymentYear, paymentMonth) = addMonths(endYear, endMonth, 1)

        return BillingPeriodInstance(
            id = "${card.id}-$startYear-$startMonth",
            cardName = card.name,
            cardColorHex = card.displayColorHex,
            cardId = card.id,
            startYear = startYear,
            startMonth = startMonth,
            startDay = card.periodStartDay,
            endYear = endYear,
            endMonth = endMonth,
            endDay = card.periodEndDay,
            paymentYear = paymentYear,
            paymentMonth = paymentMonth,
            paymentDay = card.paymentDay,
        )
    }

    fun periodsRelevantToMonth(cards: List<ApiCard>, year: Int, month: Int): List<BillingPeriodInstance> {
        val result = mutableListOf<BillingPeriodInstance>()
        val seen = mutableSetOf<String>()

        for (card in cards) {
            for (delta in -2..1) {
                val (startYear, startMonth) = addMonths(year, month, delta)
                val period = makePeriod(card, startYear, startMonth)
                if (period.id in seen) continue

                if (period.overlapsBillingMonth(year, month) || period.hasPaymentInMonth(year, month)) {
                    seen.add(period.id)
                    result.add(period)
                }
            }
        }

        return result.sortedWith(
            compareBy<BillingPeriodInstance> { it.startYear }
                .thenBy { it.startMonth }
                .thenBy { it.cardName.lowercase(locale) },
        )
    }

    fun billingPeriodsVisibleInMonth(
        periods: List<BillingPeriodInstance>,
        year: Int,
        month: Int,
    ): List<BillingPeriodInstance> {
        return periods.filter { it.overlapsBillingMonth(year, month) }
    }

    fun paymentsInMonth(
        periods: List<BillingPeriodInstance>,
        year: Int,
        month: Int,
    ): List<BillingPeriodInstance> {
        return periods.filter { it.hasPaymentInMonth(year, month) }
    }

    fun dayInPeriod(period: BillingPeriodInstance, year: Int, month: Int, day: Int): Boolean {
        if (day !in 1..31) return false

        if (year == period.startYear && month == period.startMonth && day >= period.startDay) {
            return true
        }

        if (year == period.endYear && month == period.endMonth && day <= period.endDay) {
            return true
        }

        return false
    }

    fun isPaymentDay(period: BillingPeriodInstance, year: Int, month: Int, day: Int): Boolean {
        return period.paymentYear == year && period.paymentMonth == month && period.paymentDay == day
    }

    fun isPeriodSegmentStart(period: BillingPeriodInstance, year: Int, month: Int, day: Int): Boolean {
        if (!dayInPeriod(period, year, month, day)) return false

        if (year == period.startYear && month == period.startMonth && day == period.startDay) {
            return true
        }

        if (year == period.endYear && month == period.endMonth && day == 1 &&
            (year != period.startYear || month != period.startMonth)
        ) {
            return true
        }

        return false
    }

    fun isPeriodSegmentEnd(
        period: BillingPeriodInstance,
        year: Int,
        month: Int,
        day: Int,
        daysInMonth: Int,
    ): Boolean {
        if (!dayInPeriod(period, year, month, day)) return false

        if (year == period.endYear && month == period.endMonth && day == period.endDay) {
            return true
        }

        if (year == period.startYear && month == period.startMonth && day == daysInMonth &&
            (year != period.endYear || month != period.endMonth)
        ) {
            return true
        }

        return false
    }

    fun formatPeriodRange(
        startYear: Int,
        startMonth: Int,
        startDay: Int,
        endYear: Int,
        endMonth: Int,
        endDay: Int,
    ): String {
        val start = formatDayMonth(startYear, startMonth, startDay)
        val end = formatDayMonth(endYear, endMonth, endDay)
        return "$start – $end"
    }

    fun formatDayMonth(year: Int, month: Int, day: Int): String {
        val calendar = calendarFor(year, month, day)
        val format = SimpleDateFormat("d MMM", locale)
        return format.format(calendar.time)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val calendar = calendarFor(year, month, 1)
        val format = SimpleDateFormat("MMMM yyyy", locale)
        return format.format(calendar.time)
    }

    fun isCurrentMonth(year: Int, month: Int, today: Calendar = Calendar.getInstance()): Boolean {
        return today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) + 1 == month
    }

    fun buildMonthInsights(
        year: Int,
        month: Int,
        visiblePayments: List<BillingPeriodInstance>,
        visibleBillingPeriods: List<BillingPeriodInstance>,
        cardStatuses: Map<String, com.lenaralabs.cardsreminder.core.model.ApiCardStatus>,
        today: Calendar = Calendar.getInstance(),
    ): CalendarMonthInsights {
        val isViewingCurrentMonth = isCurrentMonth(year, month, today)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

        val sortedPayments = visiblePayments.sortedBy { it.paymentDay }
        val nextPayment = when {
            sortedPayments.isEmpty() -> null
            isViewingCurrentMonth -> {
                sortedPayments.firstOrNull { it.paymentDay >= todayDay }
                    ?: sortedPayments.last()
            }
            else -> sortedPayments.first()
        }

        val nextPaymentInfo = nextPayment?.let { payment ->
            val daysUntil = if (isViewingCurrentMonth) payment.paymentDay - todayDay else null
            NextPaymentInfo(
                cardId = payment.cardId,
                cardName = payment.cardName,
                cardColorHex = payment.cardColorHex,
                paymentDay = payment.paymentDay,
                paymentDateLabel = payment.paymentDateLabel,
                daysUntil = daysUntil,
                isPaid = isPaymentPeriodPaid(
                    period = payment,
                    status = cardStatuses[payment.cardId],
                ),
            )
        }

        return CalendarMonthInsights(
            paymentsThisMonth = visiblePayments.size,
            billingPeriodsThisMonth = visibleBillingPeriods.size,
            nextPayment = nextPaymentInfo,
            isViewingCurrentMonth = isViewingCurrentMonth,
        )
    }

    fun eventsOnDay(
        day: Int,
        year: Int,
        month: Int,
        activeCards: List<ApiCard>,
        periodsByCardId: Map<String, List<BillingPeriodInstance>>,
    ): List<CalendarDayEvent> {
        return activeCards.flatMap { card ->
            val cardPeriods = periodsByCardId[card.id].orEmpty()
            cardPeriods.mapNotNull { period ->
                when {
                    isPaymentDay(period, year, month, day) -> CalendarDayEvent(
                        type = CalendarDayEventType.Payment,
                        cardName = card.name,
                        cardColorHex = card.displayColorHex,
                        label = period.paymentDateLabel,
                        periodLabel = period.periodLabel,
                    )
                    isPeriodSegmentStart(period, year, month, day) -> CalendarDayEvent(
                        type = CalendarDayEventType.PeriodStart,
                        cardName = card.name,
                        cardColorHex = card.displayColorHex,
                        label = period.periodLabel,
                        periodLabel = period.periodLabel,
                    )
                    isPeriodSegmentEnd(period, year, month, day, daysInMonth(year, month)) -> CalendarDayEvent(
                        type = CalendarDayEventType.PeriodEnd,
                        cardName = card.name,
                        cardColorHex = card.displayColorHex,
                        label = period.periodLabel,
                        periodLabel = period.periodLabel,
                    )
                    else -> null
                }
            }
        }.distinctBy { "${it.cardName}-${it.type}-${it.periodLabel}" }
            .sortedWith(
                compareBy<CalendarDayEvent> { it.type.sortOrder }
                    .thenBy { it.cardName.lowercase(locale) },
            )
    }

    fun isPaymentPeriodPaid(
        period: BillingPeriodInstance,
        status: ApiCardStatus?,
    ): Boolean {
        if (status == null) return false

        if (isSameBillingPeriod(period, status)) {
            return status.isPaidThisCycle || status.kind == CardPaymentStatusKind.Paid
        }

        if (periodEndsBeforeCycleStart(period, status)) {
            return true
        }

        return false
    }

    private fun isSameBillingPeriod(period: BillingPeriodInstance, status: ApiCardStatus): Boolean {
        val statusStart = DateFormatUtils.parseIsoDate(status.cycleStart) ?: return false
        val statusEnd = DateFormatUtils.parseIsoDate(status.cycleEnd) ?: return false
        val periodStart = calendarFor(period.startYear, period.startMonth, period.startDay)
        val periodEnd = calendarFor(period.endYear, period.endMonth, period.endDay)
        val statusStartCal = Calendar.getInstance().apply { time = statusStart }
        val statusEndCal = Calendar.getInstance().apply { time = statusEnd }
        return sameCalendarDay(periodStart, statusStartCal) &&
            sameCalendarDay(periodEnd, statusEndCal)
    }

    private fun periodEndsBeforeCycleStart(period: BillingPeriodInstance, status: ApiCardStatus): Boolean {
        val cycleStart = DateFormatUtils.parseIsoDate(status.cycleStart) ?: return false
        val cycleStartCal = Calendar.getInstance().apply { time = cycleStart }
        val periodEndCal = calendarFor(period.endYear, period.endMonth, period.endDay)
        return periodEndCal.before(cycleStartCal)
    }

    private fun sameCalendarDay(first: Calendar, second: Calendar): Boolean {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
            first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
    }

    private fun calendarFor(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
