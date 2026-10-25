package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Progress of one day (1-7) in STEAM Mode. The day's check-in data itself is saved
 * in the regular tables (journal, activities...); this row tracks the day and its reflection.
 */
@Entity(tableName = "steam_days")
data class SteamDayEntity(
    @PrimaryKey val dayNumber: Int,
    val date: LocalDate,
    val isCompleted: Boolean = false,
    val reflection: String = "",
)
