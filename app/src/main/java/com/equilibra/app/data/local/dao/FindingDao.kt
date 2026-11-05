package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equilibra.app.data.local.entity.FindingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FindingDao {
    /** Newest first. */
    @Query("SELECT * FROM findings ORDER BY id DESC")
    fun observeAll(): Flow<List<FindingEntity>>

    @Query("SELECT * FROM findings WHERE id = :id")
    suspend fun getById(id: Long): FindingEntity?

    @Insert
    suspend fun insert(finding: FindingEntity): Long

    @Update
    suspend fun update(finding: FindingEntity)

    @Delete
    suspend fun delete(finding: FindingEntity)
}
