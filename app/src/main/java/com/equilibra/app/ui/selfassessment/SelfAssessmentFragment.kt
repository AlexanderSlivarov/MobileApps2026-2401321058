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
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentSelfAssessmentBinding
import com.equilibra.app.databinding.ItemWheelAssessmentBinding
import com.equilibra.app.databinding.ItemWheelLegendBinding
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 1: "Who am I and how do I manage my time?" The balance wheel and the self-assessment form. */
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

        // Load the saved answers only the first time. After a rotation, another tab or the
        // wheel screen, Android restores the fields by itself, keeping what was typed.
        if (savedInstanceState == null && !viewModel.formLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                fillForm(viewModel.loadProfile())
                viewModel.formLoaded = true
            }
        }

        binding.buttonSave.setOnClickListener { save() }
        binding.buttonAddWheel.setOnClickListener { openWheelEditor(WheelEditViewModel.NEW_ASSESSMENT_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wheels.collect { showWheels(it) }
            }
        }
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
                row.textSector.setText(sector.labelRes)
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
            row.textAverage.text =
                getString(
                    R.string.wheel_average_format,
                    getString(R.string.summary_out_of_five_format, BalanceWheel.average(BalanceWheel.scoresOf(wheel))),
                )
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

    private fun fillForm(profile: ProfileEntity) {
        binding.editPersonality.setText(profile.personalityType)
        binding.editSuperpower1.setText(profile.superpower1)
        binding.editSuperpower2.setText(profile.superpower2)
        binding.editSuperpower3.setText(profile.superpower3)
        binding.editImprove.setText(profile.wantToImprove)
        binding.editMoreTime.setText(profile.moreTimeFor)
        binding.editHowIPlan.setText(profile.howIPlan)
        binding.editStress.setText(profile.stressCoping)
    }

    private fun save() {
        val form =
            SelfAssessmentForm(
                personalityType = binding.editPersonality.text.toString(),
                superpower1 = binding.editSuperpower1.text.toString(),
                superpower2 = binding.editSuperpower2.text.toString(),
                superpower3 = binding.editSuperpower3.text.toString(),
                wantToImprove = binding.editImprove.text.toString(),
                moreTimeFor = binding.editMoreTime.text.toString(),
                howIPlan = binding.editHowIPlan.text.toString(),
                stressCoping = binding.editStress.text.toString(),
            )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(form)
            Snackbar.make(binding.root, R.string.self_saved, Snackbar.LENGTH_SHORT).show()
        }
    }
}
