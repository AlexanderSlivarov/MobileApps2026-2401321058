package com.equilibra.app.ui.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.repository.HabitsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Create, edit and delete one finding, including the 5 Whys of a problem (the C, U and D of CRUD). */
@HiltViewModel
class FindingEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val habitsRepository: HabitsRepository,
    ) : ViewModel() {
        // The id comes from the navigation argument "findingId" (-1 = new finding).
        private val findingId: Long = savedStateHandle[ARG_FINDING_ID] ?: NEW_FINDING_ID

        val isNewFinding: Boolean get() = findingId == NEW_FINDING_ID

        /**
         * True once the stored finding was put into the form. The ViewModel outlives the view
         * (e.g. when the language is switched), so the form isn't reloaded over unsaved typing.
         */
        var formLoaded = false

        /** The stored finding, or null for a new one. */
        suspend fun loadFinding(): FindingEntity? = if (isNewFinding) null else habitsRepository.getFinding(findingId)

        /** Saves the form. Only a problem keeps its "Why?" answers. */
        suspend fun save(
            text: String,
            type: FindingType,
            whys: List<String>,
        ) {
            val answers = if (type == FindingType.PROBLEM) whys.map { it.trim() } else List(whys.size) { "" }
            val finding =
                FindingEntity(
                    id = if (isNewFinding) 0 else findingId,
                    text = text.trim(),
                    type = type,
                    why1 = answers[0],
                    why2 = answers[1],
                    why3 = answers[2],
                    why4 = answers[3],
                    why5 = answers[4],
                )
            if (isNewFinding) habitsRepository.addFinding(finding) else habitsRepository.updateFinding(finding)
        }

        suspend fun delete() {
            habitsRepository.getFinding(findingId)?.let { habitsRepository.deleteFinding(it) }
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_FINDING_ID = "findingId"
            const val NEW_FINDING_ID = -1L
        }
    }
