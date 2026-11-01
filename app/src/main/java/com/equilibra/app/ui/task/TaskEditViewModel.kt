package com.equilibra.app.ui.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.TaskPriorities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** What the user typed and chose in the form. */
data class TaskForm(
    val title: String,
    val description: String,
    val isUrgent: Boolean,
    val isImportant: Boolean,
    val status: TaskStatus,
    val isTopThree: Boolean,
)

/** Create, edit and delete one task (the C, U and D of CRUD; R is the Priorities screen). */
@HiltViewModel
class TaskEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val taskRepository: TaskRepository,
    ) : ViewModel() {
        // The id comes from the navigation argument "taskId" (-1 = new task).
        private val taskId: Long = savedStateHandle[ARG_TASK_ID] ?: NEW_TASK_ID

        val isNewTask: Boolean get() = taskId == NEW_TASK_ID

        /** The stored task, or null when creating a new one. */
        suspend fun loadTask(): TaskEntity? = if (isNewTask) null else taskRepository.observeTask(taskId).first()

        /** Saves the form. Returns false if it would make more than 3 top tasks. */
        suspend fun save(form: TaskForm): Boolean {
            val allTasks = taskRepository.observeTasks().first()
            if (form.isTopThree && !TaskPriorities.canBeTopThree(allTasks, taskId)) return false

            val existing = allTasks.find { it.id == taskId }
            if (existing == null) {
                taskRepository.addTask(
                    TaskEntity(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        isUrgent = form.isUrgent,
                        isImportant = form.isImportant,
                        status = form.status,
                        isTopThree = form.isTopThree,
                    ),
                )
            } else {
                taskRepository.updateTask(
                    existing.copy(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        isUrgent = form.isUrgent,
                        isImportant = form.isImportant,
                        status = form.status,
                        isTopThree = form.isTopThree,
                    ),
                )
            }
            return true
        }

        suspend fun delete() {
            loadTask()?.let { taskRepository.deleteTask(it) }
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_TASK_ID = "taskId"
            const val NEW_TASK_ID = -1L
        }
    }
