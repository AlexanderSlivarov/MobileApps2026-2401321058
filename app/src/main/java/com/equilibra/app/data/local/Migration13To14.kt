package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/** Version 13 -> 14: "My balance rules" were removed from the Balance page, and their column with them. */
@DeleteColumn(tableName = "profile", columnName = "finalRules")
class Migration13To14 : AutoMigrationSpec
