package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Something I found in my daily life during the habit analysis: a problem, a habit or a need.
 * A problem can have up to five "Why?" answers (Lecture 2, "5 Whys"), from the symptom down
 * to a deeper cause. Unused answers are empty. defaultValue lets the migration add the
 * columns to findings that already exist.
 */
@Entity(tableName = "findings")
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val type: FindingType,
    @ColumnInfo(defaultValue = "''") val why1: String = "",
    @ColumnInfo(defaultValue = "''") val why2: String = "",
    @ColumnInfo(defaultValue = "''") val why3: String = "",
    @ColumnInfo(defaultValue = "''") val why4: String = "",
    @ColumnInfo(defaultValue = "''") val why5: String = "",
) {
    /** The five answers in order, for code that handles them all the same way. */
    val whys: List<String> get() = listOf(why1, why2, why3, why4, why5)
}

/** The three kinds the question asks about. Room stores enums as their name (TEXT). */
enum class FindingType { PROBLEM, HABIT, NEED }
