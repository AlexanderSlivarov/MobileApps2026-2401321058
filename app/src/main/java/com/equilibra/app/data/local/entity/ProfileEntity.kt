package com.equilibra.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The free-text answers of the app's single user: the self-assessment, "My method"
 * for prioritization and the final balance rules. Always one row with id = 1.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    // defaultValue is needed so the migration from version 1 can add the column to the existing row.
    @ColumnInfo(defaultValue = "''") val whoAmI: String = "",
    val howIManageTime: String = "",
    val prioritizationMethod: String = "",
    val finalRules: String = "",
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
