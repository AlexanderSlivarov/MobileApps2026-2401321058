package com.equilibra.app.di

import android.content.Context
import androidx.room.Room
import com.equilibra.app.data.local.EquilibraDatabase
import com.equilibra.app.data.local.dao.BalanceWheelDao
import com.equilibra.app.data.local.dao.DayActivityDao
import com.equilibra.app.data.local.dao.DecisionDao
import com.equilibra.app.data.local.dao.FindingDao
import com.equilibra.app.data.local.dao.JournalDao
import com.equilibra.app.data.local.dao.ProfileDao
import com.equilibra.app.data.local.dao.SteamDayDao
import com.equilibra.app.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tells Hilt how to build the database and DAOs. Installed in SingletonComponent,
 * so there is exactly one database for the whole app (opening it is expensive).
 * It uses the application context, not an activity's, so it can't leak an activity.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): EquilibraDatabase = Room.databaseBuilder(context, EquilibraDatabase::class.java, EquilibraDatabase.NAME).build()

    @Provides
    fun provideTaskDao(db: EquilibraDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideDayActivityDao(db: EquilibraDatabase): DayActivityDao = db.dayActivityDao()

    @Provides
    fun provideFindingDao(db: EquilibraDatabase): FindingDao = db.findingDao()

    @Provides
    fun provideJournalDao(db: EquilibraDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideDecisionDao(db: EquilibraDatabase): DecisionDao = db.decisionDao()

    @Provides
    fun provideBalanceWheelDao(db: EquilibraDatabase): BalanceWheelDao = db.balanceWheelDao()

    @Provides
    fun provideProfileDao(db: EquilibraDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideSteamDayDao(db: EquilibraDatabase): SteamDayDao = db.steamDayDao()
}
