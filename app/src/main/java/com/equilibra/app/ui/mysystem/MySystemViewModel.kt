package com.equilibra.app.ui.mysystem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.repository.HabitsRepository
import com.equilibra.app.data.repository.JournalRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.SystemSummary
import com.equilibra.app.domain.SystemSummaryCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Summary numbers from Priorities, Journal and My 24h (read only). */
@HiltViewModel
class MySystemViewModel
    @Inject
    constructor(
        taskRepository: TaskRepository,
        journalRepository: JournalRepository,
        habitsRepository: HabitsRepository,
    ) : ViewModel() {
        /** Recalculated by itself whenever a task, journal entry or activity changes. */
        val summary: StateFlow<SystemSummary> =
            combine(
                taskRepository.observeTasks(),
                journalRepository.observeEntries(),
                habitsRepository.observeAllActivities(),
            ) { tasks, entries, activities ->
                SystemSummaryCalculator.summarize(tasks, entries, activities)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                SystemSummaryCalculator.summarize(emptyList(), emptyList(), emptyList()),
            )
    }
