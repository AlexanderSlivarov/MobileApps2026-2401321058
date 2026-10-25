package com.equilibra.app.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * SQLite has no date type, so Room uses these to store a LocalDate as a number
 * (days since 1970-01-01). That number also sorts and compares correctly in SQL.
 */
class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let(LocalDate::ofEpochDay)
}
