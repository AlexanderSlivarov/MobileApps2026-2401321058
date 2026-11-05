package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 7 -> 8: the self-assessment became multiple-choice questions. The two free texts
 * and the old four-question answers are removed; the new answers column is added by Room.
 */
@DeleteColumn.Entries(
    DeleteColumn(tableName = "profile", columnName = "whoAmI"),
    DeleteColumn(tableName = "profile", columnName = "howIManageTime"),
    DeleteColumn(tableName = "profile", columnName = "typeAnswers"),
)
class Migration7To8 : AutoMigrationSpec
