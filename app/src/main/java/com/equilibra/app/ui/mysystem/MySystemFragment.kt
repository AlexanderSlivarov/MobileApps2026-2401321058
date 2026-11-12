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
import com.equilibra.app.databinding.FragmentMySystemBinding
import com.equilibra.app.databinding.ItemBalancePriorityBinding
import com.equilibra.app.databinding.ViewStatTileBinding
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.priorities.colorRes
import com.equilibra.app.ui.priorities.labelRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.equilibra.app.ui.priorities.showWeek
import java.time.LocalDate
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.content.res.ColorStateList
import android.widget.LinearLayout
import com.equilibra.app.data.local.entity.DayItemEntity
import com.equilibra.app.data.local.entity.DecisionEntity
import androidx.appcompat.app.AlertDialog
import com.equilibra.app.databinding.DialogDayLineBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    /** The open dialog (add / edit a line of My week), closed together with the view. */
    private var dialog: AlertDialog? = null

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
        // My week: the arrows move one week back or forward (the same weekday stays chosen).
        binding.week.buttonPreviousWeek.setOnClickListener { chooseDate(viewModel.chosenDate.minusWeeks(1)) }
        binding.week.buttonNextWeek.setOnClickListener { chooseDate(viewModel.chosenDate.plusWeeks(1)) }
        binding.week.buttonAddLine.setOnClickListener { showLineDialog(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> show(state) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // An open dialog holds the activity, so close it together with the view.
        dialog?.dismiss()
        dialog = null
        _binding = null
    }

    private fun show(state: MySystemUiState) {
        // 1. Self-assessment: my answers as rows (like on Me) and my own words, then the wheel.
        showAnswers(binding.layoutWhoRows, state.selfAnswers, SelfAssessment.WHO_AM_I, state.whoAmINote)
        showAnswers(binding.layoutTimeRows, state.selfAnswers, SelfAssessment.HOW_I_MANAGE_TIME, state.manageTimeNote)
        binding.layoutSelfSummary.isVisible = SelfAssessment.answeredCount(state.selfAnswers) > 0 ||
            state.whoAmINote.isNotBlank() || state.manageTimeNote.isNotBlank()

        binding.wheelView.setScores(state.wheel)
        binding.textWheelEmpty.isVisible = state.wheel == null

        // 2. Habit analysis: a big number per type, in the type's color.
        showTile(binding.tileProblems, state.findingCounts[FindingType.PROBLEM] ?: 0, R.plurals.label_problems, R.color.type_problem)
        showTile(binding.tileHabits, state.findingCounts[FindingType.HABIT] ?: 0, R.plurals.label_habits, R.color.type_habit)
        showTile(binding.tileNeeds, state.findingCounts[FindingType.NEED] ?: 0, R.plurals.label_needs, R.color.type_need)
        binding.textTraced.text = count(R.plurals.count_traced, state.analyzedProblems)

        // 3. Prioritization: one row per priority, in my order, with its group in the group's color.
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
        // How many things I postponed, then their names.
        binding.textNotStarted.text = count(R.plurals.count_not_started, state.postponed.size)
        binding.textPostponed.isVisible = state.postponed.isNotEmpty()
        binding.textPostponed.text = state.postponed.joinToString(separator = "\n") { "• " + it.title }

        // My week: the chosen week, its days and the chosen day's program, tasks and events.
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
                state.wheel,
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

    /** My week; tapping a day shows its lines and priorities below the strip, tapping a line edits it. */
    private fun showWeek(state: MySystemUiState) {
        binding.week.showWeek(
            state.priorities,
            state.dayItems,
            layoutInflater,
            viewModel.chosenDate,
            onChooseDay = ::chooseDate,
            onLineClick = ::showLineDialog,
        )
    }

    private fun chooseDate(date: LocalDate) {
        viewModel.chosenDate = date
        showWeek(viewModel.uiState.value)
    }

    /**
     * Add a line to the chosen day ([item] = null) or edit / delete [item]: the time, what it
     * is, and "Every week" (part of my program) or only this date (a task or event).
     */
    private fun showLineDialog(item: DayItemEntity?) {
        val form = DialogDayLineBinding.inflate(layoutInflater)
        form.editTime.setText(item?.time)
        form.editText.setText(item?.text)
        form.checkEveryWeek.isChecked = item?.everyWeek ?: false

        val builder =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(if (item == null) R.string.line_add_title else R.string.line_edit_title)
                .setView(form.root)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
        if (item != null) builder.setNeutralButton(R.string.action_delete) { _, _ -> viewModel.deleteLine(item) }
        val shown = builder.show()
        // Set here, not in setPositiveButton, so the dialog stays open while the text is empty.
        shown.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = form.editText.text.toString()
            if (text.isBlank()) {
                form.layoutText.error = getString(R.string.error_text_required)
            } else {
                viewModel.saveLine(item, form.editTime.text.toString(), text, form.checkEveryWeek.isChecked)
                shown.dismiss()
            }
        }
        dialog = shown
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
}
