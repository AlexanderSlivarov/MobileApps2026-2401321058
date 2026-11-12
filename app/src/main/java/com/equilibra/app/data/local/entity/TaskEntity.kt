package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One of my priorities in the "Priorities" section. [position] is its place in my order:
 * the lowest position is the most important (shown as number 1).
 * [isImportant] and [isUrgent] place it in the Eisenhower matrix (Lectures 2-4); the group
 * itself is not stored, it is worked out from these two. [isNotStarted] = I consciously
 * decided not to start it ("Anti-Kanban", Lectures 3-4), so it is listed separately.
 * defaultValue lets the migrations add the columns to rows that already exist.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(defaultValue = "0") val position: Int,
    @ColumnInfo(defaultValue = "0") val isImportant: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isUrgent: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isNotStarted: Boolean = false,
    /** The date I plan it for; it shows on that day in My week. Null = no date chosen. */
    val date: LocalDate? = null,
)
