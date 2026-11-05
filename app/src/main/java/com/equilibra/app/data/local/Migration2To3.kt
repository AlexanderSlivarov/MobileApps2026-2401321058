package com.equilibra.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 2 -> 3: the balance wheel changed from 6 life areas to the 7 indicators of
 * "Where am I now?" (Lecture 1). The tables stay the same, but old ratings use sector names
 * that no longer exist and can't be converted (e.g. "calm" is not the opposite of "stress"),
 * so they are removed. Room would crash reading an unknown enum name otherwise.
 * Written by hand because only data changes, not the schema.
 */
val MIGRATION_2_3 =
    object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM wheel_ratings")
            db.execSQL("DELETE FROM wheel_assessments")
        }
    }
