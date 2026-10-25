package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A task for the "Priorities" section.
 * The Eisenhower quadrant is not stored: it is derived from [isUrgent] and [isImportant].
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isUrgent: Boolean,
    val isImportant: Boolean,
    val status: TaskStatus = TaskStatus.TODO,
    val isTopThree: Boolean = false,
    val dueDate: LocalDate? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Kanban column. Room stores enums as their name (TEXT). */
enum class TaskStatus { TODO, IN_PROGRESS, DONE }
