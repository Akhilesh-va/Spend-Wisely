package com.example.mindfullexpenses.core.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

data class TimeRange(
    val start: Instant,
    val endExclusive: Instant
)

object TimeRangeProvider {
    private val zoneId: ZoneId = ZoneId.systemDefault()

    fun today(resetHour: Int = 0): TimeRange {
        val now = ZonedDateTime.now(zoneId)
        val boundaryToday = now.toLocalDate().atTime(resetHour, 0).atZone(zoneId)
        val startBoundary = if (now.isBefore(boundaryToday)) boundaryToday.minusDays(1) else boundaryToday
        val endBoundary = startBoundary.plusDays(1)
        return TimeRange(startBoundary.toInstant(), endBoundary.toInstant())
    }

    fun forDate(date: LocalDate): TimeRange {
        val start = date.atStartOfDay(zoneId).toInstant()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        return TimeRange(start, end)
    }

    fun currentWeek(): TimeRange {
        val today = LocalDate.now(zoneId)
        val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
        val firstDay = today.with(weekFields.dayOfWeek(), 1)
        val start = firstDay.atStartOfDay(zoneId).toInstant()
        val end = firstDay.plusWeeks(1).atStartOfDay(zoneId).toInstant()
        return TimeRange(start, end)
    }

    fun currentMonth(): TimeRange {
        val today = LocalDate.now(zoneId)
        val firstDay = today.with(TemporalAdjusters.firstDayOfMonth())
        val start = firstDay.atStartOfDay(zoneId).toInstant()
        val end = firstDay.plusMonths(1).atStartOfDay(zoneId).toInstant()
        return TimeRange(start, end)
    }

    fun nextDailyBoundary(resetHour: Int = 0): Instant {
        val now = ZonedDateTime.now(zoneId)
        val todayBoundary = now.toLocalDate().atTime(resetHour, 0).atZone(zoneId)
        val nextBoundary = if (now.isBefore(todayBoundary)) todayBoundary else todayBoundary.plusDays(1)
        return nextBoundary.toInstant()
    }
}


