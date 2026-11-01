package com.equilibra.app.ui.priorities

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.databinding.ItemTaskBinding
import com.equilibra.app.domain.TaskPriorities

/**
 * Shows a list of task cards. ListAdapter + DiffUtil compare the old and new list
 * and only redraw the cards that actually changed.
 */
class TaskAdapter(
    private val subtitle: Subtitle,
    private val showMoveButtons: Boolean,
    private val onClick: (TaskEntity) -> Unit,
    private val onMove: (TaskEntity, TaskStatus) -> Unit,
) : ListAdapter<TaskEntity, TaskAdapter.ViewHolder>(DIFF) {
    /** What the second line of a card shows. */
    enum class Subtitle { QUADRANT, STATUS }

    class ViewHolder(
        val binding: ItemTaskBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val task = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.textTitle.text = task.title
        binding.textTitle.paintFlags =
            if (task.status == TaskStatus.DONE) {
                binding.textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

        binding.textSubtitle.text =
            when (subtitle) {
                Subtitle.QUADRANT -> context.getString(TaskPriorities.quadrantOf(task).titleRes)
                Subtitle.STATUS -> context.getString(task.status.labelRes)
            }
        binding.imageTopThree.isVisible = task.isTopThree
        binding.root.setOnClickListener { onClick(task) }

        val previous = TaskPriorities.previousStatus(task.status)
        val next = TaskPriorities.nextStatus(task.status)
        binding.buttonMoveBack.isVisible = showMoveButtons && previous != null
        binding.buttonMoveForward.isVisible = showMoveButtons && next != null
        binding.buttonMoveBack.setOnClickListener { previous?.let { onMove(task, it) } }
        binding.buttonMoveForward.setOnClickListener { next?.let { onMove(task, it) } }
    }

    private companion object {
        val DIFF =
            object : DiffUtil.ItemCallback<TaskEntity>() {
                override fun areItemsTheSame(
                    oldItem: TaskEntity,
                    newItem: TaskEntity,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: TaskEntity,
                    newItem: TaskEntity,
                ) = oldItem == newItem
            }
    }
}
