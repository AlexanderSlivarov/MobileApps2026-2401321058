package com.equilibra.app.ui.decisions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.equilibra.app.domain.DecisionDays
import kotlinx.coroutines.launch

/** The list of my decisions, newest first (the R of CRUD). */
@HiltViewModel
class DecisionsViewModel
    @Inject
    constructor(
        private val decisionRepository: DecisionRepository,
    ) : ViewModel() {
        /** Updates by itself whenever a decision is added, changed or deleted. */
        val decisions: StateFlow<List<DecisionEntity>> =
            decisionRepository
                .observeDecisions()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        /** Marks day [index] (0 = the first day) as kept, or takes the mark away again. */
        fun toggleDay(
            decision: DecisionEntity,
            index: Int,
        ) {
            viewModelScope.launch {
                decisionRepository.updateDecision(decision.copy(daysDone = DecisionDays.toggle(decision.daysDone, index)))
            }
        }
    }
