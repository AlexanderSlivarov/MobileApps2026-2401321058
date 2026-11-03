package com.equilibra.app.ui.habits

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.databinding.FragmentHabitsBinding
import com.equilibra.app.ui.common.toDisplayString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Section 2: "What problems, habits and needs do I find in my daily life?" (basic version: My 24 hours). */
@AndroidEntryPoint
class HabitsFragment : Fragment() {
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    private val viewModel: HabitsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ActivityAdapter { activity -> openEditor(activity.id) }
        binding.recyclerActivities.adapter = adapter
        binding.buttonDate.setOnClickListener { pickDate() }
        binding.fabAddActivity.setOnClickListener { openEditor(ActivityEditViewModel.NEW_ACTIVITY_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedDate.collect { date -> binding.buttonDate.text = date.toDisplayString() }
                }
                launch {
                    viewModel.activities.collect { activities ->
                        adapter.submitList(activities)
                        binding.textEmpty.isVisible = activities.isEmpty()
                    }
                }
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

    private fun pickDate() {
        val current = viewModel.selectedDate.value
        // DatePickerDialog counts months from 0, LocalDate from 1.
        dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day -> viewModel.selectedDate.value = LocalDate.of(year, month + 1, day) },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth,
            ).apply { show() }
    }

    /** A new activity starts on the day that is shown in the list. */
    private fun openEditor(activityId: Long) {
        findNavController().navigate(
            R.id.action_habits_to_activityEdit,
            bundleOf(
                ActivityEditViewModel.ARG_ACTIVITY_ID to activityId,
                ActivityEditViewModel.ARG_DATE to viewModel.selectedDate.value.toEpochDay(),
            ),
        )
    }
}
