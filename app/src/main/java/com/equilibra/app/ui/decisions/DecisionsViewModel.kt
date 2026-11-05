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

/** The list of my decisions, newest first (the R of CRUD). */
@HiltViewModel
class DecisionsViewModel
    @Inject
    constructor(
        decisionRepository: DecisionRepository,
    ) : ViewModel() {
        /** Updates by itself whenever a decision is added, changed or deleted. */
        val decisions: StateFlow<List<DecisionEntity>> =
            decisionRepository
                .observeDecisions()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
