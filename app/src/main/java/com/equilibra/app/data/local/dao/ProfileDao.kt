package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.equilibra.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    /** Emits null until the profile is saved for the first time. */
    @Query("SELECT * FROM profile WHERE id = ${ProfileEntity.SINGLE_ROW_ID}")
    fun observe(): Flow<ProfileEntity?>

    /** Insert the row if it doesn't exist yet, otherwise update it. */
    @Upsert
    suspend fun upsert(profile: ProfileEntity)
}
