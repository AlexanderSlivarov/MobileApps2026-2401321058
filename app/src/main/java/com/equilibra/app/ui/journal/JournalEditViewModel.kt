package com.equilibra.app.ui.journal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

/** What the user filled in on the form (the date is kept in the ViewModel). */
data class JournalForm(
    val tasksPlanned: Int,
    val tasksDone: Int,
    val energy: Int,
    val motivation: Int,
    val hadRest: Boolean,
    val correctionNeeded: Boolean,
    val note: String,
)

/** Create, edit and delete one journal entry (the C, U and D of CRUD). */
@HiltViewModel
class JournalEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val journalRepository: JournalRepository,
    ) : ViewModel() {
        // The id comes from the navigation argument "entryId" (-1 = new entry).
        private val entryId: Long = savedStateHandle[ARG_ENTRY_ID] ?: NEW_ENTRY_ID

        val isNewEntry: Boolean get() = entryId == NEW_ENTRY_ID

        /** The chosen day. Kept here so it survives rotation. Today for a new entry. */
        var date: LocalDate = LocalDate.now()

        /** The stored entry, or null for a new one. Also takes over its date. */
        suspend fun loadEntry(): JournalEntryEntity? {
            if (isNewEntry) return null
            val entry = journalRepository.getEntryById(entryId)
            if (entry != null) date = entry.date
            return entry
        }

        /** Saves the form. Returns false if another entry already uses this date (one per day). */
        suspend fun save(form: JournalForm): Boolean {
            val sameDay = journalRepository.getEntryByDate(date)
            if (sameDay != null && sameDay.id != entryId) return false

            val entry =
                JournalEntryEntity(
                    id = if (isNewEntry) 0 else entryId,
                    date = date,
                    tasksPlanned = form.tasksPlanned,
                    tasksDone = form.tasksDone,
                    energy = form.energy,
                    motivation = form.motivation,
                    hadRest = form.hadRest,
                    correctionNeeded = form.correctionNeeded,
                    note = form.note.trim(),
                )
            if (isNewEntry) journalRepository.addEntry(entry) else journalRepository.updateEntry(entry)
            return true
        }

        suspend fun delete() {
            journalRepository.getEntryById(entryId)?.let { journalRepository.deleteEntry(it) }
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_ENTRY_ID = "entryId"
            const val NEW_ENTRY_ID = -1L
        }
    }
