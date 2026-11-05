package com.equilibra.app.ui.priorities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.databinding.ItemPriorityBinding

/**
 * Shows my priorities as numbered cards (1 = most important) with up/down buttons.
 * ListAdapter + DiffUtil compare the old and new list and only redraw what changed.
 */
class PriorityAdapter(
    private val onClick: (TaskEntity) -> Unit,
    private val onMove: (TaskEntity, Boolean) -> Unit,
) : ListAdapter<TaskEntity, PriorityAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemPriorityBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemPriorityBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val task = getItem(position)
        val binding = holder.binding
        binding.textNumber.text = (position + 1).toString()
        binding.textTitle.text = task.title
        // The first can't go up and the last can't go down.
        binding.buttonUp.isEnabled = position > 0
        binding.buttonDown.isEnabled = position < itemCount - 1
        binding.buttonUp.setOnClickListener { onMove(task, true) }
        binding.buttonDown.setOnClickListener { onMove(task, false) }
        binding.root.setOnClickListener { onClick(task) }
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
