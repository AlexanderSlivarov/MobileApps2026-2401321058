package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.DecisionDao
import com.equilibra.app.data.local.entity.DecisionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Balance tracking data: my decisions and their results. */
@Singleton
class DecisionRepository
    @Inject
    constructor(
        private val decisionDao: DecisionDao,
    ) {
        fun observeDecisions(): Flow<List<DecisionEntity>> = decisionDao.observeAll()

        suspend fun getDecision(id: Long): DecisionEntity? = decisionDao.getById(id)

        suspend fun addDecision(decision: DecisionEntity): Long = decisionDao.insert(decision)

        suspend fun updateDecision(decision: DecisionEntity) = decisionDao.update(decision)

        suspend fun deleteDecision(decision: DecisionEntity) = decisionDao.delete(decision)
    }
