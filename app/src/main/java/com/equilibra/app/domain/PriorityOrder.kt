package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.TaskEntity

/** Ordering my priorities, kept free of Android classes so it can be unit-tested. */
object PriorityOrder {
    /**
     * Moves the task [taskId] one place up (more important) or down in [tasks], which must be
     * in the current order. The list is then numbered again 0, 1, 2... and only the tasks whose
     * position changed are returned, ready to save. Empty when it can't move (already first or last).
     */
    fun move(
        tasks: List<TaskEntity>,
        taskId: Long,
        up: Boolean,
    ): List<TaskEntity> {
        val index = tasks.indexOfFirst { it.id == taskId }
        val otherIndex = if (up) index - 1 else index + 1
        if (index == -1 || otherIndex !in tasks.indices) return emptyList()

        val reordered = tasks.toMutableList()
        reordered[index] = tasks[otherIndex]
        reordered[otherIndex] = tasks[index]
        return renumber(reordered)
    }

    /**
     * Numbers [ordered] again 0, 1, 2... in its new order (e.g. after a drag) and returns only
     * the tasks whose position changed, ready to save. Empty when the order didn't change.
     */
    fun renumber(ordered: List<TaskEntity>): List<TaskEntity> =
        ordered
            .mapIndexed { position, task -> task.copy(position = position) }
            .filter { renumbered -> renumbered.position != ordered.first { it.id == renumbered.id }.position }
}
