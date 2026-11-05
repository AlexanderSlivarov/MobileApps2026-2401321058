package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equilibra.app.data.local.entity.DecisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DecisionDao {
    /** Newest decisions first. */
    @Query("SELECT * FROM decisions ORDER BY startDate DESC, id DESC")
    fun observeAll(): Flow<List<DecisionEntity>>

    @Query("SELECT * FROM decisions WHERE id = :id")
    suspend fun getById(id: Long): DecisionEntity?

    @Insert
    suspend fun insert(decision: DecisionEntity): Long

    @Update
    suspend fun update(decision: DecisionEntity)

    @Delete
    suspend fun delete(decision: DecisionEntity)
}
