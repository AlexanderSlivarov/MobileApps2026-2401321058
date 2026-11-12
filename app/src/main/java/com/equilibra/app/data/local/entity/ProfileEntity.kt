package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The app's single user: the self-assessment answers and my own words. Always one row with id = 1.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    /**
     * The self-assessment answers: one character per question, in order. "0", "1" or "2"
     * is the chosen option, "-" a question not answered yet, e.g. "02-1". Empty at first.
     * defaultValue lets the migration add the column to the existing row.
     */
    @ColumnInfo(defaultValue = "''") val selfAnswers: String = "",
    /** My own words for "Who am I?", next to the answers. Empty when not written. */
    @ColumnInfo(defaultValue = "''") val whoAmINote: String = "",
    /** My own words for "How do I manage my time?". Empty when not written. */
    @ColumnInfo(defaultValue = "''") val manageTimeNote: String = "",
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
