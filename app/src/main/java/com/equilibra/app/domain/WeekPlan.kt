package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * My week: on which days I plan to work on each priority. Kept free of Android classes so it
 * can be unit-tested. A priority's days are one string, Monday first, "1" = planned.
 */
object WeekPlan {
    const val DAYS = 7

    fun isPlanned(
        days: String,
        dayIndex: Int,
    ): Boolean = days.getOrNull(dayIndex) == '1'

    /** The days string for the chosen days, e.g. [true, false, true, false...] -> "1010000". */
    fun daysOf(chosen: List<Boolean>): String = chosen.take(DAYS).joinToString("") { if (it) "1" else "0" }.padEnd(DAYS, '0')

    /** The priorities planned for day [dayIndex] (0 = Monday), in the order they are given. */
    fun tasksFor(
        tasks: List<TaskEntity>,
        dayIndex: Int,
    ): List<TaskEntity> = tasks.filter { isPlanned(it.days, dayIndex) }

    /** True when at least one priority has a day, so there is a week to show. */
    fun hasPlan(tasks: List<TaskEntity>): Boolean = tasks.any { it.days.contains('1') }

    /** The 7 dates of the week that [today] is in, Monday first. */
    fun datesOfWeek(today: LocalDate): List<LocalDate> {
        val monday = today.with(DayOfWeek.MONDAY)
        return (0 until DAYS).map { monday.plusDays(it.toLong()) }
    }

    /** Today's place in the week: 0 = Monday ... 6 = Sunday. */
    fun todayIndex(today: LocalDate): Int = today.dayOfWeek.value - 1
}
