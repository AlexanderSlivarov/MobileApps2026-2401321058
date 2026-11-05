package com.equilibra.app.ui.priorities

import android.content.Intent
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
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.databinding.DialogPriorityBinding
import com.equilibra.app.databinding.DialogTextInputBinding
import com.equilibra.app.databinding.FragmentPrioritiesBinding
import com.equilibra.app.domain.Eisenhower
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

        val adapter = PriorityAdapter(showOrder = true, onClick = ::showTaskDialog, onMove = viewModel::move)
        binding.recyclerPriorities.adapter = adapter
        // "I consciously didn't start": same cards, without numbers and up/down buttons.
        val notStartedAdapter = PriorityAdapter(showOrder = false, onClick = ::showTaskDialog, onMove = viewModel::move)
        binding.recyclerNotStarted.adapter = notStartedAdapter

        binding.fabAddTask.setOnClickListener { showTaskDialog(null) }
        binding.buttonEditMethod.setOnClickListener { showMethodDialog() }
        binding.buttonShare.setOnClickListener { sharePriorities() }

        // Collect the data only while the screen is visible (STARTED or later).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Each card's number and up/down buttons depend on its place in the list,
                    // so after the list changes all visible cards are drawn again.
                    adapter.submitList(state.tasks) { adapter.notifyItemRangeChanged(0, adapter.itemCount) }
                    binding.textEmpty.isVisible = state.tasks.isEmpty()
                    notStartedAdapter.submitList(state.notStarted)
                    binding.textNotStartedEmpty.isVisible = state.notStarted.isEmpty()
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

    /** Add a priority ([task] = null) or edit / delete an existing one, in a plain dialog. */
    private fun showTaskDialog(task: TaskEntity?) {
        val form = DialogPriorityBinding.inflate(layoutInflater)
        form.editTitle.setText(task?.title)
        form.checkImportant.isChecked = task?.isImportant ?: false
        form.checkUrgent.isChecked = task?.isUrgent ?: false
        form.checkNotStarted.isChecked = task?.isNotStarted ?: false

        val builder =
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(if (task == null) R.string.add_task else R.string.priority_edit)
                .setView(form.root)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
        if (task != null) builder.setNeutralButton(R.string.action_delete) { _, _ -> confirmDelete(task) }
        val shown = builder.show()

        // Set the Save click here, not in setPositiveButton, so the dialog stays open on an empty title.
        shown.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = form.editTitle.text.toString()
            if (title.isBlank()) {
                form.layoutTitle.error = getString(R.string.error_title_required)
            } else {
                viewModel.save(
                    task,
                    PriorityForm(
                        title = title,
                        isImportant = form.checkImportant.isChecked,
                        isUrgent = form.checkUrgent.isChecked,
                        isNotStarted = form.checkNotStarted.isChecked,
                    ),
                )
                shown.dismiss()
            }
        }
        dialog = shown
    }

    private fun confirmDelete(task: TaskEntity) {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.task_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.delete(task) }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
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

    /** My priorities in order with their group, what I didn't start, and My method, as plain text. */
    private fun buildShareText(state: PrioritiesUiState): String =
        buildString {
            appendLine(getString(R.string.share_subject))
            appendLine()
            if (state.tasks.isEmpty()) appendLine(getString(R.string.share_none))
            state.tasks.forEachIndexed { index, task ->
                appendLine("${index + 1}. ${task.title} (${getString(Eisenhower.groupOf(task).labelRes)})")
            }

            if (state.notStarted.isNotEmpty()) {
                appendLine()
                appendLine(getString(R.string.not_started_title))
                state.notStarted.forEach { appendLine("• ${it.title}") }
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
