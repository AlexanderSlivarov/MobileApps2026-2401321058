package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.TaskEntity

/**
 * The four groups of the Eisenhower matrix (Lectures 2-4), numbered I-IV as in the lectures:
 * I important + urgent, II important, III urgent, IV neither.
 */
enum class EisenhowerGroup { I, II, III, IV }

/** Eisenhower rules, kept free of Android classes so they can be unit-tested. */
object Eisenhower {
    /** Importance and urgency are two separate questions; together they give the group. */
    fun groupOf(
        isImportant: Boolean,
        isUrgent: Boolean,
    ): EisenhowerGroup =
        when {
            isImportant && isUrgent -> EisenhowerGroup.I
            isImportant -> EisenhowerGroup.II
            isUrgent -> EisenhowerGroup.III
            else -> EisenhowerGroup.IV
        }

    fun groupOf(task: TaskEntity): EisenhowerGroup = groupOf(task.isImportant, task.isUrgent)

    /** How many of my priorities are in each group. Every group is present, 0 if empty. */
    fun countByGroup(tasks: List<TaskEntity>): Map<EisenhowerGroup, Int> =
        EisenhowerGroup.entries.associateWith { group -> tasks.count { groupOf(it) == group } }
}
