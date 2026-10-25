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
    @Query("SELECT * FROM findings ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FindingEntity>>

    @Insert
    suspend fun insert(finding: FindingEntity): Long

    @Update
    suspend fun update(finding: FindingEntity)

    @Delete
    suspend fun delete(finding: FindingEntity)
}
