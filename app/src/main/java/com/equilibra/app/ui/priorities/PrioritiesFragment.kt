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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.databinding.DialogPriorityBinding
import com.equilibra.app.databinding.FragmentPrioritiesBinding
import com.equilibra.app.domain.Eisenhower
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.equilibra.app.domain.EisenhowerGroup
import com.equilibra.app.ui.common.toDisplayString
import java.time.LocalDate
import android.app.DatePickerDialog

/** Section 3: "How do I order my priorities?" */
@AndroidEntryPoint
class PrioritiesFragment : Fragment() {
    // The view (and so the binding) is destroyed before the fragment, e.g. when you
    // switch tabs. It is set to null in onDestroyView so the old view can't leak.
    private var _binding: FragmentPrioritiesBinding? = null
    private val binding get() = _binding!!

    private var dialog: AlertDialog? = null
    private var datePicker: DatePickerDialog? = null

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
        binding.empty.imageIcon.setImageResource(R.drawable.ic_list)
        binding.empty.textTitle.setText(R.string.priorities_empty)

        // One section per group (I, II, III, IV), each with its own list. Dragging and the arrows
        // work inside one list, so a priority can't leave its group: "Do now" stays on top.
        val sections =
            mapOf(
                EisenhowerGroup.I to binding.groupI,
                EisenhowerGroup.II to binding.groupIi,
                EisenhowerGroup.III to binding.groupIii,
                EisenhowerGroup.IV to binding.groupIv,
            )
        val adapters =
            sections.mapValues { (group, section) ->
                val color = ContextCompat.getColor(requireContext(), group.colorRes)
                section.viewDot.backgroundTintList = ColorStateList.valueOf(color)
                section.textGroupTitle.setText(group.labelRes)
                section.textGroupTitle.setTextColor(color)
                val adapter = PriorityAdapter(showOrder = true, onClick = ::showTaskDialog, onMove = viewModel::move)
                section.recyclerGroup.adapter = adapter
                attachDrag(section.recyclerGroup, adapter)
                adapter
            }
        // "I consciously didn't start": same cards, without numbers and up/down buttons.
        val notStartedAdapter = PriorityAdapter(showOrder = false, onClick = ::showTaskDialog, onMove = viewModel::move)
        binding.recyclerNotStarted.adapter = notStartedAdapter

        binding.fabAddTask.setOnClickListener { showTaskDialog(null) }
        binding.buttonShare.setOnClickListener { sharePriorities() }

        // Collect the data only while the screen is visible (STARTED or later).
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Each card's number and up/down buttons depend on its place, so after the
                    // lists change all visible cards are drawn again. Numbers go on across groups.
                    var before = 0
                    sections.forEach { (group, section) ->
                        val inGroup = state.tasks.filter { Eisenhower.groupOf(it) == group }
                        val adapter = adapters.getValue(group)
                        adapter.numberOffset = before
                        adapter.submitList(inGroup) { adapter.notifyItemRangeChanged(0, adapter.itemCount) }
                        section.root.isVisible = inGroup.isNotEmpty()
                        before += inGroup.size
                    }
                    binding.empty.root.isVisible = state.tasks.isEmpty()
                    notStartedAdapter.submitList(state.notStarted)
                    // "Postponed" shows only when something is postponed.
                    binding.textNotStartedTitle.isVisible = state.notStarted.isNotEmpty()
                }
            }
        }
    }

    /**
     * Long-press a priority and drag it up or down inside its group (the arrows still work).
     * While dragging, the list on screen is changed; when the card is let go, the new order is saved.
     */
    private fun attachDrag(
        recyclerView: RecyclerView,
        adapter: PriorityAdapter,
    ) {
        val callback =
            object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    val list = adapter.currentList.toMutableList()
                    list.add(target.adapterPosition, list.removeAt(viewHolder.adapterPosition))
                    adapter.submitList(list)
                    return true
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int,
                ) = Unit

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ) {
                    super.clearView(recyclerView, viewHolder)
                    viewModel.saveOrder(adapter.currentList)
                }
            }
        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // An open dialog holds the activity, so close it together with the view.
        dialog?.dismiss()
        dialog = null
        datePicker?.dismiss()
        datePicker = null
        _binding = null
    }

    /** Add a priority ([task] = null) or edit / delete an existing one, in a plain dialog. */
    private fun showTaskDialog(task: TaskEntity?) {
        val form = DialogPriorityBinding.inflate(layoutInflater)
        form.editTitle.setText(task?.title)
        form.checkImportant.isChecked = task?.isImportant ?: false
        form.checkUrgent.isChecked = task?.isUrgent ?: false
        form.checkNotStarted.isChecked = task?.isNotStarted ?: false
        // The chosen date lives in this variable while the dialog is open.
        var date = task?.date
        fun showDate() {
            form.chipDate.text = date?.toDisplayString() ?: getString(R.string.priority_no_date)
            form.chipDate.isCloseIconVisible = date != null
        }
        showDate()
        form.chipDate.setOnClickListener {
            val shownDate = date ?: LocalDate.now()
            // DatePickerDialog counts months from 0, LocalDate from 1.
            datePicker =
                DatePickerDialog(
                    requireContext(),
                    { _, year, month, day ->
                        date = LocalDate.of(year, month + 1, day)
                        showDate()
                    },
                    shownDate.year,
                    shownDate.monthValue - 1,
                    shownDate.dayOfMonth,
                ).apply { show() }
        }
        form.chipDate.setOnCloseIconClickListener {
            date = null
            showDate()
        }

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
                        date = date,
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

    /** My priorities in order with their group, and the postponed ones, as plain text. */
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
        }.trimEnd()
}
