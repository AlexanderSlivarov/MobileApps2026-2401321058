package com.equilibra.app.ui.selfassessment

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.WheelKind
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentWheelEditBinding
import com.equilibra.app.databinding.ItemWheelSliderBinding
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.sectorLabel
import com.equilibra.app.ui.common.toDisplayString
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Rate the balance wheel (1-5 per sector) for a day, or edit / delete a saved rating. */
@AndroidEntryPoint
class WheelEditFragment : Fragment() {
    private var _binding: FragmentWheelEditBinding? = null
    private val binding get() = _binding!!

    private var dialog: Dialog? = null

    /** One slider per sector, created in code from the WheelSector list. */
    private val sliders = mutableMapOf<WheelSector, Slider>()

    private val viewModel: WheelEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWheelEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonDelete.isVisible = !viewModel.isNewAssessment
        addSliders()

        // The ViewModel keeps every value, so the form is filled from it after a rotation too.
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.load()
            showValues()
            binding.chipGroupKind.setOnCheckedStateChangeListener { _, checkedIds ->
                viewModel.kind = kindOfChip(checkedIds.firstOrNull())
            }
        }

        binding.buttonDate.setOnClickListener { pickDate() }
        binding.buttonSave.setOnClickListener { save() }
        binding.buttonDelete.setOnClickListener { confirmDelete() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // An open dialog holds the activity, so close it together with the view.
        dialog?.dismiss()
        dialog = null
        sliders.clear()
        _binding = null
    }

    private fun addSliders() {
        WheelSector.entries.forEach { sector ->
            val row = ItemWheelSliderBinding.inflate(layoutInflater, binding.layoutSliders, true)
            val color = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), sector.colorRes))
            row.textSector.text = requireContext().sectorLabel(sector)
            row.textSector.setTextColor(color)
            row.slider.contentDescription = requireContext().sectorLabel(sector)
            row.slider.trackActiveTintList = color
            row.slider.thumbTintList = color
            // All rows come from the same layout, so they share view ids. Android would restore
            // every slider with the same saved value, so their state is kept in the ViewModel instead.
            row.slider.isSaveEnabled = false
            row.slider.addOnChangeListener { _, value, _ -> viewModel.scores[sector] = value.toInt() }
            sliders[sector] = row.slider
        }
    }

    private fun showValues() {
        binding.buttonDate.text = viewModel.date.toDisplayString()
        binding.chipGroupKind.check(chipOfKind(viewModel.kind))
        sliders.forEach { (sector, slider) -> slider.value = viewModel.scores.getValue(sector).toFloat() }
    }

    private fun chipOfKind(kind: WheelKind): Int =
        when (kind) {
            WheelKind.START -> R.id.chip_start
            WheelKind.CHECK_IN -> R.id.chip_check_in
            WheelKind.FINAL -> R.id.chip_final
        }

    private fun kindOfChip(chipId: Int?): WheelKind =
        when (chipId) {
            R.id.chip_start -> WheelKind.START
            R.id.chip_final -> WheelKind.FINAL
            else -> WheelKind.CHECK_IN
        }

    private fun pickDate() {
        val current = viewModel.date
        // DatePickerDialog counts months from 0, LocalDate from 1.
        dialog =
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    viewModel.date = LocalDate.of(year, month + 1, day)
                    binding.buttonDate.text = viewModel.date.toDisplayString()
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth,
            ).apply { show() }
    }

    private fun save() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.save()) {
                findNavController().navigateUp()
            } else {
                Snackbar.make(binding.root, R.string.error_wheel_kind_taken, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmDelete() {
        dialog =
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.wheel_delete_confirm)
                .setPositiveButton(R.string.action_delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.delete()
                        findNavController().navigateUp()
                    }
                }.setNegativeButton(R.string.action_cancel, null)
                .show()
    }
}
