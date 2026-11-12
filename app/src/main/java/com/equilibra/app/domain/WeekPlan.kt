package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.DayItemEntity
import com.equilibra.app.data.local.entity.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * My week: the priorities on their dates, and the lines of each day (my program, tasks and
 * events). Kept free of Android classes so it can be unit-tested.
 */
object WeekPlan {
    const val DAYS = 7

    /** The priorities planned for [date], in the order they are given. */
    fun tasksFor(
        tasks: List<TaskEntity>,
        date: LocalDate,
    ): List<TaskEntity> = tasks.filter { it.date == date }

    /** The Monday of the week that [date] is in. */
    fun mondayOf(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    /** The 7 dates of the week that [date] is in, Monday first. */
    fun datesOfWeek(date: LocalDate): List<LocalDate> {
        val monday = mondayOf(date)
        return (0 until DAYS).map { monday.plusDays(it.toLong()) }
    }

    /**
     * The lines of [date], sorted by time: my program for that weekday (every-week lines)
     * and the one-time tasks and events of exactly that date.
     */
    fun linesFor(
        items: List<DayItemEntity>,
        date: LocalDate,
    ): List<DayItemEntity> =
        items
            .filter { if (it.everyWeek) it.date.dayOfWeek == date.dayOfWeek else it.date == date }
            .sortedBy { it.time }

    /** True when [date] has a one-time task or event (it gets its own dot in the strip). */
    fun hasOneTime(
        items: List<DayItemEntity>,
        date: LocalDate,
    ): Boolean = items.any { !it.everyWeek && it.date == date }
}
