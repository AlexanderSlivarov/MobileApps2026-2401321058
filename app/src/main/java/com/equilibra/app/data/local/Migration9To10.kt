package com.equilibra.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 9 -> 10: the self-assessment grew from 6 to 12 questions (6 per half), and
 * "What do I do with a big task?" was removed. The answers are one character per question,
 * so the old ones are moved to their new places:
 * old "ABCDEF" (focus, rest, own time, plan, big task, distraction) becomes
 * "ABC---DF----" (the 3 new "who am I" questions and 4 new "time" questions are not answered yet).
 * Written by hand because only data changes, not the schema.
 */
val MIGRATION_9_10 =
    object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "UPDATE profile SET selfAnswers = " +
                    "substr(selfAnswers, 1, 3) || '---' || substr(selfAnswers, 4, 1) || substr(selfAnswers, 6, 1) || '----' " +
                    "WHERE length(selfAnswers) = 6",
            )
        }
    }
