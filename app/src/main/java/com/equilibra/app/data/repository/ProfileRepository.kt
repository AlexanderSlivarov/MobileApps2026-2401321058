package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.ProfileDao
import com.equilibra.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Self-assessment answers, "My method" and the final rules (one profile row). */
@Singleton
class ProfileRepository
    @Inject
    constructor(
        private val profileDao: ProfileDao,
    ) {
        /** Never null for the UI: an empty profile until something is saved. */
        fun observeProfile(): Flow<ProfileEntity> = profileDao.observe().map { it ?: ProfileEntity() }

        suspend fun saveProfile(profile: ProfileEntity) = profileDao.upsert(profile.copy(id = ProfileEntity.SINGLE_ROW_ID))
    }
