package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** What the user filled in on the self-assessment form: the two parts of the section question. */
data class SelfAssessmentForm(
    val whoAmI: String,
    val howIManageTime: String,
)

/**
 * Reads and saves the self-assessment answers in the single profile row (the R and U of CRUD),
 * and lists the saved balance wheel ratings.
 */
@HiltViewModel
class SelfAssessmentViewModel
    @Inject
    constructor(
        private val profileRepository: ProfileRepository,
        wheelRepository: BalanceWheelRepository,
    ) : ViewModel() {
        /** All balance wheel ratings, oldest first. Updates by itself after a save or delete. */
        val wheels: StateFlow<List<WheelAssessmentWithRatings>> =
            wheelRepository
                .observeAssessments()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        /**
         * True once the saved answers were put into the form. The ViewModel lives on while the
         * user rates the wheel, so coming back doesn't reload the form over unsaved typing.
         */
        var formLoaded = false

        /** The saved profile, or an empty one if nothing was saved yet. */
        suspend fun loadProfile(): ProfileEntity = profileRepository.observeProfile().first()

        /**
         * Saves the form. The profile row also holds "My method" and the final rules,
         * so the current row is read first and only the self-assessment fields change.
         */
        suspend fun save(form: SelfAssessmentForm) {
            val current = loadProfile()
            profileRepository.saveProfile(
                current.copy(
                    whoAmI = form.whoAmI.trim(),
                    howIManageTime = form.howIManageTime.trim(),
                ),
            )
        }
    }
