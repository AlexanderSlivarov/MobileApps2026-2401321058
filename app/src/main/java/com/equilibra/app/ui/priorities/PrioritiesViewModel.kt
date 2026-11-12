package com.equilibra.app.ui.priorities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.PriorityOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Everything the Priorities screen shows: my priorities in order, the ones I consciously
 * didn't start (Anti-Kanban), and "My method".
 */
data class PrioritiesUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val notStarted: List<TaskEntity> = emptyList(),
    val method: String = "",
)

/** What the user filled in on the priority dialog. */
data class PriorityForm(
    val title: String,
    val isImportant: Boolean,
    val isUrgent: Boolean,
    val isNotStarted: Boolean,
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
                // partition splits one list in two: (not started, the rest), both still in my order.
                val (notStarted, active) = tasks.partition { it.isNotStarted }
                PrioritiesUiState(tasks = Eisenhower.inGroupOrder(active), notStarted = notStarted, method = profile.prioritizationMethod)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PrioritiesUiState())

        /** Adds a new priority when [existing] is null, otherwise saves the changes to it. */
        fun save(
            existing: TaskEntity?,
            form: PriorityForm,
        ) {
            viewModelScope.launch {
                val task =
                    (existing ?: TaskEntity(title = "", position = 0)).copy(
                        title = form.title.trim(),
                        isImportant = form.isImportant,
                        isUrgent = form.isUrgent,
                        isNotStarted = form.isNotStarted,
                    )
                if (existing == null) taskRepository.addTask(task) else taskRepository.updateTask(task)
            }
        }

        fun delete(task: TaskEntity) {
            viewModelScope.launch { taskRepository.deleteTask(task) }
        }

        /** Moves a priority one place up or down in my order, only inside its own group. */
        fun move(
            task: TaskEntity,
            up: Boolean,
        ) {
            viewModelScope.launch {
                val group = Eisenhower.groupOf(task)
                val sameGroup = uiState.value.tasks.filter { Eisenhower.groupOf(it) == group }
                val changed = PriorityOrder.move(sameGroup, task.id, up)
                if (changed.isNotEmpty()) taskRepository.updateTasks(changed)
            }
        }

        /** Saves my order after a priority was dragged inside its group; [ordered] is that group as it looks now. */
        fun saveOrder(ordered: List<TaskEntity>) {
            viewModelScope.launch {
                val changed = PriorityOrder.renumber(ordered)
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
