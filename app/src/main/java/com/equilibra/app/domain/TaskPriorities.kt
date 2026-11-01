package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus

/** The four Eisenhower matrix quadrants. */
enum class Quadrant { DO_FIRST, SCHEDULE, DELEGATE, ELIMINATE }

/**
 * Prioritization rules, kept free of Android classes so they can be unit-tested.
 */
object TaskPriorities {
    const val TOP_THREE_LIMIT = 3

    fun quadrantOf(
        isUrgent: Boolean,
        isImportant: Boolean,
    ): Quadrant =
        when {
            isUrgent && isImportant -> Quadrant.DO_FIRST
            isImportant -> Quadrant.SCHEDULE
            isUrgent -> Quadrant.DELEGATE
            else -> Quadrant.ELIMINATE
        }

    fun quadrantOf(task: TaskEntity): Quadrant = quadrantOf(task.isUrgent, task.isImportant)

    /** Every quadrant is present in the result, empty ones with an empty list. */
    fun groupByQuadrant(tasks: List<TaskEntity>): Map<Quadrant, List<TaskEntity>> =
        Quadrant.entries.associateWith { quadrant -> tasks.filter { quadrantOf(it) == quadrant } }

    /** Every Kanban column is present in the result, empty ones with an empty list. */
    fun groupByStatus(tasks: List<TaskEntity>): Map<TaskStatus, List<TaskEntity>> =
        TaskStatus.entries.associateWith { status -> tasks.filter { it.status == status } }

    /** Whether the task [taskId] may be in the top 3 without going over the limit. */
    fun canBeTopThree(
        tasks: List<TaskEntity>,
        taskId: Long,
    ): Boolean = tasks.count { it.isTopThree && it.id != taskId } < TOP_THREE_LIMIT

    /** The Kanban column to the left, or null in the first column. */
    fun previousStatus(status: TaskStatus): TaskStatus? = TaskStatus.entries.getOrNull(status.ordinal - 1)

    /** The Kanban column to the right, or null in the last column. */
    fun nextStatus(status: TaskStatus): TaskStatus? = TaskStatus.entries.getOrNull(status.ordinal + 1)
}
