package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One of my priorities in the "Priorities" section. [position] is its place in my order:
 * the lowest position is the most important (shown as number 1).
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    // defaultValue is needed so the migration from version 1 can add the column to existing rows.
    @ColumnInfo(defaultValue = "0") val position: Int,
)
