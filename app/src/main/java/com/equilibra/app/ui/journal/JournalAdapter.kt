package com.equilibra.app.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.JournalEntryEntity
import com.equilibra.app.databinding.ItemJournalEntryBinding
import com.equilibra.app.domain.JournalStats
import com.equilibra.app.ui.common.toDisplayString

/** Shows journal entries as cards. Tapping a card opens it for editing. */
class JournalAdapter(
    private val onClick: (JournalEntryEntity) -> Unit,
) : ListAdapter<JournalEntryEntity, JournalAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemJournalEntryBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemJournalEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val entry = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.textDate.text = entry.date.toDisplayString()

        val percent = JournalStats.completionPercent(entry.tasksDone, entry.tasksPlanned)
        binding.textTasks.text =
            if (percent == null) {
                context.getString(R.string.journal_tasks_none_planned, entry.tasksDone)
            } else {
                context.getString(R.string.journal_tasks_format, entry.tasksDone, entry.tasksPlanned, percent)
            }

        binding.textLevels.text = context.getString(R.string.journal_levels_format, entry.energy, entry.motivation)
        binding.textFlags.text =
            listOf(
                context.getString(if (entry.hadRest) R.string.journal_had_rest else R.string.journal_no_rest),
                context.getString(
                    if (entry.correctionNeeded) R.string.journal_correction_needed else R.string.journal_on_track,
                ),
            ).joinToString(" · ")

        binding.textNote.text = entry.note
        binding.textNote.isVisible = entry.note.isNotBlank()
        binding.root.setOnClickListener { onClick(entry) }
    }

    private companion object {
        val DIFF =
            object : DiffUtil.ItemCallback<JournalEntryEntity>() {
                override fun areItemsTheSame(
                    oldItem: JournalEntryEntity,
                    newItem: JournalEntryEntity,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: JournalEntryEntity,
                    newItem: JournalEntryEntity,
                ) = oldItem == newItem
            }
    }
}
