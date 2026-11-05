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
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2, spec = Migration1To2::class)],
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
