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
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.FiveWhys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity

/** Everything the Balance page shows: one part from each section. */
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
    /** "My method": how I order my priorities, written on Priorities. */
    val method: String = "",
    /** The self-assessment answers, shown as the starting point. */
    val selfAnswers: String = "",
    /** My own words for "Who am I?" and "How do I manage my time?". */
    val whoAmINote: String = "",
    val manageTimeNote: String = "",
    /** All decisions, each shown with its 7 days. */
    val decisions: List<DecisionEntity> = emptyList(),
    /** All observations, for the Statistics. */
    val findings: List<FindingEntity> = emptyList(),
)

/** Puts the four sections together into my final balance system. */
@HiltViewModel
class MySystemViewModel
    @Inject
    constructor(
        private val wheelRepository: BalanceWheelRepository,
        private val habitsRepository: HabitsRepository,
        private val taskRepository: TaskRepository,
        private val decisionRepository: DecisionRepository,
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
                    priorities = Eisenhower.inGroupOrder(tasks.filter { !it.isNotStarted }),
                    notStartedCount = tasks.count { it.isNotStarted },
                    decisionCounts = BalanceOverview.countDecisions(decisions),
                    method = profile.prioritizationMethod,
                    selfAnswers = profile.selfAnswers,
                    whoAmINote = profile.whoAmINote,
                    manageTimeNote = profile.manageTimeNote,
                    decisions = decisions,
                    findings = findings,
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MySystemUiState())

        /** Everything in the app, read once, for the report ("Save as PDF"). */
        suspend fun loadReportData(): ReportData =
            ReportData(
                profile = profileRepository.observeProfile().first(),
                wheels = wheelRepository.observeAssessments().first(),
                findings = habitsRepository.observeFindings().first(),
                tasks = taskRepository.observeTasks().first(),
                decisions = decisionRepository.observeDecisions().first(),
            )
    }
