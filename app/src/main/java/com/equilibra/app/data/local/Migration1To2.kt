package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 1 -> 2: the app was reduced to exactly what the course requirements ask for.
 * Room writes the SQL for this migration itself (an AutoMigration) by comparing the two
 * exported schemas in app/schemas. The annotations tell it what was removed or renamed on
 * purpose, so it doesn't have to guess. All other data (wheel, decisions, tasks...) is kept.
 */
@DeleteTable.Entries(
    DeleteTable(tableName = "day_activities"),
    DeleteTable(tableName = "journal_entries"),
    DeleteTable(tableName = "steam_days"),
)
@DeleteColumn.Entries(
    DeleteColumn(tableName = "tasks", columnName = "description"),
    DeleteColumn(tableName = "tasks", columnName = "isUrgent"),
    DeleteColumn(tableName = "tasks", columnName = "isImportant"),
    DeleteColumn(tableName = "tasks", columnName = "status"),
    DeleteColumn(tableName = "tasks", columnName = "isTopThree"),
    DeleteColumn(tableName = "tasks", columnName = "createdAt"),
    DeleteColumn(tableName = "findings", columnName = "activityId"),
    DeleteColumn(tableName = "findings", columnName = "createdAt"),
    DeleteColumn(tableName = "profile", columnName = "personalityType"),
    DeleteColumn(tableName = "profile", columnName = "superpower1"),
    DeleteColumn(tableName = "profile", columnName = "superpower2"),
    DeleteColumn(tableName = "profile", columnName = "superpower3"),
    DeleteColumn(tableName = "profile", columnName = "wantToImprove"),
    DeleteColumn(tableName = "profile", columnName = "moreTimeFor"),
    DeleteColumn(tableName = "profile", columnName = "stressCoping"),
    DeleteColumn(tableName = "profile", columnName = "conclusions"),
)
@RenameColumn(tableName = "profile", fromColumnName = "howIPlan", toColumnName = "howIManageTime")
class Migration1To2 : AutoMigrationSpec {
    /** Fixes values that were valid in version 1 but not in version 2. */
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        // Existing priorities keep the order they were created in.
        db.execSQL("UPDATE tasks SET position = id")
        // Good and harmful habits are now simply habits.
        db.execSQL("UPDATE findings SET type = 'HABIT' WHERE type IN ('GOOD_HABIT', 'HARMFUL_HABIT')")
        // "Partly" is no longer a result; such decisions go back to "too early to tell".
        db.execSQL("UPDATE decisions SET result = 'PENDING' WHERE result = 'PARTLY'")
    }
}
