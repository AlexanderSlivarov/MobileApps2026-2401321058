package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.BalanceWheelDao
import com.equilibra.app.data.local.entity.WheelAssessmentEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelRatingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Dated balance wheel ratings, oldest first, so "before" and "after" can be compared. */
@Singleton
class BalanceWheelRepository
    @Inject
    constructor(
        private val wheelDao: BalanceWheelDao,
    ) {
        fun observeAssessments(): Flow<List<WheelAssessmentWithRatings>> = wheelDao.observeAllWithRatings()

        suspend fun addAssessment(
            assessment: WheelAssessmentEntity,
            ratings: List<WheelRatingEntity>,
        ): Long = wheelDao.insertWithRatings(assessment, ratings)

        suspend fun updateAssessment(
            assessment: WheelAssessmentEntity,
            ratings: List<WheelRatingEntity>,
        ) = wheelDao.updateWithRatings(assessment, ratings)

        suspend fun deleteAssessment(assessment: WheelAssessmentEntity) = wheelDao.deleteAssessment(assessment)
    }
