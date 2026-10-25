package com.equilibra.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.equilibra.app.data.local.dao.BalanceWheelDao
import com.equilibra.app.data.local.dao.DayActivityDao
import com.equilibra.app.data.local.dao.DecisionDao
import com.equilibra.app.data.local.dao.FindingDao
import com.equilibra.app.data.local.dao.JournalDao
import com.equilibra.app.data.local.dao.ProfileDao
import com.equilibra.app.data.local.dao.SteamDayDao
import com.equilibra.app.data.local.dao.TaskDao
import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.SteamDayEntity
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
        DayActivityEntity::class,
        FindingEntity::class,
        JournalEntryEntity::class,
        DecisionEntity::class,
        WheelAssessmentEntity::class,
        WheelRatingEntity::class,
        ProfileEntity::class,
        SteamDayEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class EquilibraDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun dayActivityDao(): DayActivityDao

    abstract fun findingDao(): FindingDao

    abstract fun journalDao(): JournalDao

    abstract fun decisionDao(): DecisionDao

    abstract fun balanceWheelDao(): BalanceWheelDao

    abstract fun profileDao(): ProfileDao

    abstract fun steamDayDao(): SteamDayDao

    companion object {
        const val NAME = "equilibra.db"
    }
}
