package com.equilibra.app.ui.common

import android.content.Context
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.domain.SelfAssessment

/** The texts of one self-assessment question: the question, its 3 options, and a summary sentence per option. */
data class SelfQuestionTexts(
    @param:StringRes val question: Int,
    val options: List<Int>,
    val summaries: List<Int>,
)

/** The 6 questions in order: 3 about "who am I", then 3 about "how I manage my time". */
val SELF_QUESTIONS: List<SelfQuestionTexts> =
    listOf(
        SelfQuestionTexts(
            R.string.self_q_focus,
            listOf(R.string.self_q_focus_0, R.string.self_q_focus_1, R.string.self_q_focus_2),
            listOf(R.string.self_s_focus_0, R.string.self_s_focus_1, R.string.self_s_focus_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_rest,
            listOf(R.string.self_q_rest_0, R.string.self_q_rest_1, R.string.self_q_rest_2),
            listOf(R.string.self_s_rest_0, R.string.self_s_rest_1, R.string.self_s_rest_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_own_time,
            listOf(R.string.self_q_own_time_0, R.string.self_q_own_time_1, R.string.self_q_own_time_2),
            listOf(R.string.self_s_own_time_0, R.string.self_s_own_time_1, R.string.self_s_own_time_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_plan,
            listOf(R.string.self_q_plan_0, R.string.self_q_plan_1, R.string.self_q_plan_2),
            listOf(R.string.self_s_plan_0, R.string.self_s_plan_1, R.string.self_s_plan_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_big_task,
            listOf(R.string.self_q_big_task_0, R.string.self_q_big_task_1, R.string.self_q_big_task_2),
            listOf(R.string.self_s_big_task_0, R.string.self_s_big_task_1, R.string.self_s_big_task_2),
        ),
        SelfQuestionTexts(
            R.string.self_q_distraction,
            listOf(R.string.self_q_distraction_0, R.string.self_q_distraction_1, R.string.self_q_distraction_2),
            listOf(R.string.self_s_distraction_0, R.string.self_s_distraction_1, R.string.self_s_distraction_2),
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
