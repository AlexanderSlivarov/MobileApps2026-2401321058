package com.equilibra.app.domain

import kotlin.math.roundToInt

/** Journal calculations, kept free of Android classes so they can be unit-tested. */
object JournalStats {
    /**
     * Share of planned tasks that were done, in percent (e.g. 2 of 3 = 67).
     * Null when nothing was planned, because there is nothing to compare against.
     */
    fun completionPercent(
        tasksDone: Int,
        tasksPlanned: Int,
    ): Int? = if (tasksPlanned <= 0) null else (tasksDone * 100.0 / tasksPlanned).roundToInt()
}
