package com.equilibra.app.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The 7 days I track a decision, kept free of Android classes so it can be unit-tested.
 * The days are stored as one string, a character per day: "1" kept, "0" not (yet).
 */
object DecisionDays {
    const val DAYS = 7

    fun isDone(
        days: String,
        index: Int,
    ): Boolean = days.getOrNull(index) == '1'

    /** The days after tapping day [index] (0 = the first day): kept becomes not kept and back. */
    fun toggle(
        days: String,
        index: Int,
    ): String {
        require(index in 0 until DAYS) { "No such day" }
        val chars = days.padEnd(DAYS, '0').take(DAYS).toCharArray()
        chars[index] = if (chars[index] == '1') '0' else '1'
        return String(chars)
    }

    /** On how many of the 7 days I kept the decision. */
    fun doneCount(days: String): Int = days.take(DAYS).count { it == '1' }

    /** Which day it is today: 1 on the start day, 0 or less before it, more than 7 after the week. */
    fun dayNumber(
        start: LocalDate,
        today: LocalDate,
    ): Long = ChronoUnit.DAYS.between(start, today) + 1

    /** A day can be marked only once it has come (no ticking days in the future). */
    fun canMark(
        index: Int,
        start: LocalDate,
        today: LocalDate,
    ): Boolean = index + 1 <= dayNumber(start, today)
}
