package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * A decision I made (e.g. "I go to bed by 23:00"), the day I start it, the 7 days I track,
 * its result and what changed after it. defaultValue lets the migration add [daysDone] to
 * decisions that already exist.
 */
@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val startDate: LocalDate,
    val result: DecisionResult = DecisionResult.PENDING,
    /** What changed after this decision. */
    val note: String = "",
    /**
     * The 7 days from [startDate]: one character per day, "1" = I kept the decision that day,
     * "0" = I didn't (yet). E.g. "1101000". Empty at first.
     */
    @ColumnInfo(defaultValue = "''") val daysDone: String = "",
)

/** PENDING = still in progress, WORKED = it worked out, DID_NOT_WORK = it didn't work out. */
enum class DecisionResult { PENDING, WORKED, DID_NOT_WORK }
