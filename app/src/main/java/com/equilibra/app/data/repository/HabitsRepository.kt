package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.FindingDao
import com.equilibra.app.data.local.entity.FindingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Habit analysis data: the problems, habits and needs I found in my daily life. */
@Singleton
class HabitsRepository
    @Inject
    constructor(
        private val findingDao: FindingDao,
    ) {
        fun observeFindings(): Flow<List<FindingEntity>> = findingDao.observeAll()

        suspend fun getFinding(id: Long): FindingEntity? = findingDao.getById(id)

        suspend fun addFinding(finding: FindingEntity): Long = findingDao.insert(finding)

        suspend fun updateFinding(finding: FindingEntity) = findingDao.update(finding)

        suspend fun deleteFinding(finding: FindingEntity) = findingDao.delete(finding)
    }
