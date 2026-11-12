package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Something I found in my daily life during the habit analysis: a problem, a habit or a need.
 * A problem can have up to five "Why?" answers (Lecture 2, "5 Whys"), from the symptom down
 * to a deeper cause. Unused answers are empty. defaultValue lets the migration add the
 * columns to findings that already exist. A habit can say whether I keep, change or stop it;
 * a need can say which part of life it is for.
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
    /** For a habit: do I keep it, change it or stop it? Null when not chosen (or not a habit). */
    val habitPlan: HabitPlan? = null,
    /** For a need: which part of life it is for. Null when not chosen (or not a need). */
    val needArea: NeedArea? = null,
) {
    /** The five answers in order, for code that handles them all the same way. */
    val whys: List<String> get() = listOf(why1, why2, why3, why4, why5)
}

/** The three kinds the question asks about. Room stores enums as their name (TEXT). */
enum class FindingType { PROBLEM, HABIT, NEED }

/** What I do with a habit. */
enum class HabitPlan { KEEP, CHANGE, STOP }

/** Which part of life a need is for. */
enum class NeedArea { STUDY, REST, HEALTH, PEOPLE }
