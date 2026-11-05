package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Something I found in my daily life during the habit analysis: a problem, a habit or a need. */
@Entity(tableName = "findings")
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val type: FindingType,
)

/** The three kinds the question asks about. Room stores enums as their name (TEXT). */
enum class FindingType { PROBLEM, HABIT, NEED }
