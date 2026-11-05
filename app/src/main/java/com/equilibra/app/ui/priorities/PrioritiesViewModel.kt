package com.equilibra.app.ui.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.PriorityOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything the Priorities screen shows: my priorities in order and "My method". */
data class PrioritiesUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val method: String = "",
)

/**
 * Survives rotation (the Fragment and its views are recreated, this object is not).
 * Hilt creates it and passes in the repositories.
 */
@HiltViewModel
class PrioritiesViewModel
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        /** Rebuilt automatically whenever the priorities or the profile change in the database. */
        val uiState: StateFlow<PrioritiesUiState> =
            combine(taskRepository.observeTasks(), profileRepository.observeProfile()) { tasks, profile ->
                PrioritiesUiState(tasks = tasks, method = profile.prioritizationMethod)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrioritiesUiState())

        /** Adds a new priority when [existing] is null, otherwise renames it. */
        fun save(
            existing: TaskEntity?,
            title: String,
        ) {
            viewModelScope.launch {
                if (existing == null) {
                    taskRepository.addTask(title.trim())
                } else {
                    taskRepository.updateTask(existing.copy(title = title.trim()))
                }
            }
        }

        fun delete(task: TaskEntity) {
            viewModelScope.launch { taskRepository.deleteTask(task) }
        }

        /** Moves a priority one place up or down in my order. */
        fun move(
            task: TaskEntity,
            up: Boolean,
        ) {
            viewModelScope.launch {
                val changed = PriorityOrder.move(uiState.value.tasks, task.id, up)
                if (changed.isNotEmpty()) taskRepository.updateTasks(changed)
            }
        }

        fun saveMethod(text: String) {
            viewModelScope.launch {
                val profile = profileRepository.observeProfile().first()
                profileRepository.saveProfile(profile.copy(prioritizationMethod = text.trim()))
            }
        }
    }
