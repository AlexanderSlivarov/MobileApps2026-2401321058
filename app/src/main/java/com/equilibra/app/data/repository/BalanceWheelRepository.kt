package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.BalanceWheelDao
import com.equilibra.app.data.local.entity.WheelScoreEntity
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.domain.BalanceWheel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** My one rating of the balance wheel. */
@Singleton
class BalanceWheelRepository
    @Inject
    constructor(
        private val wheelDao: BalanceWheelDao,
    ) {
        /** The score of each indicator, or null while I haven't rated yet. */
        fun observeScores(): Flow<Map<WheelSector, Int>?> = wheelDao.observeScores().map(BalanceWheel::scoresOf)

        suspend fun saveScores(scores: Map<WheelSector, Int>) =
            wheelDao.saveScores(scores.map { (sector, score) -> WheelScoreEntity(sector, score) })
    }
