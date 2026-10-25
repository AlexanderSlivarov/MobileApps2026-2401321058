package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The free-text answers of the app's single user: the self-assessment, "My method"
 * for prioritization and the final rules and conclusions. Always one row with id = 1.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val personalityType: String = "",
    val superpower1: String = "",
    val superpower2: String = "",
    val superpower3: String = "",
    val wantToImprove: String = "",
    val moreTimeFor: String = "",
    val howIPlan: String = "",
    val stressCoping: String = "",
    val prioritizationMethod: String = "",
    val finalRules: String = "",
    val conclusions: String = "",
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
