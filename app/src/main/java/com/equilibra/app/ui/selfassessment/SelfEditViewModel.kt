package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.domain.SelfAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Edit one half of the self-assessment: "Who am I?" or "How do I manage my time?".
 * Its 3 questions and my own words are saved together with "Save".
 */
@HiltViewModel
class SelfEditViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        // The half comes from the navigation argument "part".
        val part: Int = savedStateHandle[ARG_PART] ?: PART_WHO_AM_I

        /** The question numbers of this half. */
        val questions: IntRange = if (part == PART_WHO_AM_I) SelfAssessment.WHO_AM_I else SelfAssessment.HOW_I_MANAGE_TIME

        /**
         * True once the stored answers were put into the form. The ViewModel outlives the view
         * (e.g. when the language is switched), so the form isn't reloaded over unsaved changes.
         */
        var formLoaded = false

        /** The answers as chosen in the form, not saved yet. The chips are made in code, so the ViewModel keeps them. */
        var draftAnswers = ""

        /** Reads the profile and starts the form from its answers. */
        suspend fun loadProfile(): ProfileEntity {
            val profile = profileRepository.observeProfile().first()
            draftAnswers = profile.selfAnswers
            return profile
        }

        fun choose(
            questionIndex: Int,
            option: Int,
        ) {
            draftAnswers = SelfAssessment.withAnswer(draftAnswers, questionIndex, option)
        }

        /** Saves the chosen answers and my own words for this half. The rest of the profile stays. */
        suspend fun save(note: String) {
            val profile = profileRepository.observeProfile().first()
            val updated =
                if (part == PART_WHO_AM_I) {
                    profile.copy(selfAnswers = draftAnswers, whoAmINote = note.trim())
                } else {
                    profile.copy(selfAnswers = draftAnswers, manageTimeNote = note.trim())
                }
            profileRepository.saveProfile(updated)
        }

        companion object {
            /** Must match the argument name in nav_graph.xml. */
            const val ARG_PART = "part"
            const val PART_WHO_AM_I = 0
            const val PART_MANAGE_TIME = 1
        }
    }
