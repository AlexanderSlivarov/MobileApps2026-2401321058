package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** A decision I made (e.g. "no phone after 23:00"), when I started it, and its result. */
@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val startDate: LocalDate,
    val result: DecisionResult = DecisionResult.PENDING,
    /** What actually happened. */
    val note: String = "",
)

/** PENDING = too early to tell. */
enum class DecisionResult { PENDING, WORKED, DID_NOT_WORK }
