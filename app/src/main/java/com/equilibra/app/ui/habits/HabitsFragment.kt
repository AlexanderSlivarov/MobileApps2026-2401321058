package com.equilibra.app.ui.habits

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
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.databinding.FragmentHabitsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 2: "What problems, habits and needs do I find in my daily life?" */
@AndroidEntryPoint
class HabitsFragment : Fragment() {
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

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
        binding.empty.imageIcon.setImageResource(R.drawable.ic_schedule)
        binding.empty.textTitle.setText(R.string.habits_empty)

        val adapter = FindingAdapter { finding -> openEditor(finding.id) }
        binding.recyclerFindings.adapter = adapter
        binding.fabAddFinding.setOnClickListener { openEditor(FindingEditViewModel.NEW_FINDING_ID) }

        // The filter: check the chip of the type kept in the ViewModel, then follow the taps.
        binding.chipGroupFilter.check(chipOfType(viewModel.filter.value))
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.filter.value = typeOfChip(checkedIds.firstOrNull())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.findings)
                    binding.empty.root.isVisible = !state.hasAny
                    binding.scrollFilter.isVisible = state.hasAny
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun chipOfType(type: FindingType?): Int =
        when (type) {
            null -> R.id.chip_all
            FindingType.PROBLEM -> R.id.chip_problem
            FindingType.HABIT -> R.id.chip_habit
            FindingType.NEED -> R.id.chip_need
        }

    private fun typeOfChip(chipId: Int?): FindingType? =
        when (chipId) {
            R.id.chip_problem -> FindingType.PROBLEM
            R.id.chip_habit -> FindingType.HABIT
            R.id.chip_need -> FindingType.NEED
            else -> null
        }

    private fun openEditor(findingId: Long) {
        findNavController().navigate(
            R.id.action_habits_to_findingEdit,
            bundleOf(FindingEditViewModel.ARG_FINDING_ID to findingId),
        )
    }
}
