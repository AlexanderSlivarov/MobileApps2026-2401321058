package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equilibra.app.data.local.entity.DayItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayItemDao {
    /** All lines of My week; there are few, so the screen picks each day's lines itself. */
    @Query("SELECT * FROM day_items ORDER BY time, id")
    fun observeAll(): Flow<List<DayItemEntity>>

    @Insert
    suspend fun insert(item: DayItemEntity): Long

    @Update
    suspend fun update(item: DayItemEntity)

    @Delete
    suspend fun delete(item: DayItemEntity)
}
