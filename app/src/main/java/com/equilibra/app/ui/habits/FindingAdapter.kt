package com.equilibra.app.ui.habits

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.HabitPlan
import com.equilibra.app.data.local.entity.NeedArea
import com.equilibra.app.databinding.ItemFindingBinding
import com.equilibra.app.domain.FiveWhys
import com.google.android.material.color.MaterialColors

/** The UI text for each finding type. */
@get:StringRes
val FindingType.labelRes: Int
    get() =
        when (this) {
            FindingType.PROBLEM -> R.string.finding_problem
            FindingType.HABIT -> R.string.finding_habit
            FindingType.NEED -> R.string.finding_need
        }

/** The question in the text field for each finding type. */
@get:StringRes
val FindingType.hintRes: Int
    get() =
        when (this) {
            FindingType.PROBLEM -> R.string.finding_hint_problem
            FindingType.HABIT -> R.string.finding_hint_habit
            FindingType.NEED -> R.string.finding_hint_need
        }

/** The color of each finding type (values/colors.xml, with a lighter one for the dark theme). */
@get:ColorRes
val FindingType.colorRes: Int
    get() =
        when (this) {
            FindingType.PROBLEM -> R.color.type_problem
            FindingType.HABIT -> R.color.type_habit
            FindingType.NEED -> R.color.type_need
        }

/** The UI text for what I do with a habit. */
@get:StringRes
val HabitPlan.labelRes: Int
    get() =
        when (this) {
            HabitPlan.KEEP -> R.string.habit_keep
            HabitPlan.CHANGE -> R.string.habit_change
            HabitPlan.STOP -> R.string.habit_stop
        }

/** The tag text with its sign: ✓ keep, ↻ change, ✕ stop. */
@get:StringRes
val HabitPlan.tagFormatRes: Int
    get() =
        when (this) {
            HabitPlan.KEEP -> R.string.tag_keep_format
            HabitPlan.CHANGE -> R.string.tag_change_format
            HabitPlan.STOP -> R.string.tag_stop_format
        }

/** The tag color: green to keep, amber to change, red to stop. */
@get:ColorRes
val HabitPlan.colorRes: Int
    get() =
        when (this) {
            HabitPlan.KEEP -> R.color.result_worked
            HabitPlan.CHANGE -> R.color.group_iii
            HabitPlan.STOP -> R.color.result_did_not_work
        }

/** The UI text for the area of a need. */
@get:StringRes
val NeedArea.labelRes: Int
    get() =
        when (this) {
            NeedArea.STUDY -> R.string.area_study
            NeedArea.REST -> R.string.area_rest
            NeedArea.HEALTH -> R.string.area_health
            NeedArea.PEOPLE -> R.string.area_people
        }

/** The icon for the area of a need. */
@get:DrawableRes
val NeedArea.iconRes: Int
    get() =
        when (this) {
            NeedArea.STUDY -> R.drawable.ic_area_study
            NeedArea.REST -> R.drawable.ic_area_rest
            NeedArea.HEALTH -> R.drawable.ic_area_health
            NeedArea.PEOPLE -> R.drawable.ic_area_people
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
        val binding = holder.binding
        val color = ContextCompat.getColor(binding.root.context, finding.type.colorRes)
        binding.viewDot.backgroundTintList = ColorStateList.valueOf(color)
        binding.textType.setText(finding.type.labelRes)
        binding.textType.setTextColor(color)
        binding.textFinding.text = finding.text
        // For a problem analysed with the 5 Whys, show the deepest answer reached in its own box.
        val rootCause = FiveWhys.rootCause(finding.whys)
        binding.layoutRootCause.isVisible = rootCause != null
        binding.textRootCause.text = rootCause
        showHabitPlan(binding, finding)
        showNeedArea(binding, finding)
        binding.root.setOnClickListener { onClick(finding) }
    }

    /** A habit: a colored tag, green "Keep it", amber "Change it" or red "Stop it". */
    private fun showHabitPlan(
        binding: ItemFindingBinding,
        finding: FindingEntity,
    ) {
        val plan = finding.habitPlan
        binding.textPlan.isVisible = plan != null
        if (plan == null) return
        val context = binding.root.context
        binding.textPlan.text = context.getString(plan.tagFormatRes, context.getString(plan.labelRes))
        binding.textPlan.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, plan.colorRes))
        // The page background color reads well on both the light and the dark tag color.
        binding.textPlan.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurface))
    }

    /** A need: the part of life it is for, as an icon and a word. */
    private fun showNeedArea(
        binding: ItemFindingBinding,
        finding: FindingEntity,
    ) {
        val area = finding.needArea
        binding.layoutArea.isVisible = area != null
        if (area == null) return
        binding.imageArea.setImageResource(area.iconRes)
        binding.textArea.setText(area.labelRes)
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
