package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** A decision I tried (e.g. "no phone after 23:00") and whether it worked. */
@Entity(tableName = "decisions")
data class DecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val startDate: LocalDate,
    val result: DecisionResult = DecisionResult.PENDING,
    val note: String = "",
)

enum class DecisionResult { PENDING, WORKED, PARTLY, DID_NOT_WORK }
