package com.equilibra.app.ui.journal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.equilibra.app.R
import com.equilibra.app.databinding.FragmentPlaceholderBinding

/** Section 4: daily journal, decisions and results. Placeholder until its real screen is built. */
class JournalFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // The binding is a local variable, so it can't outlive the view (no leak).
        val binding = FragmentPlaceholderBinding.bind(view)
        binding.textQuestion.setText(R.string.question_journal)
    }
}
