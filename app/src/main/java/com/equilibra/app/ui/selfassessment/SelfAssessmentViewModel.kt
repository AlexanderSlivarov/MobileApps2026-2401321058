package com.equilibra.app.ui.selfassessment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.domain.SelfAssessment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The self-assessment answers (saved in the single profile row) and the balance wheel ratings. */
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

        /** The self-assessment answers, e.g. "02-1". Updates by itself after each answer. */
        val selfAnswers: StateFlow<String> =
            profileRepository
                .observeProfile()
                .map { it.selfAnswers }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

        /**
         * Saves one answer right away. The profile row also holds "My method" and the final
         * rules, so the current row is read first and only the answers change.
         */
        fun answer(
            questionIndex: Int,
            option: Int,
        ) {
            viewModelScope.launch {
                val current = profileRepository.observeProfile().first()
                val updated = SelfAssessment.withAnswer(current.selfAnswers, questionIndex, option)
                if (updated != current.selfAnswers) profileRepository.saveProfile(current.copy(selfAnswers = updated))
            }
        }
    }
