package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.WheelAssessmentEntity
import com.equilibra.app.data.local.entity.WheelKind
import com.equilibra.app.data.local.entity.WheelRatingEntity
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.domain.BalanceWheel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/** Create, edit and delete one dated balance wheel rating (the C, U and D of CRUD). */
@HiltViewModel
class WheelEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val wheelRepository: BalanceWheelRepository,
    ) : ViewModel() {
        // The id comes from the navigation argument "assessmentId" (-1 = new rating).
        private val assessmentId: Long = savedStateHandle[ARG_ASSESSMENT_ID] ?: NEW_ASSESSMENT_ID

        val isNewAssessment: Boolean get() = assessmentId == NEW_ASSESSMENT_ID

        // Everything on the form is kept here, so it survives rotation.
        var date: LocalDate = LocalDate.now()
        var kind: WheelKind = WheelKind.CHECK_IN
        val scores: MutableMap<WheelSector, Int> =
            WheelSector.entries.associateWith { BalanceWheel.DEFAULT_SCORE }.toMutableMap()

        private var loaded = false

        /**
         * Fills the form values once: from the stored rating, or for a new rating with
         * START if there is no starting wheel yet (otherwise a check-in).
         */
        suspend fun load() {
            if (loaded) return
            loaded = true
            if (isNewAssessment) {
                val all = wheelRepository.observeAssessments().first()
                kind = if (all.none { it.assessment.kind == WheelKind.START }) WheelKind.START else WheelKind.CHECK_IN
            } else {
                val stored = wheelRepository.getAssessment(assessmentId) ?: return
                date = stored.assessment.date
                kind = stored.assessment.kind
                scores.putAll(BalanceWheel.scoresOf(stored))
            }
        }

        /** Saves the rating. Returns false if this START or FINAL already exists (only one of each). */
        suspend fun save(): Boolean {
            val all = wheelRepository.observeAssessments().first()
            if (BalanceWheel.isKindTaken(all, kind, assessmentId)) return false

            val assessment =
                WheelAssessmentEntity(
                    id = if (isNewAssessment) 0 else assessmentId,
                    date = date,
                    kind = kind,
                )
            // The assessment id is filled in by the DAO inside the transaction.
            val ratings = scores.map { (sector, score) -> WheelRatingEntity(assessmentId = 0, sector = sector, score = score) }
            if (isNewAssessment) {
                wheelRepository.addAssessment(assessment, ratings)
            } else {
                wheelRepository.updateAssessment(assessment, ratings)
            }
            return true
        }

        suspend fun delete() {
            wheelRepository.getAssessment(assessmentId)?.let { wheelRepository.deleteAssessment(it.assessment) }
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_ASSESSMENT_ID = "assessmentId"
            const val NEW_ASSESSMENT_ID = -1L
        }
    }
