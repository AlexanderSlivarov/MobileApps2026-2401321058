package com.equilibra.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.equilibra.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Reads return Flow: Room re-emits the list every time the table changes, so the UI
 * updates by itself. Writes are suspend functions, so they never run on the main thread.
 */
@Dao
interface TaskDao {
    /** In my order: position 0 first. The id breaks ties, so the order is always the same. */
    @Query("SELECT * FROM tasks ORDER BY position, id")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    /** Null when there are no tasks yet. */
    @Query("SELECT MAX(position) FROM tasks")
    suspend fun maxPosition(): Int?

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    /** Saves several tasks in one transaction, e.g. two that swapped places. */
    @Update
    suspend fun updateAll(tasks: List<TaskEntity>)

    @Delete
    suspend fun delete(task: TaskEntity)
}
