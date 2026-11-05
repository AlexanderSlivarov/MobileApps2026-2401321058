package com.equilibra.app.ui.mysystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.databinding.DialogTextInputBinding
import com.equilibra.app.databinding.FragmentMySystemBinding
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.toDisplayString
import com.equilibra.app.ui.decisions.labelRes
import com.equilibra.app.ui.habits.labelRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 5: "What does my final balance system look like?" */
@AndroidEntryPoint
class MySystemFragment : Fragment() {
    private var _binding: FragmentMySystemBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null

    private val viewModel: MySystemViewModel by viewModels()

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
        binding.buttonEditRules.setOnClickListener { showRulesDialog() }

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
        // 1. Self-assessment: the balance wheel, start (dashed) and now (filled).
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

        // 2. Habit analysis: how many problems, habits and needs I found.
        binding.textFindings.text =
            FindingType.entries.joinToString("\n") { type ->
                getString(R.string.count_format, getString(type.labelRes), state.findingCounts[type] ?: 0)
            } + "\n" + getString(R.string.count_format, getString(R.string.five_whys_analyzed), state.analyzedProblems)

        // 3. Prioritization: my priorities in order.
        binding.textPriorities.text =
            if (state.priorities.isEmpty()) {
                getString(R.string.share_none)
            } else {
                state.priorities.mapIndexed { index, task -> "${index + 1}. ${task.title}" }.joinToString("\n")
            }

        // 4. Balance tracking: how my decisions turned out.
        binding.textDecisions.text =
            DecisionResult.entries.joinToString("\n") { result ->
                getString(R.string.count_format, getString(result.labelRes), state.decisionCounts[result] ?: 0)
            }

        // 5. The answer itself: my final balance rules.
        binding.textRules.text = state.finalRules.ifBlank { getString(R.string.rules_empty) }
    }

    /** E.g. "Start · Nov 1, 2026". */
    private fun describe(wheel: WheelAssessmentWithRatings): String =
        getString(
            R.string.wheel_kind_date_format,
            getString(wheel.assessment.kind.labelRes),
            wheel.assessment.date.toDisplayString(),
        )

    private fun showRulesDialog() {
        val input = DialogTextInputBinding.inflate(layoutInflater)
        input.layoutInput.hint = getString(R.string.rules_hint)
        input.editInput.setText(viewModel.uiState.value.finalRules)

        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rules_title)
                .setView(input.root)
                .setPositiveButton(R.string.action_save) { _, _ ->
                    viewModel.saveFinalRules(input.editInput.text.toString())
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
