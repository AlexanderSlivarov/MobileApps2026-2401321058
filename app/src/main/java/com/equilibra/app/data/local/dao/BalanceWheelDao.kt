package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.equilibra.app.data.local.entity.WheelAssessmentEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelRatingEntity
import kotlinx.coroutines.flow.Flow

/** An abstract class (not an interface) so it can have the @Transaction method with a body. */
@Dao
abstract class BalanceWheelDao {
    @Transaction
    @Query("SELECT * FROM wheel_assessments ORDER BY date, id")
    abstract fun observeAllWithRatings(): Flow<List<WheelAssessmentWithRatings>>

    @Insert
    protected abstract suspend fun insertAssessment(assessment: WheelAssessmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRatings(ratings: List<WheelRatingEntity>)

    @Update
    protected abstract suspend fun updateAssessment(assessment: WheelAssessmentEntity)

    @Delete
    abstract suspend fun deleteAssessment(assessment: WheelAssessmentEntity)

    /**
     * Saves a new assessment and its ratings together: either both are written or
     * neither is (a transaction), so there is never a wheel without scores.
     */
    @Transaction
    open suspend fun insertWithRatings(
        assessment: WheelAssessmentEntity,
        ratings: List<WheelRatingEntity>,
    ): Long {
        val id = insertAssessment(assessment)
        insertRatings(ratings.map { it.copy(assessmentId = id) })
        return id
    }

    /** Edits an existing assessment; REPLACE overwrites each sector's old score. */
    @Transaction
    open suspend fun updateWithRatings(
        assessment: WheelAssessmentEntity,
        ratings: List<WheelRatingEntity>,
    ) {
        updateAssessment(assessment)
        insertRatings(ratings.map { it.copy(assessmentId = assessment.id) })
    }
}
