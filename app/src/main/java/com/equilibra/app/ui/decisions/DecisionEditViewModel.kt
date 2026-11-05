package com.equilibra.app.ui.decisions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.repository.DecisionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

/** What the user filled in on the decision form (the date is kept in the ViewModel). */
data class DecisionForm(
    val text: String,
    val hypothesis: String,
    val smartGoal: String,
    val progressSign: String,
    val result: DecisionResult,
    val note: String,
)

/** Create, edit and delete one decision, a small "time lab" experiment (the C, U and D of CRUD). */
@HiltViewModel
class DecisionEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val decisionRepository: DecisionRepository,
    ) : ViewModel() {
        // The id comes from the navigation argument "decisionId" (-1 = new decision).
        private val decisionId: Long = savedStateHandle[ARG_DECISION_ID] ?: NEW_DECISION_ID

        val isNewDecision: Boolean get() = decisionId == NEW_DECISION_ID

        /** The day I started the decision. Kept here so it survives rotation. Today for a new one. */
        var date: LocalDate = LocalDate.now()

        /**
         * True once the stored decision was put into the form. The ViewModel outlives the view
         * (e.g. when the language is switched), so the form isn't reloaded over unsaved typing.
         */
        var formLoaded = false

        /** The stored decision, or null for a new one. Also takes over its date. */
        suspend fun loadDecision(): DecisionEntity? {
            if (isNewDecision) return null
            val decision = decisionRepository.getDecision(decisionId)
            if (decision != null) date = decision.startDate
            return decision
        }

        suspend fun save(form: DecisionForm) {
            val decision =
                DecisionEntity(
                    id = if (isNewDecision) 0 else decisionId,
                    text = form.text.trim(),
                    startDate = date,
                    result = form.result,
                    note = form.note.trim(),
                    hypothesis = form.hypothesis.trim(),
                    smartGoal = form.smartGoal.trim(),
                    progressSign = form.progressSign.trim(),
                )
            if (isNewDecision) decisionRepository.addDecision(decision) else decisionRepository.updateDecision(decision)
        }

        suspend fun delete() {
            decisionRepository.getDecision(decisionId)?.let { decisionRepository.deleteDecision(it) }
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_DECISION_ID = "decisionId"
            const val NEW_DECISION_ID = -1L
        }
    }
