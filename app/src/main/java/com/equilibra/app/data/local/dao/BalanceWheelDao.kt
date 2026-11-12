package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.equilibra.app.data.local.entity.WheelScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceWheelDao {
    /** The scores of my one rating; an empty list while I haven't rated yet. */
    @Query("SELECT * FROM wheel_scores")
    fun observeScores(): Flow<List<WheelScoreEntity>>

    /** REPLACE: a score for an indicator that already has one overwrites it. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScores(scores: List<WheelScoreEntity>)
}
