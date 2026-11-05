package com.equilibra.app.ui.selfassessment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentSelfAssessmentBinding
import com.equilibra.app.databinding.ItemSelfQuestionBinding
import com.equilibra.app.databinding.ItemWheelAssessmentBinding
import com.equilibra.app.databinding.ItemWheelLegendBinding
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.sectorLabel
import com.equilibra.app.ui.common.selfSummary
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 1: "Who am I and how do I manage my time?" Six questions with a summary, and the balance wheel. */
@AndroidEntryPoint
class SelfAssessmentFragment : Fragment() {
    private var _binding: FragmentSelfAssessmentBinding? = null
    private val binding get() = _binding!!

    /** For each question: its chip group and the ids of its 3 option chips (created in code). */
    private val questionViews = mutableListOf<Pair<ChipGroup, List<Int>>>()

    private val viewModel: SelfAssessmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSelfAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        addQuestions()
        binding.buttonAddWheel.setOnClickListener { openWheelEditor(WheelEditViewModel.NEW_ASSESSMENT_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.wheels.collect { showWheels(it) } }
                launch { viewModel.selfAnswers.collect { showAnswers(it) } }
            }
        }
    }

    /** One row per question; a tap on an option saves the answer right away. */
    private fun addQuestions() {
        SELF_QUESTIONS.forEachIndexed { questionIndex, texts ->
            val row = ItemSelfQuestionBinding.inflate(layoutInflater, binding.layoutQuestions, true)
            row.textQuestion.setText(texts.question)
            val chipIds =
                texts.options.map { optionText ->
                    val chip = layoutInflater.inflate(R.layout.item_self_option, row.chipGroupOptions, false) as Chip
                    chip.id = View.generateViewId()
                    chip.setText(optionText)
                    // The saved answer is shown from the database, so Android doesn't need to restore it.
                    chip.isSaveEnabled = false
                    row.chipGroupOptions.addView(chip)
                    chip.id
                }
            row.chipGroupOptions.setOnCheckedStateChangeListener { _, checkedIds ->
                val option = chipIds.indexOf(checkedIds.firstOrNull())
                if (option >= 0) viewModel.answer(questionIndex, option)
            }
            questionViews.add(row.chipGroupOptions to chipIds)
        }
    }

    /** Marks the saved answers and builds the two-part summary from them. */
    private fun showAnswers(answers: String) {
        questionViews.forEachIndexed { questionIndex, (group, chipIds) ->
            SelfAssessment.answerAt(answers, questionIndex)?.let { option -> group.check(chipIds[option]) }
        }
        val empty = getString(R.string.self_summary_empty)
        binding.textWhoAmI.text = requireContext().selfSummary(answers, SelfAssessment.WHO_AM_I).ifEmpty { empty }
        binding.textManageTime.text = requireContext().selfSummary(answers, SelfAssessment.HOW_I_MANAGE_TIME).ifEmpty { empty }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        questionViews.clear()
        _binding = null
    }

    /** Draws the newest rating (compared with the start), the legend, and the list of saved ratings. */
    private fun showWheels(wheels: List<WheelAssessmentWithRatings>) {
        val (current, start) = BalanceWheel.currentAndStart(wheels)
        val currentScores = current?.let(BalanceWheel::scoresOf)
        val startScores = start?.let(BalanceWheel::scoresOf)
        binding.wheelView.setScores(currentScores, startScores)

        binding.textWheelCaption.text =
            if (current == null) {
                getString(R.string.wheel_empty)
            } else {
                val shown = getString(R.string.wheel_shown_format, describe(current))
                if (start == null) shown else shown + "\n" + getString(R.string.wheel_compare_format, describe(start))
            }

        binding.layoutLegend.removeAllViews()
        if (currentScores != null) {
            WheelSector.entries.forEach { sector ->
                val row = ItemWheelLegendBinding.inflate(layoutInflater, binding.layoutLegend, true)
                row.viewDot.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), sector.colorRes))
                row.textSector.text = requireContext().sectorLabel(sector)
                val now = currentScores.getValue(sector)
                // With a start to compare with, show how the score changed, e.g. "2 → 4".
                row.textScore.text =
                    startScores?.let { getString(R.string.wheel_change_format, it.getValue(sector), now) } ?: now.toString()
            }
        }

        // Newest first. There are only a few ratings, so simple views are enough (no RecyclerView).
        binding.layoutWheels.removeAllViews()
        wheels.asReversed().forEach { wheel ->
            val row = ItemWheelAssessmentBinding.inflate(layoutInflater, binding.layoutWheels, true)
            row.textTitle.text = describe(wheel)
            row.root.setOnClickListener { openWheelEditor(wheel.assessment.id) }
        }
    }

    /** E.g. "Start · Nov 1, 2026". */
    private fun describe(wheel: WheelAssessmentWithRatings): String =
        getString(
            R.string.wheel_kind_date_format,
            getString(wheel.assessment.kind.labelRes),
            wheel.assessment.date.toDisplayString(),
        )

    private fun openWheelEditor(assessmentId: Long) {
        findNavController().navigate(
            R.id.action_selfAssessment_to_wheelEdit,
            bundleOf(WheelEditViewModel.ARG_ASSESSMENT_ID to assessmentId),
        )
    }
}
