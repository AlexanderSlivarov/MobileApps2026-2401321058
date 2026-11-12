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
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentSelfAssessmentBinding
import com.equilibra.app.databinding.ItemWheelLegendBinding
import com.equilibra.app.databinding.ViewAnswerTileBinding
import com.equilibra.app.databinding.ViewSelfPartBinding
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.equilibra.app.ui.common.labelRes
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
        binding.buttonAddWheel.setOnClickListener { findNavController().navigate(R.id.action_selfAssessment_to_wheelEdit) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.wheel.collect { showWheel(it) } }
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

    /** Draws my rating and the legend: each indicator's color, name and score. */
    private fun showWheel(scores: Map<WheelSector, Int>?) {
        binding.wheelView.setScores(scores)
        binding.textWheelEmpty.isVisible = scores == null

        binding.layoutLegend.removeAllViews()
        scores?.forEach { (sector, score) ->
            val row = ItemWheelLegendBinding.inflate(layoutInflater, binding.layoutLegend, true)
            row.viewDot.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), sector.colorRes))
            row.textSector.text = requireContext().getString(sector.labelRes)
            row.textScore.text = score.toString()
        }
    }

    private fun openSelfEditor(part: Int) {
        findNavController().navigate(
            R.id.action_selfAssessment_to_selfEdit,
            bundleOf(SelfEditViewModel.ARG_PART to part),
        )
    }
}
