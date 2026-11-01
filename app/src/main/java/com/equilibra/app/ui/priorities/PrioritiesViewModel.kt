package com.equilibra.app.ui.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.Quadrant
import com.equilibra.app.domain.TaskPriorities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything the Priorities screen shows, already grouped and ready to draw. */
data class PrioritiesUiState(
    val quadrants: Map<Quadrant, List<TaskEntity>> = emptyMap(),
    val columns: Map<TaskStatus, List<TaskEntity>> = emptyMap(),
    val topThree: List<TaskEntity> = emptyList(),
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
        /** Rebuilt automatically whenever tasks or the profile change in the database. */
        val uiState: StateFlow<PrioritiesUiState> =
            combine(taskRepository.observeTasks(), profileRepository.observeProfile()) { tasks, profile ->
                PrioritiesUiState(
                    // The matrix is for deciding what to do next, so finished tasks are left out.
                    quadrants = TaskPriorities.groupByQuadrant(tasks.filter { it.status != TaskStatus.DONE }),
                    columns = TaskPriorities.groupByStatus(tasks),
                    topThree = tasks.filter { it.isTopThree },
                    method = profile.prioritizationMethod,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrioritiesUiState())

        /** Which view is shown: false = Eisenhower matrix, true = Kanban. Kept here so it survives rotation. */
        var showKanban = false

        fun moveTask(
            task: TaskEntity,
            status: TaskStatus,
        ) {
            viewModelScope.launch { taskRepository.moveTask(task.id, status) }
        }

        fun saveMethod(text: String) {
            viewModelScope.launch {
                val profile = profileRepository.observeProfile().first()
                profileRepository.saveProfile(profile.copy(prioritizationMethod = text.trim()))
            }
        }
    }
