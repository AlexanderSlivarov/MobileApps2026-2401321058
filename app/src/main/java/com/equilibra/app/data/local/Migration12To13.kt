package com.equilibra.app.data.local

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 12 -> 13: a decision is now tracked for 7 days (the new daysDone column, added by
 * Room). The three experiment fields before it (hypothesis, goal, sign of progress) are removed;
 * "what changed" is the note that was already there.
 */
@DeleteColumn.Entries(
    DeleteColumn(tableName = "decisions", columnName = "hypothesis"),
    DeleteColumn(tableName = "decisions", columnName = "smartGoal"),
    DeleteColumn(tableName = "decisions", columnName = "progressSign"),
)
class Migration12To13 : AutoMigrationSpec
