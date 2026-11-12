package com.equilibra.app.ui.decisions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.DecisionResult
import com.equilibra.app.databinding.ItemDecisionBinding
import com.equilibra.app.ui.common.toDisplayString
import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import android.widget.TextView
import com.equilibra.app.databinding.ItemDayBinding
import com.equilibra.app.domain.DecisionDays
import java.time.LocalDate

/** The UI text for each result. */
@get:StringRes
val DecisionResult.labelRes: Int
    get() =
        when (this) {
            DecisionResult.PENDING -> R.string.result_pending
            DecisionResult.WORKED -> R.string.result_worked
            DecisionResult.DID_NOT_WORK -> R.string.result_did_not_work
        }

/** The tag sign for each result: ✓ worked, ✕ didn't work, … too early to tell. */
@get:StringRes
val DecisionResult.tagFormatRes: Int
    get() =
        when (this) {
            DecisionResult.WORKED -> R.string.tag_keep_format
            DecisionResult.DID_NOT_WORK -> R.string.tag_stop_format
            DecisionResult.PENDING -> R.string.tag_pending_format
        }

/** The color for each result (the same as on Balance). */
@get:ColorRes
val DecisionResult.colorRes: Int
    get() =
        when (this) {
            DecisionResult.WORKED -> R.color.result_worked
            DecisionResult.DID_NOT_WORK -> R.color.result_did_not_work
            DecisionResult.PENDING -> R.color.result_pending
        }

/** Shows decisions as cards. Tapping a card opens it for editing. */
class DecisionAdapter(
    private val onClick: (DecisionEntity) -> Unit,
    private val onToggleDay: (DecisionEntity, Int) -> Unit,
) : ListAdapter<DecisionEntity, DecisionAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemDecisionBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = ItemDecisionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // The 7 day circles are made once per card and only filled in onBindViewHolder.
        repeat(DecisionDays.DAYS) { ItemDayBinding.inflate(LayoutInflater.from(parent.context), binding.layoutDays, true) }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val decision = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.textDecision.text = decision.text
        // The result as a colored tag: green worked out, red didn't, gray still in progress.
        val result = decision.result
        binding.textResult.text = context.getString(result.tagFormatRes, context.getString(result.labelRes))
        binding.textResult.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, result.colorRes))
        binding.textResult.setTextColor(MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurface))

        // Which day of the 7 it is today, or when it starts / started.
        val today = LocalDate.now()
        val day = DecisionDays.dayNumber(decision.startDate, today)
        binding.textSince.text =
            when {
                day < 1 -> context.getString(R.string.decision_starts_format, decision.startDate.toDisplayString())
                day <= DecisionDays.DAYS -> context.getString(R.string.decision_day_format, day.toInt())
                else -> context.getString(R.string.decision_since_format, decision.startDate.toDisplayString())
            }
        showDays(binding, decision, today)
        binding.textKept.text = context.getString(R.string.decision_kept_format, DecisionDays.doneCount(decision.daysDone))

        binding.layoutChange.isVisible = decision.note.isNotBlank()
        binding.textChange.text = decision.note
        binding.root.setOnClickListener { onClick(decision) }
    }

    /** The 7 circles: filled when kept, outlined when not, faded for days that haven't come yet. */
    private fun showDays(
        binding: ItemDecisionBinding,
        decision: DecisionEntity,
        today: LocalDate,
    ) {
        val context = binding.root.context
        val kept = ContextCompat.getColor(context, R.color.result_worked)
        val notKept = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurfaceContainerHighest)
        val onKept = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurface)
        val onNotKept = MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorOnSurfaceVariant)
        for (index in 0 until DecisionDays.DAYS) {
            val circle = binding.layoutDays.getChildAt(index) as TextView
            val done = DecisionDays.isDone(decision.daysDone, index)
            val canMark = DecisionDays.canMark(index, decision.startDate, today)
            circle.text = (index + 1).toString()
            circle.backgroundTintList = ColorStateList.valueOf(if (done) kept else notKept)
            circle.setTextColor(if (done) onKept else onNotKept)
            circle.alpha = if (canMark) 1f else FUTURE_ALPHA
            circle.isEnabled = canMark
            circle.setOnClickListener { onToggleDay(decision, index) }
        }
    }

    private companion object {
        const val FUTURE_ALPHA = 0.4f

        val DIFF =
            object : DiffUtil.ItemCallback<DecisionEntity>() {
                override fun areItemsTheSame(
                    oldItem: DecisionEntity,
                    newItem: DecisionEntity,
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: DecisionEntity,
                    newItem: DecisionEntity,
                ) = oldItem == newItem
            }
    }
}
