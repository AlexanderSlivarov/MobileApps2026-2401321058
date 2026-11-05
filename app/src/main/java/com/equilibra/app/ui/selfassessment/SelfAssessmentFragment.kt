package com.equilibra.app.ui.selfassessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.databinding.FragmentSelfAssessmentBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 1: "Who am I and how do I manage my time?" (basic version: the self-assessment form). */
@AndroidEntryPoint
class SelfAssessmentFragment : Fragment() {
    private var _binding: FragmentSelfAssessmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SelfAssessmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSelfAssessmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Load the saved answers only the first time. After a rotation or coming back
        // from another tab, Android restores the fields by itself, keeping what was typed.
        if (savedInstanceState == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                fillForm(viewModel.loadProfile())
            }
        }

        binding.buttonSave.setOnClickListener { save() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fillForm(profile: ProfileEntity) {
        binding.editPersonality.setText(profile.personalityType)
        binding.editSuperpower1.setText(profile.superpower1)
        binding.editSuperpower2.setText(profile.superpower2)
        binding.editSuperpower3.setText(profile.superpower3)
        binding.editImprove.setText(profile.wantToImprove)
        binding.editMoreTime.setText(profile.moreTimeFor)
        binding.editHowIPlan.setText(profile.howIPlan)
        binding.editStress.setText(profile.stressCoping)
    }

    private fun save() {
        val form =
            SelfAssessmentForm(
                personalityType = binding.editPersonality.text.toString(),
                superpower1 = binding.editSuperpower1.text.toString(),
                superpower2 = binding.editSuperpower2.text.toString(),
                superpower3 = binding.editSuperpower3.text.toString(),
                wantToImprove = binding.editImprove.text.toString(),
                moreTimeFor = binding.editMoreTime.text.toString(),
                howIPlan = binding.editHowIPlan.text.toString(),
                stressCoping = binding.editStress.text.toString(),
            )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(form)
            Snackbar.make(binding.root, R.string.self_saved, Snackbar.LENGTH_SHORT).show()
        }
    }
}
