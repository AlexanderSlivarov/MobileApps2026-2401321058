package com.equilibra.app.ui.mysystem

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.equilibra.app.R
import com.equilibra.app.databinding.FragmentPlaceholderBinding

/** Section 5: final balance system dashboard. Placeholder until its real screen is built. */
class MySystemFragment : Fragment(R.layout.fragment_placeholder) {
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // The binding is a local variable, so it can't outlive the view (no leak).
        val binding = FragmentPlaceholderBinding.bind(view)
        binding.textQuestion.setText(R.string.question_my_system)
    }
}
