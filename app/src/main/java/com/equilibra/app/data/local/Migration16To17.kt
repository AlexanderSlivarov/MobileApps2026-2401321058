package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 16 -> 17: a priority gets one date instead of days of the week. The old days can't
 * be turned into dates, so their column is removed; the new date column starts empty.
 */
@DeleteColumn(tableName = "tasks", columnName = "days")
class Migration16To17 : AutoMigrationSpec
