package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 11 -> 12: a habit now says whether I keep, change or stop it, and a need which part
 * of life it is for. The old columns (helps / hurts, how often, how well a need is met) are
 * removed; the new ones are added by Room.
 */
@DeleteColumn.Entries(
    DeleteColumn(tableName = "findings", columnName = "habitEffect"),
    DeleteColumn(tableName = "findings", columnName = "habitFrequency"),
    DeleteColumn(tableName = "findings", columnName = "needMet"),
)
class Migration11To12 : AutoMigrationSpec
