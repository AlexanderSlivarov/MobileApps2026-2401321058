package com.equilibra.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 15 -> 16, written by hand because data has to be moved:
 * 1. The wheel keeps only one rating: the newest rating's scores move to the new table
 *    wheel_scores (one row per indicator), then the two old tables are dropped.
 * 2. "My method" is gone. SQLite on older Android can't drop a column, so the profile table
 *    is built again without it: new table, copy the rows, drop the old one, rename.
 * 3. The new table day_items for My week (my program by days, tasks and events).
 */
val MIGRATION_15_16 =
    object : Migration(15, 16) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `wheel_scores` (`sector` TEXT NOT NULL, `score` INTEGER NOT NULL, PRIMARY KEY(`sector`))",
            )
            db.execSQL(
                "INSERT INTO wheel_scores (sector, score) SELECT sector, score FROM wheel_ratings " +
                    "WHERE assessmentId = (SELECT id FROM wheel_assessments ORDER BY date DESC, id DESC LIMIT 1)",
            )
            db.execSQL("DROP TABLE wheel_ratings")
            db.execSQL("DROP TABLE wheel_assessments")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `profile_new` (`id` INTEGER NOT NULL, `selfAnswers` TEXT NOT NULL DEFAULT '', " +
                    "`whoAmINote` TEXT NOT NULL DEFAULT '', `manageTimeNote` TEXT NOT NULL DEFAULT '', PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "INSERT INTO profile_new (id, selfAnswers, whoAmINote, manageTimeNote) " +
                    "SELECT id, selfAnswers, whoAmINote, manageTimeNote FROM profile",
            )
            db.execSQL("DROP TABLE profile")
            db.execSQL("ALTER TABLE profile_new RENAME TO profile")

            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `day_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`date` INTEGER NOT NULL, `time` TEXT NOT NULL, `text` TEXT NOT NULL, `everyWeek` INTEGER NOT NULL)",
            )
        }
    }
