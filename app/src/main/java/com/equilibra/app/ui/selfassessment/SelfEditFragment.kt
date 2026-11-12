package com.equilibra.app.ui.selfassessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.equilibra.app.R
import com.equilibra.app.databinding.FragmentSelfEditBinding
import com.equilibra.app.databinding.ItemSelfQuestionBinding
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** "Who am I?" or "How do I manage my time?": 3 helper questions and a field for my own words. */
@AndroidEntryPoint
class SelfEditFragment : Fragment() {
    private var _binding: FragmentSelfEditBinding? = null
    private val binding get() = _binding!!

    /** For each question of this half: its chip group and the ids of its 3 option chips (created in code). */
    private val questionViews = mutableListOf<Pair<ChipGroup, List<Int>>>()

    private val viewModel: SelfEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSelfEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.textPartTitle.setText(
            if (viewModel.part == SelfEditViewModel.PART_WHO_AM_I) R.string.self_who_am_i else R.string.self_manage_time,
        )
        addQuestions()

        // Load the saved answers only the first time. After a language switch the chips are
        // checked from the ViewModel, and Android restores the typed words by itself.
        if (savedInstanceState == null && !viewModel.formLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                val profile = viewModel.loadProfile()
                val note = if (viewModel.part == SelfEditViewModel.PART_WHO_AM_I) profile.whoAmINote else profile.manageTimeNote
                binding.editNote.setText(note)
                checkChosenOptions()
                viewModel.formLoaded = true
            }
        } else {
            checkChosenOptions()
        }

        binding.buttonSave.setOnClickListener { save() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        questionViews.clear()
        _binding = null
    }

    /** One row per question of this half; a tap on an option is kept in the ViewModel until "Save". */
    private fun addQuestions() {
        viewModel.questions.forEach { questionIndex ->
            val texts = SELF_QUESTIONS[questionIndex]
            val row = ItemSelfQuestionBinding.inflate(layoutInflater, binding.layoutQuestions, true)
            row.textQuestion.setText(texts.question)
            val chipIds =
                texts.options.map { optionText ->
                    val chip = layoutInflater.inflate(R.layout.item_self_option, row.chipGroupOptions, false) as Chip
                    chip.id = View.generateViewId()
                    chip.setText(optionText)
                    // The chips get new ids every time, so the ViewModel restores the choice, not Android.
                    chip.isSaveEnabled = false
                    row.chipGroupOptions.addView(chip)
                    chip.id
                }
            row.chipGroupOptions.setOnCheckedStateChangeListener { _, checkedIds ->
                val option = chipIds.indexOf(checkedIds.firstOrNull())
                if (option >= 0) viewModel.choose(questionIndex, option)
            }
            questionViews.add(row.chipGroupOptions to chipIds)
        }
    }

    private fun checkChosenOptions() {
        viewModel.questions.zip(questionViews).forEach { (questionIndex, views) ->
            val (group, chipIds) = views
            SelfAssessment.answerAt(viewModel.draftAnswers, questionIndex)?.let { option -> group.check(chipIds[option]) }
        }
    }

    private fun save() {
        val note = binding.editNote.text.toString()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(note)
            findNavController().navigateUp()
        }
    }
}
