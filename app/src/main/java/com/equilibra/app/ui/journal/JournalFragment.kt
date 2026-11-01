package com.equilibra.app.ui.journal

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
import com.equilibra.app.databinding.FragmentJournalBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 4: "How do I track the results of my decisions?" (basic version: the daily journal). */
@AndroidEntryPoint
class JournalFragment : Fragment() {
    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = JournalAdapter { entry -> openEditor(entry.id) }
        binding.recyclerEntries.adapter = adapter
        binding.fabAddEntry.setOnClickListener { openEditor(JournalEditViewModel.NEW_ENTRY_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { entries ->
                    adapter.submitList(entries)
                    binding.textEmpty.isVisible = entries.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openEditor(entryId: Long) {
        findNavController().navigate(
            R.id.action_journal_to_journalEdit,
            bundleOf(JournalEditViewModel.ARG_ENTRY_ID to entryId),
        )
    }
}
