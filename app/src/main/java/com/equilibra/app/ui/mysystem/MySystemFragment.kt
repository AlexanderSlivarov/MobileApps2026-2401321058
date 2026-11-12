package com.equilibra.app.ui.mysystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.PluralsRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.databinding.FragmentMySystemBinding
import com.equilibra.app.databinding.ItemBalancePriorityBinding
import com.equilibra.app.databinding.ViewStatTileBinding
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.toDisplayString
import com.equilibra.app.ui.priorities.colorRes
import com.equilibra.app.ui.priorities.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.equilibra.app.domain.WeekPlan
import com.equilibra.app.ui.priorities.showWeek
import java.time.LocalDate
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.res.ColorStateList
import android.widget.LinearLayout
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.databinding.ItemBalanceDecisionBinding
import com.equilibra.app.databinding.ItemKeptDotBinding
import com.equilibra.app.databinding.ViewAnswerTileBinding
import com.equilibra.app.databinding.ViewSelfNoteBinding
import com.equilibra.app.domain.DecisionDays
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.google.android.material.color.MaterialColors
import com.equilibra.app.databinding.ItemStatTileBinding

/** Section 5: "What does my final balance system look like?" */
@AndroidEntryPoint
class MySystemFragment : Fragment() {
    private var _binding: FragmentMySystemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MySystemViewModel by viewModels()


    /** Opens Android's "save" screen for a PDF file; the chosen place comes back to savePdf. */
    private val saveLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { savePdf(it) }

