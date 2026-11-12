package com.equilibra.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.repository.HabitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** What the Observations screen shows. */
data class HabitsUiState(
    /** The findings of the chosen type (all of them when no type is chosen), newest first. */
    val findings: List<FindingEntity> = emptyList(),
    /** False only when there are no findings at all (then the empty screen shows). */
    val hasAny: Boolean = false,
)

/** The problems, habits and needs I found, newest first (the R of CRUD), with a filter by type. */
@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        habitsRepository: HabitsRepository,
    ) : ViewModel() {
        /** The type chosen in the filter; null = all types. Kept here, so it survives rotation. */
        val filter = MutableStateFlow<FindingType?>(null)

        /** Updates by itself whenever a finding is added, changed or deleted, or the filter changes. */
        val uiState: StateFlow<HabitsUiState> =
            combine(habitsRepository.observeFindings(), filter) { findings, type ->
                HabitsUiState(
                    findings = if (type == null) findings else findings.filter { it.type == type },
                    hasAny = findings.isNotEmpty(),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState())
    }
