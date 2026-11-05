package com.equilibra.app.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.databinding.ItemFindingBinding

/** The UI text for each finding type. */
@get:StringRes
val FindingType.labelRes: Int
    get() =
        when (this) {
            FindingType.PROBLEM -> R.string.finding_problem
            FindingType.HABIT -> R.string.finding_habit
            FindingType.NEED -> R.string.finding_need
        }

/** Shows findings as cards. Tapping a card opens it for editing. */
class FindingAdapter(
    private val onClick: (FindingEntity) -> Unit,
) : ListAdapter<FindingEntity, FindingAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemFindingBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemFindingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val finding = getItem(position)
        holder.binding.textType.setText(finding.type.labelRes)
        holder.binding.textFinding.text = finding.text
        holder.binding.root.setOnClickListener { onClick(finding) }
    }

    private companion object {
        val DIFF =
            object : DiffUtil.ItemCallback<FindingEntity>() {
                override fun areItemsTheSame(
                    oldItem: FindingEntity,
                    newItem: FindingEntity,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: FindingEntity,
                    newItem: FindingEntity,
                ) = oldItem == newItem
            }
    }
}
