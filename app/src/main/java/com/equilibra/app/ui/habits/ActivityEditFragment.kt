package com.equilibra.app.ui.habits

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
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
import com.equilibra.app.data.local.entity.ActivityCategory
import com.equilibra.app.databinding.FragmentActivityEditBinding
import com.equilibra.app.domain.ActivityTimes
import com.equilibra.app.ui.common.minuteOfDayToDisplayString
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Add a new activity to "My 24 hours" or edit / delete an existing one. */
@AndroidEntryPoint
class ActivityEditFragment : Fragment() {
    private var _binding: FragmentActivityEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    private val viewModel: ActivityEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentActivityEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewActivity
        showPickedValues()

        viewLifecycleOwner.lifecycleScope.launch {
            val activity = viewModel.loadActivity()
            // Fill the title only the first time. After a rotation Android restores what was typed.
            if (savedInstanceState == null) activity?.let { binding.editTitle.setText(it.title) }
            showPickedValues()
        }

        binding.editTitle.doAfterTextChanged { binding.layoutTitle.error = null }
        binding.buttonDate.setOnClickListener { pickDate() }
        binding.buttonCategory.setOnClickListener { pickCategory() }
        binding.buttonStart.setOnClickListener {
            pickTime(viewModel.startMinute) { viewModel.startMinute = it }
        }
        binding.buttonEnd.setOnClickListener {
            pickTime(viewModel.endMinute) { viewModel.endMinute = it }
        }
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

    /** Shows the values that are chosen with pickers (they live in the ViewModel). */
    private fun showPickedValues() {
        binding.buttonDate.text = viewModel.date.toDisplayString()
        binding.buttonCategory.setText(viewModel.category.labelRes)
        binding.buttonStart.text = minuteOfDayToDisplayString(viewModel.startMinute)
        binding.buttonEnd.text = minuteOfDayToDisplayString(viewModel.endMinute)
        val duration = ActivityTimes.durationMinutes(viewModel.startMinute, viewModel.endMinute)
        binding.textDuration.text = getString(R.string.activity_duration_format, requireContext().formatDuration(duration))
    }

    private fun pickDate() {
        val current = viewModel.date
        // DatePickerDialog counts months from 0, LocalDate from 1.
        dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    viewModel.date = LocalDate.of(year, month + 1, day)
                    showPickedValues()
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth,
            ).apply { show() }
    }

    private fun pickCategory() {
        val categories = ActivityCategory.entries
        val labels = categories.map { getString(it.labelRes) }.toTypedArray()
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.activity_category)
                .setSingleChoiceItems(labels, categories.indexOf(viewModel.category)) { picker, which ->
                    viewModel.category = categories[which]
                    showPickedValues()
                    picker.dismiss()
                }.show()
    }

    /** Opens a clock in the phone's 12 or 24 hour format and passes the result as minutes from midnight. */
    private fun pickTime(
        currentMinute: Int,
        onPicked: (Int) -> Unit,
    ) {
        dialog =
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    onPicked(hour * 60 + minute)
                    showPickedValues()
                },
                currentMinute / 60,
                currentMinute % 60,
                DateFormat.is24HourFormat(requireContext()),
            ).apply { show() }
    }

    private fun save() {
        val title = binding.editTitle.text.toString()
        if (title.isBlank()) {
            binding.layoutTitle.error = getString(R.string.error_title_required)
            return
        }
        if (viewModel.startMinute == viewModel.endMinute) {
            Snackbar.make(binding.root, R.string.error_same_start_end, Snackbar.LENGTH_LONG).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(title)
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.activity_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
