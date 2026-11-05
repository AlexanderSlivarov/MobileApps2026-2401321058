package com.equilibra.app.ui.decisions

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
import com.equilibra.app.databinding.FragmentDecisionsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 4: "How do I track the results of my decisions?" */
@AndroidEntryPoint
class DecisionsFragment : Fragment() {
    private var _binding: FragmentDecisionsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DecisionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDecisionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DecisionAdapter { decision -> openEditor(decision.id) }
        binding.recyclerDecisions.adapter = adapter
        binding.fabAddDecision.setOnClickListener { openEditor(DecisionEditViewModel.NEW_DECISION_ID) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.decisions.collect { decisions ->
                    adapter.submitList(decisions)
                    binding.textEmpty.isVisible = decisions.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openEditor(decisionId: Long) {
        findNavController().navigate(
            R.id.action_decisions_to_decisionEdit,
            bundleOf(DecisionEditViewModel.ARG_DECISION_ID to decisionId),
        )
    }
}
