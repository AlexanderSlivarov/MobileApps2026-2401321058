package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A decision I made (e.g. "no phone after 23:00"), when I started it, and its result.
 * "My time lab" (Lecture 1) turns it into a small experiment: a [hypothesis], a SMART goal
 * for 7 days ([smartGoal]) and how I will recognize progress ([progressSign]).
 * defaultValue lets the migration add the columns to decisions that already exist.
 */
@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val startDate: LocalDate,
    val result: DecisionResult = DecisionResult.PENDING,
    /** What actually happened. */
    val note: String = "",
    @ColumnInfo(defaultValue = "''") val hypothesis: String = "",
    @ColumnInfo(defaultValue = "''") val smartGoal: String = "",
    @ColumnInfo(defaultValue = "''") val progressSign: String = "",
)

/** PENDING = too early to tell. */
enum class DecisionResult { PENDING, WORKED, DID_NOT_WORK }
