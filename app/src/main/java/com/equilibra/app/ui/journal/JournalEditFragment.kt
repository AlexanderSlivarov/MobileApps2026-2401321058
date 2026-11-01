package com.equilibra.app.ui.journal

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
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.databinding.FragmentJournalEditBinding
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Add a new journal entry or edit / delete an existing one. */
@AndroidEntryPoint
class JournalEditFragment : Fragment() {
    private var _binding: FragmentJournalEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    private val viewModel: JournalEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentJournalEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewEntry
        showDate()

        // Load the entry into the form only the first time. After a rotation Android
        // restores the fields by itself, keeping what the user had typed.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadEntry()?.let { fillForm(it) }
            }
        }

        binding.editDone.doAfterTextChanged { binding.layoutDone.error = null }
        binding.editPlanned.doAfterTextChanged { binding.layoutPlanned.error = null }
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

    private fun fillForm(entry: JournalEntryEntity) {
        showDate()
        binding.editDone.setText(entry.tasksDone.toString())
        binding.editPlanned.setText(entry.tasksPlanned.toString())
        binding.sliderEnergy.value = entry.energy.toFloat()
        binding.sliderMotivation.value = entry.motivation.toFloat()
        binding.switchRest.isChecked = entry.hadRest
        binding.switchCorrection.isChecked = entry.correctionNeeded
        binding.editNote.setText(entry.note)
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

    private fun save() {
        val done = binding.editDone.text.toString().toIntOrNull()
        val planned = binding.editPlanned.text.toString().toIntOrNull()
        when {
            done == null -> {
                binding.layoutDone.error = getString(R.string.error_enter_number)
                return
            }
            planned == null -> {
                binding.layoutPlanned.error = getString(R.string.error_enter_number)
                return
            }
            done > planned -> {
                binding.layoutDone.error = getString(R.string.error_done_more_than_planned)
                return
            }
        }

        val form =
            JournalForm(
                tasksPlanned = planned,
                tasksDone = done,
                energy = binding.sliderEnergy.value.toInt(),
                motivation = binding.sliderMotivation.value.toInt(),
                hadRest = binding.switchRest.isChecked,
                correctionNeeded = binding.switchCorrection.isChecked,
                note = binding.editNote.text.toString(),
            )
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.save(form)) {
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, R.string.error_date_taken, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.journal_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
