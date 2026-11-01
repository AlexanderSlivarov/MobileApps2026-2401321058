package com.equilibra.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The list of journal entries, newest first (the R of CRUD). */
@HiltViewModel
class JournalViewModel
    @Inject
    constructor(
        journalRepository: JournalRepository,
    ) : ViewModel() {
        /** Updates by itself whenever an entry is added, changed or deleted. */
        val entries: StateFlow<List<JournalEntryEntity>> =
            journalRepository
                .observeEntries()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
