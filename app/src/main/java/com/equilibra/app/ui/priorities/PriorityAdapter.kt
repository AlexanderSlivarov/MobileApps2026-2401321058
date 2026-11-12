package com.equilibra.app.ui.priorities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.databinding.ItemPriorityBinding
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.EisenhowerGroup

/** The UI text for each Eisenhower group: its number and the action from the lectures. */
@get:StringRes
val EisenhowerGroup.labelRes: Int
    get() =
        when (this) {
            EisenhowerGroup.I -> R.string.group_i
            EisenhowerGroup.II -> R.string.group_ii
            EisenhowerGroup.III -> R.string.group_iii
            EisenhowerGroup.IV -> R.string.group_iv
        }

/** Each group has its own color (a lighter one in the night theme, values-night). */
@get:ColorRes
val EisenhowerGroup.colorRes: Int
    get() =
        when (this) {
            EisenhowerGroup.I -> R.color.group_i
            EisenhowerGroup.II -> R.color.group_ii
            EisenhowerGroup.III -> R.color.group_iii
            EisenhowerGroup.IV -> R.color.group_iv
        }

/**
 * Shows priorities as cards with their Eisenhower group. With [showOrder] they are numbered
 * (1 = most important) and have up/down buttons; without it (the "didn't start" list) they don't.
 * ListAdapter + DiffUtil compare the old and new list and only redraw what changed.
 */
class PriorityAdapter(
    private val showOrder: Boolean,
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
        binding.textTitle.text = task.title
        binding.textGroup.setText(Eisenhower.groupOf(task).labelRes)

        binding.textNumber.isVisible = showOrder
        binding.buttonUp.isVisible = showOrder
        binding.buttonDown.isVisible = showOrder
        binding.textNumber.text = (position + 1).toString()
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
