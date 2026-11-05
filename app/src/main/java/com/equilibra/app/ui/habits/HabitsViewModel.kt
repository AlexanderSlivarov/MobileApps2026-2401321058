package com.equilibra.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.repository.HabitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The problems, habits and needs I found, newest first (the R of CRUD). */
@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        habitsRepository: HabitsRepository,
    ) : ViewModel() {
        /** Updates by itself whenever a finding is added, changed or deleted. */
        val findings: StateFlow<List<FindingEntity>> =
            habitsRepository
                .observeFindings()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
