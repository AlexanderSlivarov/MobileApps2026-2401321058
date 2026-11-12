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
import com.equilibra.app.ui.common.selfSummary
import com.equilibra.app.ui.common.toDisplayString
import com.equilibra.app.ui.priorities.colorRes
import com.equilibra.app.ui.priorities.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.equilibra.app.domain.WeekPlan
import com.equilibra.app.ui.priorities.showWeek
import java.time.LocalDate
import com.google.android.material.color.MaterialColors
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.io.OutputStream

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
        // My balance rules: the answer to the final question, at the top.

        // 1. Self-assessment: the summary of the answers, then the wheel, start (dashed) and now (filled).
        val whoAmI = requireContext().selfSummary(state.selfAnswers, SelfAssessment.WHO_AM_I)
        val manageTime = requireContext().selfSummary(state.selfAnswers, SelfAssessment.HOW_I_MANAGE_TIME)
        binding.layoutSelfSummary.isVisible = whoAmI.isNotEmpty() || manageTime.isNotEmpty()
        binding.textWhoAmI.text = whoAmI.ifEmpty { getString(R.string.share_none) }
        binding.textManageTime.text = manageTime.ifEmpty { getString(R.string.share_none) }

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
    }

    /** Draws the page (everything above the "Save as PDF" button) as a PDF into [output]. */
    private fun writePdf(output: OutputStream) {
        val content = binding.layoutContent
        val background = MaterialColors.getColor(content, android.R.attr.colorBackground)
        BalancePdf.write(content, binding.buttonSavePdf.top, background, output)
    }

    /**
     * "Save as PDF": Android's own "save" screen lets me pick the folder and the name (e.g.
     * Downloads), then the PDF is written there. No storage permission is needed for this.
     */
    private fun savePdf(uri: Uri?) {
        if (uri == null) return // the save screen was closed without saving
        requireContext().contentResolver.openOutputStream(uri)?.use { writePdf(it) }
        Toast.makeText(requireContext(), R.string.pdf_saved, Toast.LENGTH_SHORT).show()
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
