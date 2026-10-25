package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.SteamDayDao
import com.equilibra.app.data.local.entity.SteamDayEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Progress through the 7-day STEAM Mode. */
@Singleton
class SteamRepository
    @Inject
    constructor(
        private val steamDayDao: SteamDayDao,
    ) {
        fun observeDays(): Flow<List<SteamDayEntity>> = steamDayDao.observeAll()

        suspend fun saveDay(day: SteamDayEntity) = steamDayDao.upsert(day)

        suspend fun restart() = steamDayDao.deleteAll()
    }
