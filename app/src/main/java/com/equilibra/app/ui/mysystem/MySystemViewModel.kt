package com.equilibra.app.ui.mysystem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equilibra.app.data.local.entity.DayItemEntity
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.data.repository.BalanceWheelRepository
import com.equilibra.app.data.repository.DayItemRepository
import com.equilibra.app.data.repository.DecisionRepository
import com.equilibra.app.data.repository.HabitsRepository
import com.equilibra.app.data.repository.ProfileRepository
import com.equilibra.app.data.repository.TaskRepository
import com.equilibra.app.domain.BalanceOverview
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.FiveWhys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity

/** Everything the Balance page shows: one part from each section. */
data class MySystemUiState(
    /** My rating on the wheel (null = not rated yet). */
    val wheel: Map<WheelSector, Int>? = null,
    val findingCounts: Map<FindingType, Int> = emptyMap(),
    /** Problems traced with the 5 Whys (at least one "Why?" answered). */
    val analyzedProblems: Int = 0,
    val priorities: List<TaskEntity> = emptyList(),
    /** The priorities I postponed, in my order. */
    val postponed: List<TaskEntity> = emptyList(),
    val decisionCounts: Map<DecisionResult, Int> = emptyMap(),
    /** The self-assessment answers, shown as the starting point. */
    val selfAnswers: String = "",
    /** My own words for "Who am I?" and "How do I manage my time?". */
    val whoAmINote: String = "",
    val manageTimeNote: String = "",
    /** All decisions, each shown with its 7 days. */
    val decisions: List<DecisionEntity> = emptyList(),
    /** All observations, for the Statistics. */
    val findings: List<FindingEntity> = emptyList(),
    /** All lines of My week: my program by days, tasks and events. */
    val dayItems: List<DayItemEntity> = emptyList(),
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
        private val dayItemRepository: DayItemRepository,
    ) : ViewModel() {
        /**
         * The day chosen in My week; its week is the one shown. Today at first. Kept here,
         * not in the fragment, so it survives rotation.
         */
        var chosenDate: LocalDate = LocalDate.now()

        /** Recalculated by itself whenever anything changes in any section. */
        val uiState: StateFlow<MySystemUiState> =
            combine(
                wheelRepository.observeScores(),
                habitsRepository.observeFindings(),
                taskRepository.observeTasks(),
                decisionRepository.observeDecisions(),
                profileRepository.observeProfile(),
            ) { wheel, findings, tasks, decisions, profile ->
                MySystemUiState(
                    wheel = wheel,
                    findingCounts = BalanceOverview.countFindings(findings),
                    analyzedProblems = FiveWhys.countAnalyzed(findings),
                    priorities = Eisenhower.inGroupOrder(tasks.filter { !it.isNotStarted }),
                    postponed = tasks.filter { it.isNotStarted },
                    decisionCounts = BalanceOverview.countDecisions(decisions),
                    selfAnswers = profile.selfAnswers,
                    whoAmINote = profile.whoAmINote,
                    manageTimeNote = profile.manageTimeNote,
                    decisions = decisions,
                    findings = findings,
                )
            }.combine(dayItemRepository.observeItems()) { state, items ->
                // combine takes at most 5 flows with names, so the sixth is added like this.
                state.copy(dayItems = items)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MySystemUiState())

        /** Adds a line to My week when [existing] is null, otherwise saves the changes to it. */
        fun saveLine(
            existing: DayItemEntity?,
            time: String,
            text: String,
            everyWeek: Boolean,
        ) {
            viewModelScope.launch {
                val item =
                    (existing ?: DayItemEntity(date = chosenDate, time = "", text = "", everyWeek = false))
                        .copy(time = time.trim(), text = text.trim(), everyWeek = everyWeek)
                if (existing == null) dayItemRepository.addItem(item) else dayItemRepository.updateItem(item)
            }
        }

        fun deleteLine(item: DayItemEntity) {
            viewModelScope.launch { dayItemRepository.deleteItem(item) }
        }

        /** Everything in the app, read once, for the report ("Save as PDF"). */
        suspend fun loadReportData(): ReportData =
            ReportData(
                profile = profileRepository.observeProfile().first(),
                wheel = wheelRepository.observeScores().first(),
                findings = habitsRepository.observeFindings().first(),
                tasks = taskRepository.observeTasks().first(),
                decisions = decisionRepository.observeDecisions().first(),
                dayItems = dayItemRepository.observeItems().first(),
            )
    }
