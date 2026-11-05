package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.ViewModel
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** What the user filled in on the self-assessment form. */
data class SelfAssessmentForm(
    val personalityType: String,
    val superpower1: String,
    val superpower2: String,
    val superpower3: String,
    val wantToImprove: String,
    val moreTimeFor: String,
    val howIPlan: String,
    val stressCoping: String,
)

/** Reads and saves the self-assessment answers in the single profile row (the R and U of CRUD). */
@HiltViewModel
class SelfAssessmentViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        /** The saved profile, or an empty one if nothing was saved yet. */
        suspend fun loadProfile(): ProfileEntity = profileRepository.observeProfile().first()

        /**
         * Saves the form. The profile row also holds "My method" from Priorities,
         * so the current row is read first and only the self-assessment fields change.
         */
        suspend fun save(form: SelfAssessmentForm) {
            val current = loadProfile()
            profileRepository.saveProfile(
                current.copy(
                    personalityType = form.personalityType.trim(),
                    superpower1 = form.superpower1.trim(),
                    superpower2 = form.superpower2.trim(),
                    superpower3 = form.superpower3.trim(),
                    wantToImprove = form.wantToImprove.trim(),
                    moreTimeFor = form.moreTimeFor.trim(),
                    howIPlan = form.howIPlan.trim(),
                    stressCoping = form.stressCoping.trim(),
                ),
            )
        }
    }
