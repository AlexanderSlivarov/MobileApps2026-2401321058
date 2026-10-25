package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.DayActivityDao
import com.equilibra.app.data.local.dao.FindingDao
import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.data.local.entity.FindingEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Habit analysis data: "My 24 hours" activities and the findings about them. */
@Singleton
class HabitsRepository
    @Inject
    constructor(
        private val activityDao: DayActivityDao,
        private val findingDao: FindingDao,
    ) {
        fun observeActivities(date: LocalDate): Flow<List<DayActivityEntity>> = activityDao.observeByDate(date)

        fun observeActivitiesBetween(
            from: LocalDate,
            to: LocalDate,
        ): Flow<List<DayActivityEntity>> = activityDao.observeBetween(from, to)

        fun observeAllActivities(): Flow<List<DayActivityEntity>> = activityDao.observeAll()

        suspend fun addActivity(activity: DayActivityEntity): Long = activityDao.insert(activity)

        suspend fun updateActivity(activity: DayActivityEntity) = activityDao.update(activity)

        suspend fun deleteActivity(activity: DayActivityEntity) = activityDao.delete(activity)

        fun observeFindings(): Flow<List<FindingEntity>> = findingDao.observeAll()

        suspend fun addFinding(finding: FindingEntity): Long = findingDao.insert(finding)

        suspend fun updateFinding(finding: FindingEntity) = findingDao.update(finding)

        suspend fun deleteFinding(finding: FindingEntity) = findingDao.delete(finding)
    }
