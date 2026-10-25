package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.equilibra.app.data.local.entity.SteamDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SteamDayDao {
    @Query("SELECT * FROM steam_days ORDER BY dayNumber")
    fun observeAll(): Flow<List<SteamDayEntity>>

    @Upsert
    suspend fun upsert(day: SteamDayEntity)

    @Query("DELETE FROM steam_days")
    suspend fun deleteAll()
}
