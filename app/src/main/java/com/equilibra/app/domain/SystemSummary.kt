package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import kotlin.math.roundToInt

/** The numbers shown on "My system". Averages are null when there is no data yet. */
data class SystemSummary(
    val tasksTotal: Int,
    val tasksDone: Int,
    val journalDays: Int,
    val averageCompletion: Int?,
    val averageEnergy: Double?,
    val averageMotivation: Double?,
    val activitiesCount: Int,
    val activityDays: Int,
)

/** Builds the summary from the other sections, kept free of Android classes so it can be unit-tested. */
object SystemSummaryCalculator {
    fun summarize(
        tasks: List<TaskEntity>,
        entries: List<JournalEntryEntity>,
        activities: List<DayActivityEntity>,
    ): SystemSummary {
        // Days with nothing planned have no completion percent, so they are left out.
        val dailyPercents = entries.mapNotNull { JournalStats.completionPercent(it.tasksDone, it.tasksPlanned) }
        return SystemSummary(
            tasksTotal = tasks.size,
            tasksDone = tasks.count { it.status == TaskStatus.DONE },
            journalDays = entries.size,
            averageCompletion = dailyPercents.takeIf { it.isNotEmpty() }?.average()?.roundToInt(),
            averageEnergy = entries.takeIf { it.isNotEmpty() }?.map { it.energy }?.average(),
            averageMotivation = entries.takeIf { it.isNotEmpty() }?.map { it.motivation }?.average(),
            activitiesCount = activities.size,
            activityDays = activities.map { it.date }.distinct().size,
        )
    }
}
