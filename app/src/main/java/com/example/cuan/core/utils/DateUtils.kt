package com.example.cuan.core.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Date utility functions
 */
object DateUtils {
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale("id", "ID"))
    private val shortDateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("id", "ID"))
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
    private val shortMonthFormatter = DateTimeFormatter.ofPattern("MMM", Locale("id", "ID"))
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun formatDate(date: LocalDate): String = date.format(dateFormatter)

    fun formatShortDate(date: LocalDate): String = date.format(shortDateFormatter)

    fun formatMonthYear(month: YearMonth): String = month.format(monthYearFormatter)

    fun formatShortMonth(month: YearMonth): String = month.format(shortMonthFormatter)

    fun formatIso(date: LocalDate): String = date.format(isoFormatter)

    fun parseIso(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, isoFormatter)
        } catch (e: Exception) {
            null
        }
    }

    fun today(): LocalDate = LocalDate.now()

    fun nowMillis(): Long = System.currentTimeMillis()

    fun daysBetween(start: LocalDate, end: LocalDate): Long = ChronoUnit.DAYS.between(start, end)

    fun daysUntil(deadline: LocalDate): Long = daysBetween(LocalDate.now(), deadline)

    fun getCurrentMonth(): YearMonth = YearMonth.now()

    fun getPreviousMonth(month: YearMonth): YearMonth = month.minusMonths(1)

    fun getMonthRange(month: YearMonth): Pair<LocalDate, LocalDate> {
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        return start to end
    }

    fun todayFormatted(): String = formatDate(today())

    fun getDayOfWeekName(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID"))
        return date.format(formatter)
    }

    fun getShortDayOfWeek(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEE", Locale("id", "ID"))
        return date.format(formatter)
    }
}