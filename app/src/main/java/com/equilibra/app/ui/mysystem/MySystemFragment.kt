package com.equilibra.app.ui.mysystem

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.equilibra.app.R
import com.equilibra.app.databinding.FragmentMySystemBinding
import com.equilibra.app.domain.SystemSummary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** Section 5: "What does my final balance system look like?" (basic version: summary numbers + Share). */
@AndroidEntryPoint
class MySystemFragment : Fragment() {
    private var _binding: FragmentMySystemBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MySystemViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMySystemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonShare.setOnClickListener { shareSummary() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.summary.collect { summary ->
                    binding.textPriorities.text = prioritiesText(summary)
                    binding.textJournal.text = journalText(summary)
                    binding.textActivities.text = activitiesText(summary)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun prioritiesText(summary: SystemSummary): String =
        getString(R.string.summary_tasks_format, summary.tasksDone, summary.tasksTotal)

    private fun journalText(summary: SystemSummary): String =
        listOf(
            getString(R.string.summary_journal_days_format, summary.journalDays),
            getString(
                R.string.summary_completion_format,
                summary.averageCompletion?.let { getString(R.string.summary_percent_format, it) } ?: noData(),
            ),
            getString(R.string.summary_energy_format, summary.averageEnergy?.let(::outOfFive) ?: noData()),
            getString(R.string.summary_motivation_format, summary.averageMotivation?.let(::outOfFive) ?: noData()),
        ).joinToString("\n")

    private fun activitiesText(summary: SystemSummary): String =
        getString(R.string.summary_activities_format, summary.activitiesCount, summary.activityDays)

    /** An average with one decimal, e.g. "3.4 / 5". */
    private fun outOfFive(value: Double): String = getString(R.string.summary_out_of_five_format, value)

    private fun noData(): String = getString(R.string.share_none)

    /** Extra feature reused: Share Intent with the summary as plain text (see PrioritiesFragment). */
    private fun shareSummary() {
        val summary = viewModel.summary.value
        val text =
            listOf(
                getString(R.string.summary_share_subject),
                getString(R.string.nav_priorities) + "\n" + prioritiesText(summary),
                getString(R.string.nav_journal) + "\n" + journalText(summary),
                getString(R.string.nav_habits) + "\n" + activitiesText(summary),
            ).joinToString("\n\n")
        val shareIntent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.summary_share_subject))
                putExtra(Intent.EXTRA_TEXT, text)
            }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
    }
}
