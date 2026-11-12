package com.equilibra.app.ui.common

import android.content.Context
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.domain.SelfAssessment

/**
 * The texts of one self-assessment question: the question, a short label for its tile on Me
 * (e.g. "Focus"), its 3 options, and a summary sentence per option.
 */
data class SelfQuestionTexts(
    @param:StringRes val question: Int,
    @param:StringRes val label: Int,
    val options: List<Int>,
    val summaries: List<Int>,
)

/** The 12 questions in order: 6 about "who am I", then 6 about "how I manage my time". */
val SELF_QUESTIONS: List<SelfQuestionTexts> =
    listOf(
        SelfQuestionTexts(
            R.string.self_q_focus,
            R.string.self_k_focus,
            listOf(R.string.self_q_focus_0, R.string.self_q_focus_1, R.string.self_q_focus_2),
            listOf(R.string.self_s_focus_0, R.string.self_s_focus_1, R.string.self_s_focus_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_rest,
            R.string.self_k_rest,
            listOf(R.string.self_q_rest_0, R.string.self_q_rest_1, R.string.self_q_rest_2),
            listOf(R.string.self_s_rest_0, R.string.self_s_rest_1, R.string.self_s_rest_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_own_time,
            R.string.self_k_own_time,
            listOf(R.string.self_q_own_time_0, R.string.self_q_own_time_1, R.string.self_q_own_time_2),
            listOf(R.string.self_s_own_time_0, R.string.self_s_own_time_1, R.string.self_s_own_time_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_study,
            R.string.self_k_study,
            listOf(R.string.self_q_study_0, R.string.self_q_study_1, R.string.self_q_study_2),
            listOf(R.string.self_s_study_0, R.string.self_s_study_1, R.string.self_s_study_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_sleep,
            R.string.self_k_sleep,
            listOf(R.string.self_q_sleep_0, R.string.self_q_sleep_1, R.string.self_q_sleep_2),
            listOf(R.string.self_s_sleep_0, R.string.self_s_sleep_1, R.string.self_s_sleep_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_motivation,
            R.string.self_k_motivation,
            listOf(R.string.self_q_motivation_0, R.string.self_q_motivation_1, R.string.self_q_motivation_2),
            listOf(R.string.self_s_motivation_0, R.string.self_s_motivation_1, R.string.self_s_motivation_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_plan,
            R.string.self_k_plan,
            listOf(R.string.self_q_plan_0, R.string.self_q_plan_1, R.string.self_q_plan_2),
            listOf(R.string.self_s_plan_0, R.string.self_s_plan_1, R.string.self_s_plan_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_distraction,
            R.string.self_k_distraction,
            listOf(R.string.self_q_distraction_0, R.string.self_q_distraction_1, R.string.self_q_distraction_2),
            listOf(R.string.self_s_distraction_0, R.string.self_s_distraction_1, R.string.self_s_distraction_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_start,
            R.string.self_k_start,
            listOf(R.string.self_q_start_0, R.string.self_q_start_1, R.string.self_q_start_2),
            listOf(R.string.self_s_start_0, R.string.self_s_start_1, R.string.self_s_start_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_notes,
            R.string.self_k_notes,
            listOf(R.string.self_q_notes_0, R.string.self_q_notes_1, R.string.self_q_notes_2),
            listOf(R.string.self_s_notes_0, R.string.self_s_notes_1, R.string.self_s_notes_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_lots,
            R.string.self_k_lots,
            listOf(R.string.self_q_lots_0, R.string.self_q_lots_1, R.string.self_q_lots_2),
            listOf(R.string.self_s_lots_0, R.string.self_s_lots_1, R.string.self_s_lots_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_breaks,
            R.string.self_k_breaks,
            listOf(R.string.self_q_breaks_0, R.string.self_q_breaks_1, R.string.self_q_breaks_2),
            listOf(R.string.self_s_breaks_0, R.string.self_s_breaks_1, R.string.self_s_breaks_2),
        ),
    )

/**
 * The summary sentences for the answered questions in [range], one per line,
 * e.g. "I focus best in the morning.\nI rest best by moving." Empty when none is answered.
 */
fun Context.selfSummary(
    answers: String,
    range: IntRange,
): String =
    range
        .mapNotNull { index ->
            SelfAssessment.answerAt(answers, index)?.let { option -> getString(SELF_QUESTIONS[index].summaries[option]) }
        }.joinToString("\n")
