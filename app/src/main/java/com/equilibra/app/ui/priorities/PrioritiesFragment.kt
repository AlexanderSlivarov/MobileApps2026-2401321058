package com.equilibra.app.ui.priorities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.databinding.DialogTextInputBinding
import com.equilibra.app.databinding.FragmentPrioritiesBinding
import com.equilibra.app.databinding.ViewTaskGroupBinding
import com.equilibra.app.domain.Quadrant
import com.equilibra.app.domain.TaskPriorities
import com.equilibra.app.ui.task.TaskEditViewModel
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 3: "How do I order my priorities?" */
@AndroidEntryPoint
class PrioritiesFragment : Fragment() {
    // The view (and so the binding) is destroyed before the fragment, e.g. when you
    // switch tabs. It is set to null in onDestroyView so the old view can't leak.
    private var _binding: FragmentPrioritiesBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null

    private val viewModel: PrioritiesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPrioritiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val topThreeAdapter = TaskAdapter(TaskAdapter.Subtitle.QUADRANT, false, ::openTask, viewModel::moveTask)
        binding.recyclerTopThree.adapter = topThreeAdapter

        val quadrantGroups =
            mapOf(
                Quadrant.DO_FIRST to binding.quadrantDoFirst,
                Quadrant.SCHEDULE to binding.quadrantSchedule,
                Quadrant.DELEGATE to binding.quadrantDelegate,
                Quadrant.ELIMINATE to binding.quadrantEliminate,
            )
        val quadrantAdapters =
            quadrantGroups.mapValues { (quadrant, group) ->
                group.textTitle.setText(quadrant.titleRes)
                group.textSubtitle.setText(quadrant.hintRes)
                group.root.setCardBackgroundColor(MaterialColors.getColor(group.root, quadrant.colorAttr))
                createAdapter(group, TaskAdapter.Subtitle.STATUS, showMoveButtons = false)
            }

        val columnGroups =
            mapOf(
                TaskStatus.TODO to binding.columnTodo,
                TaskStatus.IN_PROGRESS to binding.columnInProgress,
                TaskStatus.DONE to binding.columnDone,
            )
        val columnAdapters =
            columnGroups.mapValues { (status, group) ->
                group.textTitle.setText(status.labelRes)
                createAdapter(group, TaskAdapter.Subtitle.QUADRANT, showMoveButtons = true)
            }

        // Matrix / Kanban switch
        binding.toggleView.check(if (viewModel.showKanban) R.id.button_kanban else R.id.button_matrix)
        showSelectedView()
        binding.toggleView.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                viewModel.showKanban = checkedId == R.id.button_kanban
                showSelectedView()
            }
        }

        binding.fabAddTask.setOnClickListener { openEditor(TaskEditViewModel.NEW_TASK_ID) }
        binding.buttonEditMethod.setOnClickListener { showMethodDialog() }
        binding.buttonShare.setOnClickListener { sharePriorities() }

        // Collect the data only while the screen is visible (STARTED or later).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    topThreeAdapter.submitList(state.topThree)
                    binding.textTopThreeEmpty.isVisible = state.topThree.isEmpty()

                    quadrantAdapters.forEach { (quadrant, adapter) ->
                        showTasks(quadrantGroups.getValue(quadrant), adapter, state.quadrants[quadrant].orEmpty())
                    }
                    columnAdapters.forEach { (status, adapter) ->
                        val group = columnGroups.getValue(status)
                        val tasks = state.columns[status].orEmpty()
                        showTasks(group, adapter, tasks)
                        group.textSubtitle.text = resources.getQuantityString(R.plurals.task_count, tasks.size, tasks.size)
                    }

                    binding.textMethod.text = state.method.ifBlank { getString(R.string.my_method_empty) }
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

    private fun showSelectedView() {
        binding.layoutMatrix.isVisible = !viewModel.showKanban
        binding.layoutKanban.isVisible = viewModel.showKanban
    }

    private fun createAdapter(
        group: ViewTaskGroupBinding,
        subtitle: TaskAdapter.Subtitle,
        showMoveButtons: Boolean,
    ): TaskAdapter {
        val adapter = TaskAdapter(subtitle, showMoveButtons, ::openTask, viewModel::moveTask)
        group.recyclerTasks.adapter = adapter
        return adapter
    }

    private fun showTasks(
        group: ViewTaskGroupBinding,
        adapter: TaskAdapter,
        tasks: List<TaskEntity>,
    ) {
        adapter.submitList(tasks)
        group.textEmpty.isVisible = tasks.isEmpty()
    }

    private fun openTask(task: TaskEntity) = openEditor(task.id)

    private fun openEditor(taskId: Long) {
        findNavController().navigate(
            R.id.action_priorities_to_taskEdit,
            bundleOf(TaskEditViewModel.ARG_TASK_ID to taskId),
        )
    }

    /**
     * Extra feature: Share Intent. ACTION_SEND with plain text lets the user pick any
     * app that can receive text (Viber, Messenger, email...). No permission is needed,
     * because our app only hands the text over; the chosen app does the sending.
     */
    private fun sharePriorities() {
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject))
                putExtra(Intent.EXTRA_TEXT, buildShareText(viewModel.uiState.value))
            }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
    }

    /** The current priorities as plain text: Top 3, the Kanban columns and My method. */
    private fun buildShareText(state: PrioritiesUiState): String =
        buildString {
            appendLine(getString(R.string.share_subject))
            appendLine()
            appendLine(getString(R.string.top_three_title))
            if (state.topThree.isEmpty()) appendLine(getString(R.string.share_none))
            state.topThree.forEachIndexed { index, task -> appendLine("${index + 1}. ${task.title}") }

            TaskStatus.entries.forEach { status ->
                val tasks = state.columns[status].orEmpty()
                appendLine()
                appendLine(getString(status.labelRes))
                if (tasks.isEmpty()) appendLine(getString(R.string.share_none))
                tasks.forEach { task ->
                    appendLine("• ${task.title} (${getString(TaskPriorities.quadrantOf(task).titleRes)})")
                }
            }

            if (state.method.isNotBlank()) {
                appendLine()
                appendLine(getString(R.string.my_method_title))
                appendLine(state.method)
            }
        }.trimEnd()

    private fun showMethodDialog() {
        val input = DialogTextInputBinding.inflate(layoutInflater)
        input.layoutInput.hint = getString(R.string.my_method_hint)
        input.editInput.setText(viewModel.uiState.value.method)

        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.my_method_title)
                .setView(input.root)
                .setPositiveButton(R.string.action_save) { _, _ ->
                    viewModel.saveMethod(input.editInput.text.toString())
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
