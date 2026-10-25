package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.TaskDao
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single entry point to task data for the ViewModels. They never touch the DAO
 * or the database directly, so the data source could change without changing the UI.
 */
@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
    ) {
        fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

        fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)

        fun observeTopThree(): Flow<List<TaskEntity>> = taskDao.observeTopThree()

        suspend fun addTask(task: TaskEntity): Long = taskDao.insert(task)

        suspend fun updateTask(task: TaskEntity) = taskDao.update(task)

        suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)

        suspend fun moveTask(
            id: Long,
            status: TaskStatus,
        ) = taskDao.updateStatus(id, status)
    }
