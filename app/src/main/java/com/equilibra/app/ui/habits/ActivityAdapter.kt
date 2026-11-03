package com.equilibra.app.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DayActivityEntity
import com.equilibra.app.databinding.ItemActivityBinding
import com.equilibra.app.domain.ActivityTimes
import com.equilibra.app.ui.common.minuteOfDayToDisplayString

/** Shows the activities of a day as cards. Tapping a card opens it for editing. */
class ActivityAdapter(
    private val onClick: (DayActivityEntity) -> Unit,
) : ListAdapter<DayActivityEntity, ActivityAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemActivityBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val activity = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.textTitle.text = activity.title
        binding.textTime.text =
            context.getString(
                R.string.activity_time_format,
                minuteOfDayToDisplayString(activity.startMinute),
                minuteOfDayToDisplayString(activity.endMinute),
                context.formatDuration(ActivityTimes.durationMinutes(activity.startMinute, activity.endMinute)),
            )
        binding.textCategory.setText(activity.category.labelRes)
        binding.root.setOnClickListener { onClick(activity) }
    }

    private companion object {
        val DIFF =
            object : DiffUtil.ItemCallback<DayActivityEntity>() {
                override fun areItemsTheSame(
                    oldItem: DayActivityEntity,
                    newItem: DayActivityEntity,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: DayActivityEntity,
                    newItem: DayActivityEntity,
                ) = oldItem == newItem
            }
    }
}
