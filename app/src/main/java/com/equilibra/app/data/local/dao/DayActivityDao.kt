package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equilibra.app.data.local.entity.DayActivityEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayActivityDao {
    @Query("SELECT * FROM day_activities WHERE date = :date ORDER BY startMinute")
    fun observeByDate(date: LocalDate): Flow<List<DayActivityEntity>>

    @Query("SELECT * FROM day_activities WHERE date BETWEEN :from AND :to ORDER BY date, startMinute")
    fun observeBetween(
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<DayActivityEntity>>

    @Query("SELECT * FROM day_activities ORDER BY date, startMinute")
    fun observeAll(): Flow<List<DayActivityEntity>>

    @Insert
    suspend fun insert(activity: DayActivityEntity): Long

    @Update
    suspend fun update(activity: DayActivityEntity)

    @Delete
    suspend fun delete(activity: DayActivityEntity)
}
