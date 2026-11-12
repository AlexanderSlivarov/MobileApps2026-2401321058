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
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The self-assessment answers and my own words (saved in the single profile row) and the balance wheel ratings. */
@HiltViewModel
class SelfAssessmentViewModel
    @Inject
    constructor(
        profileRepository: ProfileRepository,
        wheelRepository: BalanceWheelRepository,
    ) : ViewModel() {
        /** All balance wheel ratings, oldest first. Updates by itself after a save or delete. */
        val wheels: StateFlow<List<WheelAssessmentWithRatings>> =
            wheelRepository
                .observeAssessments()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        /** The profile: the self-assessment answers and my own words. Updates by itself after a save. */
        val profile: StateFlow<ProfileEntity> =
            profileRepository
                .observeProfile()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileEntity())
    }
