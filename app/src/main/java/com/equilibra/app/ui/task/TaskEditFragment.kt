package com.equilibra.app.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.databinding.FragmentTaskEditBinding
import com.equilibra.app.domain.TaskPriorities
import com.equilibra.app.ui.priorities.hintRes
import com.equilibra.app.ui.priorities.titleRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Add a new task or edit / delete an existing one. */
@AndroidEntryPoint
class TaskEditFragment : Fragment() {
    private var _binding: FragmentTaskEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null

    private val viewModel: TaskEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTaskEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewTask

        // Load the task into the form only the first time. After a rotation Android
        // restores the fields by itself, keeping what the user had typed.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadTask()?.let { fillForm(it) }
            }
        }

        binding.editTitle.doAfterTextChanged { binding.layoutTitle.error = null }
        binding.switchUrgent.setOnCheckedChangeListener { _, _ -> updateQuadrantText() }
        binding.switchImportant.setOnCheckedChangeListener { _, _ -> updateQuadrantText() }
        updateQuadrantText()

        binding.buttonSave.setOnClickListener { save() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialog?.dismiss()
        dialog = null
        _binding = null
    }

    private fun fillForm(task: TaskEntity) {
        binding.editTitle.setText(task.title)
        binding.editDescription.setText(task.description)
        binding.switchUrgent.isChecked = task.isUrgent
        binding.switchImportant.isChecked = task.isImportant
        binding.switchTopThree.isChecked = task.isTopThree
        binding.toggleStatus.check(
            when (task.status) {
                TaskStatus.TODO -> R.id.button_status_todo
                TaskStatus.IN_PROGRESS -> R.id.button_status_in_progress
                TaskStatus.DONE -> R.id.button_status_done
            },
        )
    }

    private fun save() {
        val form =
            TaskForm(
                title = binding.editTitle.text.toString(),
                description = binding.editDescription.text.toString(),
                isUrgent = binding.switchUrgent.isChecked,
                isImportant = binding.switchImportant.isChecked,
                status =
                    when (binding.toggleStatus.checkedButtonId) {
                        R.id.button_status_in_progress -> TaskStatus.IN_PROGRESS
                        R.id.button_status_done -> TaskStatus.DONE
                        else -> TaskStatus.TODO
                    },
                isTopThree = binding.switchTopThree.isChecked,
            )
        if (form.title.isBlank()) {
            binding.layoutTitle.error = getString(R.string.error_title_required)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.save(form)) {
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, R.string.error_top_three_full, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.task_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    /** Live preview of the Eisenhower quadrant while the switches change. */
    private fun updateQuadrantText() {
        val quadrant = TaskPriorities.quadrantOf(binding.switchUrgent.isChecked, binding.switchImportant.isChecked)
        binding.textQuadrant.text =
            getString(R.string.task_quadrant_format, getString(quadrant.titleRes), getString(quadrant.hintRes))
    }
}
