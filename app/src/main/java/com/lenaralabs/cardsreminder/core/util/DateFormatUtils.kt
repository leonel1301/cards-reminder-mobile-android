package com.lenaralabs.cardsreminder.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatUtils {
    private val locale: Locale get() = Locale.getDefault()

    private val isoFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US),
    )

    fun parseIsoDate(value: String): java.util.Date? {
        for (format in isoFormats) {
            runCatching { return format.parse(value) }.getOrNull()
        }
        return null
    }

    fun formatShortDate(value: String): String {
        val date = parseIsoDate(value) ?: return value
        return SimpleDateFormat("d MMM yyyy", locale).format(date)
    }

    fun formatShortDateTime(value: String): String {
        val date = parseIsoDate(value) ?: return value
        return SimpleDateFormat("d MMM yyyy, HH:mm", locale).format(date)
    }

    fun formatDateRange(start: String, end: String): String {
        return "${formatShortDate(start)} – ${formatShortDate(end)}"
    }

    fun formatTimelineHeaderDate(date: Date = Date()): String {
        return SimpleDateFormat("EEEE, d MMMM", locale).format(date)
    }

    fun isToday(isoDate: String): Boolean {
        val date = parseIsoDate(isoDate) ?: return false
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}
