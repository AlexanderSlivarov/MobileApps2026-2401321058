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

/** The UI text for each result. */
@get:StringRes
val DecisionResult.labelRes: Int
    get() =
        when (this) {
            DecisionResult.PENDING -> R.string.result_pending
            DecisionResult.WORKED -> R.string.result_worked
            DecisionResult.DID_NOT_WORK -> R.string.result_did_not_work
        }

/** Shows decisions as cards. Tapping a card opens it for editing. */
class DecisionAdapter(
    private val onClick: (DecisionEntity) -> Unit,
) : ListAdapter<DecisionEntity, DecisionAdapter.ViewHolder>(DIFF) {
    class ViewHolder(
        val binding: ItemDecisionBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = ViewHolder(ItemDecisionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val decision = getItem(position)
        val binding = holder.binding
        val context = binding.root.context

        binding.textDecision.text = decision.text
        binding.textResult.text =
            context.getString(
                R.string.decision_result_format,
                context.getString(decision.result.labelRes),
                decision.startDate.toDisplayString(),
            )
        // My time lab: the SMART goal is what the experiment tests.
        binding.textGoal.text = context.getString(R.string.smart_goal_format, decision.smartGoal)
        binding.textGoal.isVisible = decision.smartGoal.isNotBlank()
        binding.textNote.text = decision.note
        binding.textNote.isVisible = decision.note.isNotBlank()
        binding.root.setOnClickListener { onClick(decision) }
    }

    private companion object {
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
