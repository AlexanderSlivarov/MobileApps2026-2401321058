package com.equilibra.app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.equilibra.app.data.local.dao.BalanceWheelDao
import com.equilibra.app.data.local.dao.DecisionDao
import com.equilibra.app.data.local.dao.FindingDao
import com.equilibra.app.data.local.dao.ProfileDao
import com.equilibra.app.data.local.dao.TaskDao
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelAssessmentEntity
import com.equilibra.app.data.local.entity.WheelRatingEntity

/**
 * The Room database (file: equilibra.db). Room generates the implementation at compile
 * time. When a table changes, [version] goes up and a Migration keeps the existing data.
 */
@Database(
    entities = [
        TaskEntity::class,
        FindingEntity::class,
        DecisionEntity::class,
        WheelAssessmentEntity::class,
        WheelRatingEntity::class,
        ProfileEntity::class,
    ],
    version = 15,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = Migration1To2::class),
        // 3 -> 4: the five "Why?" columns on findings. Only new columns, so Room needs no spec.
        AutoMigration(from = 3, to = 4),
        // 4 -> 5: important / urgent / not started on tasks. Only new columns again.
        AutoMigration(from = 4, to = 5),
        // 5 -> 6: the "My time lab" fields on decisions. Only new columns again.
        AutoMigration(from = 5, to = 6),
        // 6 -> 7: the "My profile" answers on the profile. Only a new column.
        AutoMigration(from = 6, to = 7),
        // 7 -> 8: the self-assessment questions replace the old texts and answers.
        AutoMigration(from = 7, to = 8, spec = Migration7To8::class),
        // 8 -> 9: my own words for "Who am I?" and "How do I manage my time?". Only new columns.
        AutoMigration(from = 8, to = 9),
        // 9 -> 10: MIGRATION_9_10 (by hand, in DatabaseModule) moves the answers to the 12 questions.
        // 10 -> 11: habit effect / frequency and how well a need is met. Only new columns.
        AutoMigration(from = 10, to = 11),
        // 11 -> 12: keep / change / stop for a habit and the area of a need replace the 10 -> 11 fields.
        AutoMigration(from = 11, to = 12, spec = Migration11To12::class),
        // 12 -> 13: the 7 tracked days of a decision replace its three experiment fields.
        AutoMigration(from = 12, to = 13, spec = Migration12To13::class),
        // 13 -> 14: "My balance rules" are gone.
        AutoMigration(from = 13, to = 14, spec = Migration13To14::class),
        // 14 -> 15: the planned days of a priority (My week). Only a new column.
        AutoMigration(from = 14, to = 15),
    ],
)
@TypeConverters(Converters::class)
abstract class EquilibraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun findingDao(): FindingDao

    abstract fun decisionDao(): DecisionDao

    abstract fun balanceWheelDao(): BalanceWheelDao

    abstract fun profileDao(): ProfileDao

    companion object {
        const val NAME = "equilibra.db"
    }
}
