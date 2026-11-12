package com.equilibra.app.ui.selfassessment

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.databinding.FragmentWheelEditBinding
import com.equilibra.app.databinding.ItemWheelSliderBinding
import com.equilibra.app.ui.common.colorRes
import com.equilibra.app.ui.common.labelRes
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Rate my state on the balance wheel: a slider 1-5 per indicator, then Save. */
@AndroidEntryPoint
class WheelEditFragment : Fragment() {
    private var _binding: FragmentWheelEditBinding? = null
    private val binding get() = _binding!!

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
        addSliders()

        // The ViewModel keeps every value, so the form is filled from it after a rotation too.
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.load()
            sliders.forEach { (sector, slider) -> slider.value = viewModel.scores.getValue(sector).toFloat() }
        }

        binding.buttonSave.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.save()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sliders.clear()
        _binding = null
    }

    private fun addSliders() {
        WheelSector.entries.forEach { sector ->
            val row = ItemWheelSliderBinding.inflate(layoutInflater, binding.layoutSliders, true)
            val color = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), sector.colorRes))
            row.textSector.text = requireContext().getString(sector.labelRes)
            row.textSector.setTextColor(color)
            row.slider.contentDescription = requireContext().getString(sector.labelRes)
            row.slider.trackActiveTintList = color
            row.slider.thumbTintList = color
            // All rows come from the same layout, so they share view ids. Android would restore
            // every slider with the same saved value, so their state is kept in the ViewModel instead.
            row.slider.isSaveEnabled = false
            row.slider.addOnChangeListener { _, value, _ -> viewModel.scores[sector] = value.toInt() }
            sliders[sector] = row.slider
        }
    }
}
