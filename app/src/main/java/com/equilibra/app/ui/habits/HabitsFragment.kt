package com.equilibra.app.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.databinding.DialogFindingBinding
import com.equilibra.app.databinding.FragmentHabitsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 2: "What problems, habits and needs do I find in my daily life?" */
@AndroidEntryPoint
class HabitsFragment : Fragment() {
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null

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

        val adapter = FindingAdapter { finding -> showFindingDialog(finding) }
        binding.recyclerFindings.adapter = adapter
        binding.fabAddFinding.setOnClickListener { showFindingDialog(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.findings.collect { findings ->
                    adapter.submitList(findings)
                    binding.textEmpty.isVisible = findings.isEmpty()
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

    /** Add a finding ([finding] = null) or edit / delete an existing one, in a plain dialog. */
    private fun showFindingDialog(finding: FindingEntity?) {
        val form = DialogFindingBinding.inflate(layoutInflater)
        form.editFinding.setText(finding?.text)
        form.chipGroupType.check(chipOf(finding?.type ?: FindingType.PROBLEM))

        val builder =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(if (finding == null) R.string.finding_add else R.string.finding_edit)
                .setView(form.root)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
        if (finding != null) builder.setNeutralButton(R.string.action_delete) { _, _ -> confirmDelete(finding) }
        val shown = builder.show()

        // Set the Save click here, not in setPositiveButton, so the dialog stays open on an empty text.
        shown.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = form.editFinding.text.toString()
            if (text.isBlank()) {
                form.layoutFinding.error = getString(R.string.error_text_required)
            } else {
                viewModel.save(finding, text, typeOf(form.chipGroupType.checkedChipId))
                shown.dismiss()
            }
        }
        dialog = shown
    }

    private fun confirmDelete(finding: FindingEntity) {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.finding_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.delete(finding) }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    private fun chipOf(type: FindingType): Int =
        when (type) {
            FindingType.PROBLEM -> R.id.chip_problem
            FindingType.HABIT -> R.id.chip_habit
            FindingType.NEED -> R.id.chip_need
        }

    private fun typeOf(chipId: Int): FindingType =
        when (chipId) {
            R.id.chip_habit -> FindingType.HABIT
            R.id.chip_need -> FindingType.NEED
            else -> FindingType.PROBLEM
        }
}
