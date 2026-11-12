package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** The self-assessment answers and my own words (saved in the single profile row) and my balance wheel rating. */
@HiltViewModel
class SelfAssessmentViewModel
    @Inject
    constructor(
        profileRepository: ProfileRepository,
        wheelRepository: BalanceWheelRepository,
    ) : ViewModel() {
        /** My rating on the balance wheel (null = not rated yet). Updates by itself after a save. */
        val wheel: StateFlow<Map<WheelSector, Int>?> =
            wheelRepository
                .observeScores()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

        /** The profile: the self-assessment answers and my own words. Updates by itself after a save. */
        val profile: StateFlow<ProfileEntity> =
            profileRepository
                .observeProfile()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileEntity())
    }
