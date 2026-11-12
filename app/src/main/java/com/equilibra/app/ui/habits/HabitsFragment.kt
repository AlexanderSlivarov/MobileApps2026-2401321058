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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.findings.collect { findings ->
                    adapter.submitList(findings)
                    binding.empty.root.isVisible = findings.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openEditor(findingId: Long) {
        findNavController().navigate(
            R.id.action_habits_to_findingEdit,
            bundleOf(FindingEditViewModel.ARG_FINDING_ID to findingId),
        )
    }
}
