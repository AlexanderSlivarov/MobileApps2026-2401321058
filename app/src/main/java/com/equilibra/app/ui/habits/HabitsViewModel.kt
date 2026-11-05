package com.equilibra.app.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.repository.HabitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The problems, habits and needs I found, with add, edit and delete (full CRUD). */
@HiltViewModel
class HabitsViewModel
    @Inject
    constructor(
        private val habitsRepository: HabitsRepository,
    ) : ViewModel() {
        /** Updates by itself whenever a finding is added, changed or deleted. */
        val findings: StateFlow<List<FindingEntity>> =
            habitsRepository
                .observeFindings()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        /** Adds a new finding when [existing] is null, otherwise saves the changes to it. */
        fun save(
            existing: FindingEntity?,
            text: String,
            type: FindingType,
        ) {
            viewModelScope.launch {
                if (existing == null) {
                    habitsRepository.addFinding(FindingEntity(text = text.trim(), type = type))
                } else {
                    habitsRepository.updateFinding(existing.copy(text = text.trim(), type = type))
                }
            }
        }

        fun delete(finding: FindingEntity) {
            viewModelScope.launch { habitsRepository.deleteFinding(finding) }
        }
    }
