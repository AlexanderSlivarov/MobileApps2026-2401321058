package com.equilibra.app.ui.selfassessment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentSelfAssessmentBinding
import com.equilibra.app.databinding.ItemWheelAssessmentBinding
import com.equilibra.app.databinding.ItemWheelLegendBinding
import com.equilibra.app.databinding.ViewAnswerTileBinding
import com.equilibra.app.databinding.ViewSelfPartBinding
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.sectorLabel
import com.equilibra.app.ui.common.toDisplayString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 1: "Who am I and how do I manage my time?" The balance wheel, then my answers and my own words for each half. */
@AndroidEntryPoint
class SelfAssessmentFragment : Fragment() {
    private var _binding: FragmentSelfAssessmentBinding? = null
    private val binding get() = _binding!!

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
        binding.partWhoAmI.textTitle.setText(R.string.self_who_am_i)
        binding.partManageTime.textTitle.setText(R.string.self_manage_time)
        binding.partWhoAmI.buttonEdit.setOnClickListener { openSelfEditor(SelfEditViewModel.PART_WHO_AM_I) }
        binding.partManageTime.buttonEdit.setOnClickListener { openSelfEditor(SelfEditViewModel.PART_MANAGE_TIME) }
        binding.buttonAddWheel.setOnClickListener { openWheelEditor(WheelEditViewModel.NEW_ASSESSMENT_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.wheels.collect { showWheels(it) } }
                launch { viewModel.profile.collect { showProfile(it) } }
            }
        }
    }

    /** The two halves: each answer as a tile, and my own words as a quote. */
    private fun showProfile(profile: ProfileEntity) {
        showPart(binding.partWhoAmI, profile.selfAnswers, SelfAssessment.WHO_AM_I, profile.whoAmINote)
        showPart(binding.partManageTime, profile.selfAnswers, SelfAssessment.HOW_I_MANAGE_TIME, profile.manageTimeNote)
    }

    private fun showPart(
        part: ViewSelfPartBinding,
        answers: String,
        questions: IntRange,
        note: String,
    ) {
        part.layoutAnswers.removeAllViews()
        questions.forEach { questionIndex ->
            val texts = SELF_QUESTIONS[questionIndex]
            val row = ViewAnswerTileBinding.inflate(layoutInflater, part.layoutAnswers, true)
            row.textLabel.setText(texts.label)
            // The chosen option, or a dash when the question isn't answered yet.
            row.textValue.text =
                SelfAssessment.answerAt(answers, questionIndex)?.let { getString(texts.options[it]) }
                    ?: getString(R.string.share_none)
        }
        part.textNote.isVisible = note.isNotBlank()
        part.textNote.text = getString(R.string.self_note_format, note)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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

    private fun openSelfEditor(part: Int) {
        findNavController().navigate(
            R.id.action_selfAssessment_to_selfEdit,
            bundleOf(SelfEditViewModel.ARG_PART to part),
        )
    }

    private fun openWheelEditor(assessmentId: Long) {
        findNavController().navigate(
            R.id.action_selfAssessment_to_wheelEdit,
            bundleOf(WheelEditViewModel.ARG_ASSESSMENT_ID to assessmentId),
        )
    }
}