    /** The day chosen in My week (0 = Monday); today at first. */
    private var chosenDay = WeekPlan.todayIndex(LocalDate.now())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMySystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonSavePdf.setOnClickListener { saveLauncher.launch(getString(R.string.pdf_file_name, LocalDate.now())) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> show(state) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun show(state: MySystemUiState) {
        // 1. Self-assessment: my answers as rows (like on Me) and my own words, then the wheel,
        // start (dashed) and now (filled).
        showAnswers(binding.layoutWhoRows, state.selfAnswers, SelfAssessment.WHO_AM_I, state.whoAmINote)
        showAnswers(binding.layoutTimeRows, state.selfAnswers, SelfAssessment.HOW_I_MANAGE_TIME, state.manageTimeNote)
        binding.layoutSelfSummary.isVisible = SelfAssessment.answeredCount(state.selfAnswers) > 0 ||
            state.whoAmINote.isNotBlank() || state.manageTimeNote.isNotBlank()

        binding.wheelView.setScores(
            state.wheelNow?.let(BalanceWheel::scoresOf),
            state.wheelStart?.let(BalanceWheel::scoresOf),
        )
        binding.textWheel.text =
            if (state.wheelNow == null) {
                getString(R.string.wheel_empty)
            } else {
                listOfNotNull(
                    state.wheelStart?.let { getString(R.string.wheel_compare_format, describe(it)) },
                    getString(R.string.wheel_shown_format, describe(state.wheelNow)),
                ).joinToString("\n")
            }

        // 2. Habit analysis: a big number per type, in the type's color.
        showTile(binding.tileProblems, state.findingCounts[FindingType.PROBLEM] ?: 0, R.plurals.label_problems, R.color.type_problem)
        showTile(binding.tileHabits, state.findingCounts[FindingType.HABIT] ?: 0, R.plurals.label_habits, R.color.type_habit)
        showTile(binding.tileNeeds, state.findingCounts[FindingType.NEED] ?: 0, R.plurals.label_needs, R.color.type_need)
        binding.textTraced.text = count(R.plurals.count_traced, state.analyzedProblems)

        // 3. Prioritization: My method (if written), then one row per priority, in my order,
        // with its group in the group's color.
        binding.layoutMethod.isVisible = state.method.isNotBlank()
        binding.textMethod.text = state.method
        binding.textPrioritiesEmpty.isVisible = state.priorities.isEmpty()
        binding.layoutPriorities.removeAllViews()
        state.priorities.forEachIndexed { index, task ->
            val row = ItemBalancePriorityBinding.inflate(layoutInflater, binding.layoutPriorities, true)
            val group = Eisenhower.groupOf(task)
            row.textNumber.text = (index + 1).toString()
            // The number in a circle of the group's color, like on the Priorities page.
            row.textNumber.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), group.colorRes))
            row.textNumber.setTextColor(MaterialColors.getColor(row.root, com.google.android.material.R.attr.colorSurface))
            row.textTitle.text = task.title
            row.textGroup.setText(group.labelRes)
            row.textGroup.setTextColor(ContextCompat.getColor(requireContext(), group.colorRes))
        }
        binding.textNotStarted.text = count(R.plurals.count_not_started, state.notStartedCount)

        // My week: the priorities on the days I planned them, with this week's dates.
        binding.week.root.isVisible = WeekPlan.hasPlan(state.priorities)
        showWeek(state)

        // 4. Balance tracking: how many decisions worked, as a bar and as numbers.
        val worked = state.decisionCounts[DecisionResult.WORKED] ?: 0
        val total = state.decisionCounts.values.sum()
        binding.textDecisionsProgress.text =
            if (total == 0) {
                getString(R.string.decisions_none)
            } else {
                resources.getQuantityString(R.plurals.decisions_progress, total, worked, total)
            }
        binding.progressDecisions.progress = if (total == 0) 0 else worked * 100 / total
        showTile(binding.tileWorked, worked, R.plurals.label_worked, R.color.result_worked)
        showTile(
            binding.tileDidNotWork,
            state.decisionCounts[DecisionResult.DID_NOT_WORK] ?: 0,
            R.plurals.label_did_not_work,
            R.color.result_did_not_work,
        )
        showTile(binding.tilePending, state.decisionCounts[DecisionResult.PENDING] ?: 0, R.plurals.label_pending, R.color.result_pending)
        showDecisions(state.decisions)

        // Statistics: numbers and facts worked out from my data.
        showStatistics(state)

    }

    /**
     * "Save as PDF": Android's own "save" screen lets me pick the folder and the name (e.g.
     * Downloads), then the report of all my data is written there as a PDF. No storage
     * permission is needed for this.
     */
    private fun savePdf(uri: Uri?) {
        if (uri == null) return // the save screen was closed without saving
        viewLifecycleOwner.lifecycleScope.launch {
            // The report: all the data from every tab, written out on A4 pages.
            val blocks = BalanceReport.build(requireContext(), viewModel.loadReportData())
            requireContext().contentResolver.openOutputStream(uri)?.use { ReportPdf(requireContext()).write(blocks, it) }
            Toast.makeText(requireContext(), R.string.pdf_saved, Toast.LENGTH_SHORT).show()
        }
    }

    /** The answers of one half as rows (short label + chosen option), then my own words as a quote. */
    private fun showAnswers(
        container: LinearLayout,
        answers: String,
        questions: IntRange,
        note: String,
    ) {
        container.removeAllViews()
        questions.forEach { index ->
            val texts = SELF_QUESTIONS[index]
            val row = ViewAnswerTileBinding.inflate(layoutInflater, container, true)
            row.textLabel.setText(texts.label)
            row.textValue.text =
                SelfAssessment.answerAt(answers, index)?.let { getString(texts.options[it]) } ?: getString(R.string.share_none)
        }
        if (note.isNotBlank()) {
            val quote = ViewSelfNoteBinding.inflate(layoutInflater, container, true)
            quote.root.text = getString(R.string.self_note_format, note)
        }
    }

    /** Each decision: its text, its 7 days as dots (green = kept) and "4/7". */
    private fun showDecisions(decisions: List<DecisionEntity>) {
        binding.layoutDecisions.removeAllViews()
        val kept = ContextCompat.getColor(requireContext(), R.color.result_worked)
        val notKept = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceContainerHighest)
        decisions.forEach { decision ->
            val row = ItemBalanceDecisionBinding.inflate(layoutInflater, binding.layoutDecisions, true)
            row.textTitle.text = decision.text
            for (day in 0 until DecisionDays.DAYS) {
                val dot = ItemKeptDotBinding.inflate(layoutInflater, row.layoutDots, true).root
                dot.backgroundTintList = ColorStateList.valueOf(if (DecisionDays.isDone(decision.daysDone, day)) kept else notKept)
            }
            row.textKept.text = getString(R.string.kept_short_format, DecisionDays.doneCount(decision.daysDone))
        }
    }

    /** The Statistics as tiles, two in a row; the card is hidden while there is nothing to show. */
    private fun showStatistics(state: MySystemUiState) {
        val items =
            requireContext().statisticsItems(
                state.selfAnswers,
                state.priorities,
                state.findings,
                state.decisions,
                state.wheelNow,
                state.wheelStart,
            )
        binding.cardStatistics.isVisible = items.isNotEmpty()
        binding.layoutStatistics.removeAllViews()
        items.chunked(2).forEach { pair ->
            val row = LinearLayout(requireContext())
            row.orientation = LinearLayout.HORIZONTAL
            row.isBaselineAligned = false
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            params.topMargin = resources.getDimensionPixelSize(R.dimen.stat_gap)
            binding.layoutStatistics.addView(row, params)
            pair.forEachIndexed { index, item ->
                val tile = ItemStatTileBinding.inflate(layoutInflater, row, true)
                tile.imageIcon.setImageResource(item.icon)
                tile.textValue.text = item.value
                tile.textLabel.text = item.label
                if (index == 1) (tile.root.layoutParams as LinearLayout.LayoutParams).marginStart = resources.getDimensionPixelSize(R.dimen.stat_gap)
            }
        }
    }

    /** My week; tapping a day shows its priorities below the strip. */
    private fun showWeek(state: MySystemUiState) {
        binding.week.showWeek(state.priorities, layoutInflater, chosenDay) { day ->
            chosenDay = day
            showWeek(viewModel.uiState.value)
        }
    }

    /** A number tile: the number in [color] and the word under it in the right form ("1 problem" / "3 problems"). */
    private fun showTile(
        tile: ViewStatTileBinding,
        number: Int,
        @PluralsRes label: Int,
        @ColorRes color: Int,
    ) {
        tile.textNumber.text = number.toString()
        tile.textNumber.setTextColor(ContextCompat.getColor(requireContext(), color))
        tile.textLabel.text = resources.getQuantityString(label, number)
    }

    /**
     * A count with the right word form, e.g. "1 problem" / "3 problems" ("1 проблем" / "3 проблема").
     * Android picks the form from the plurals resource for the current language.
     */
    private fun count(
        @PluralsRes plural: Int,
        number: Int,
    ): String = resources.getQuantityString(plural, number, number)

    /** E.g. "Start · Nov 1, 2026". */
    private fun describe(wheel: WheelAssessmentWithRatings): String =
        getString(
            R.string.wheel_kind_date_format,
            getString(wheel.assessment.kind.labelRes),
            wheel.assessment.date.toDisplayString(),
        )
}
