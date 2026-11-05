package com.equilibra.app.ui.decisions

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.databinding.FragmentDecisionEditBinding
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Add a new decision or edit / delete an existing one, including its result. */
@AndroidEntryPoint
class DecisionEditFragment : Fragment() {
    private var _binding: FragmentDecisionEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    private val viewModel: DecisionEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDecisionEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewDecision
        showDate()

        // Load the decision into the form only the first time. After a rotation Android
        // restores the fields and the chosen result chip by itself.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadDecision()?.let { fillForm(it) }
            }
        }

        binding.editDecision.doAfterTextChanged { binding.layoutDecision.error = null }
        binding.buttonDate.setOnClickListener { pickDate() }
        binding.buttonSave.setOnClickListener { save() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // An open dialog holds the activity, so close it together with the view.
        dialog?.dismiss()
        dialog = null
        _binding = null
    }

    private fun fillForm(decision: DecisionEntity) {
        showDate()
        binding.editDecision.setText(decision.text)
        binding.chipGroupResult.check(chipOf(decision.result))
        binding.editNote.setText(decision.note)
    }

    private fun showDate() {
        binding.buttonDate.text = viewModel.date.toDisplayString()
    }

    private fun pickDate() {
        val current = viewModel.date
        // DatePickerDialog counts months from 0, LocalDate from 1.
        dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    viewModel.date = LocalDate.of(year, month + 1, day)
                    showDate()
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth,
            ).apply { show() }
    }

    private fun chipOf(result: DecisionResult): Int =
        when (result) {
            DecisionResult.PENDING -> R.id.chip_pending
            DecisionResult.WORKED -> R.id.chip_worked
            DecisionResult.DID_NOT_WORK -> R.id.chip_did_not_work
        }

    private fun resultOf(chipId: Int): DecisionResult =
        when (chipId) {
            R.id.chip_worked -> DecisionResult.WORKED
            R.id.chip_did_not_work -> DecisionResult.DID_NOT_WORK
            else -> DecisionResult.PENDING
        }

    private fun save() {
        val text = binding.editDecision.text.toString()
        if (text.isBlank()) {
            binding.layoutDecision.error = getString(R.string.error_text_required)
            return
        }
        val result = resultOf(binding.chipGroupResult.checkedChipId)
        val note = binding.editNote.text.toString()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(text, result, note)
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.decision_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
