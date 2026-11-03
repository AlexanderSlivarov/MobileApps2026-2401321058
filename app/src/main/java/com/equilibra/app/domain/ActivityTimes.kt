package com.equilibra.app.domain

/** Time math for "My 24 hours" activities. Times are minutes from midnight (0-1439). */
object ActivityTimes {
    const val MINUTES_PER_DAY = 24 * 60

    /**
     * How long an activity lasts, in minutes. If it ends before it starts it crosses
     * midnight, e.g. 23:00-07:00 is 8 hours.
     */
    fun durationMinutes(
        startMinute: Int,
        endMinute: Int,
    ): Int =
        if (endMinute >= startMinute) {
            endMinute - startMinute
        } else {
            MINUTES_PER_DAY - startMinute + endMinute
        }
}
