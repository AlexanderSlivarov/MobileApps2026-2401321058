package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.TaskDao
import com.equilibra.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single entry point to priority data for the ViewModels. They never touch the DAO
 * or the database directly, so the data source could change without changing the UI.
 */
@Singleton
class TaskRepository
    @Inject
    constructor(
        private val taskDao: TaskDao,
    ) {
        fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()

        suspend fun getTask(id: Long): TaskEntity? = taskDao.getById(id)

        /** A new priority goes to the end of my order. */
        suspend fun addTask(title: String): Long {
            val position = (taskDao.maxPosition() ?: -1) + 1
            return taskDao.insert(TaskEntity(title = title, position = position))
        }

        suspend fun updateTask(task: TaskEntity) = taskDao.update(task)

        suspend fun updateTasks(tasks: List<TaskEntity>) = taskDao.updateAll(tasks)

        suspend fun deleteTask(task: TaskEntity) = taskDao.delete(task)
    }
