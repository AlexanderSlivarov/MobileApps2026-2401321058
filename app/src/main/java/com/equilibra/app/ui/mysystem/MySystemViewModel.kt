package com.equilibra.app.ui.mysystem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.data.repository.DecisionRepository
import com.equilibra.app.data.repository.HabitsRepository
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.BalanceOverview
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.domain.FiveWhys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Everything "My system" shows: one part from each section, plus my final balance rules. */
data class MySystemUiState(
    val wheelNow: WheelAssessmentWithRatings? = null,
    val wheelStart: WheelAssessmentWithRatings? = null,
    val findingCounts: Map<FindingType, Int> = emptyMap(),
    /** Problems traced with the 5 Whys (at least one "Why?" answered). */
    val analyzedProblems: Int = 0,
    val priorities: List<TaskEntity> = emptyList(),
    /** How many priorities I consciously didn't start (Anti-Kanban). */
    val notStartedCount: Int = 0,
    val decisionCounts: Map<DecisionResult, Int> = emptyMap(),
    val finalRules: String = "",
    /** The self-assessment answers, shown as the starting point. */
    val selfAnswers: String = "",
)

/** Puts the four sections together into my final balance system. */
@HiltViewModel
class MySystemViewModel
    @Inject
    constructor(
        wheelRepository: BalanceWheelRepository,
        habitsRepository: HabitsRepository,
        taskRepository: TaskRepository,
        decisionRepository: DecisionRepository,
        private val profileRepository: ProfileRepository,
    ) : ViewModel() {
        /** Recalculated by itself whenever anything changes in any section. */
        val uiState: StateFlow<MySystemUiState> =
            combine(
                wheelRepository.observeAssessments(),
                habitsRepository.observeFindings(),
                taskRepository.observeTasks(),
                decisionRepository.observeDecisions(),
                profileRepository.observeProfile(),
            ) { wheels, findings, tasks, decisions, profile ->
                val (now, start) = BalanceWheel.currentAndStart(wheels)
                MySystemUiState(
                    wheelNow = now,
                    wheelStart = start,
                    findingCounts = BalanceOverview.countFindings(findings),
                    analyzedProblems = FiveWhys.countAnalyzed(findings),
                    priorities = tasks.filter { !it.isNotStarted },
                    notStartedCount = tasks.count { it.isNotStarted },
                    decisionCounts = BalanceOverview.countDecisions(decisions),
                    finalRules = profile.finalRules,
                    selfAnswers = profile.selfAnswers,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MySystemUiState())

        fun saveFinalRules(text: String) {
            viewModelScope.launch {
                val profile = profileRepository.observeProfile().first()
                profileRepository.saveProfile(profile.copy(finalRules = text.trim()))
            }
        }
    }
