package com.equilibra.app.ui.habits

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.HabitPlan
import com.equilibra.app.data.local.entity.NeedArea
import com.equilibra.app.databinding.FragmentFindingEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Add a new finding or edit / delete one. For a problem, the 5 Whys lead to a deeper cause. */
@AndroidEntryPoint
class FindingEditFragment : Fragment() {
    private var _binding: FragmentFindingEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    private val viewModel: FindingEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFindingEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewFinding

        // Load the finding into the form only the first time. After a rotation or a language
        // switch Android restores the fields and the chosen type by itself.
        if (savedInstanceState == null && !viewModel.formLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadFinding()?.let { fillForm(it) }
                viewModel.formLoaded = true
            }
        }

        binding.editFinding.doAfterTextChanged { binding.layoutFinding.error = null }
        binding.chipGroupType.setOnCheckedStateChangeListener { _, _ -> showChosenType() }
        binding.buttonSave.setOnClickListener { save() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    /** Called after Android restored the fields (also the chosen type), so the title and the 5 Whys match it. */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        showChosenType()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // An open dialog holds the activity, so close it together with the view.
        dialog?.dismiss()
        dialog = null
        _binding = null
    }

    /** The five "Why?" fields, in order. */
    private fun whyFields(): List<TextInputEditText> =
        listOf(binding.editWhy1, binding.editWhy2, binding.editWhy3, binding.editWhy4, binding.editWhy5)

    private fun fillForm(finding: FindingEntity) {
        binding.chipGroupType.check(chipOf(finding.type))
        binding.editFinding.setText(finding.text)
        whyFields().zip(finding.whys).forEach { (field, answer) -> field.setText(answer) }
        finding.habitPlan?.let { binding.chipGroupPlan.check(PLAN_CHIPS.getValue(it)) }
        finding.needArea?.let { binding.chipGroupArea.check(AREA_CHIPS.getValue(it)) }
        showChosenType()
    }

    /**
     * The toolbar title and the question in the field follow the chosen type ("Problem", "Habit" or "Need"), and the
     * extra card matches it: the 5 Whys for a problem (from the symptom to a cause), keep /
     * change / stop for a habit, and the area of life for a need.
     */
    private fun showChosenType() {
        val type = typeOf(binding.chipGroupType.checkedChipId)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(type.labelRes)
        binding.layoutFinding.hint = getString(type.hintRes)
        binding.cardWhys.isVisible = type == FindingType.PROBLEM
        binding.cardHabit.isVisible = type == FindingType.HABIT
        binding.cardNeed.isVisible = type == FindingType.NEED
    }

    private fun chipOf(type: FindingType): Int =
        when (type) {
            FindingType.PROBLEM -> R.id.chip_problem
            FindingType.HABIT -> R.id.chip_habit
            FindingType.NEED -> R.id.chip_need
        }

    private fun typeOf(chipId: Int): FindingType =
        when (chipId) {
            R.id.chip_habit -> FindingType.HABIT
            R.id.chip_need -> FindingType.NEED
            else -> FindingType.PROBLEM
        }

    private fun save() {
        val text = binding.editFinding.text.toString()
        if (text.isBlank()) {
            binding.layoutFinding.error = getString(R.string.error_text_required)
            return
        }
        val type = typeOf(binding.chipGroupType.checkedChipId)
        val whys = whyFields().map { it.text.toString() }
        // A chip id that isn't in the map (no chip checked) gives null: "not chosen".
        val plan = PLAN_CHIPS.entries.firstOrNull { it.value == binding.chipGroupPlan.checkedChipId }?.key
        val area = AREA_CHIPS.entries.firstOrNull { it.value == binding.chipGroupArea.checkedChipId }?.key
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(text, type, whys, plan, area)
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.finding_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }

    private companion object {
        /** Each choice and its chip in fragment_finding_edit.xml. */
        val PLAN_CHIPS = mapOf(HabitPlan.KEEP to R.id.chip_keep, HabitPlan.CHANGE to R.id.chip_change, HabitPlan.STOP to R.id.chip_stop)
        val AREA_CHIPS =
            mapOf(
                NeedArea.STUDY to R.id.chip_study,
                NeedArea.REST to R.id.chip_rest,
                NeedArea.HEALTH to R.id.chip_health,
                NeedArea.PEOPLE to R.id.chip_people,
            )
    }
}
