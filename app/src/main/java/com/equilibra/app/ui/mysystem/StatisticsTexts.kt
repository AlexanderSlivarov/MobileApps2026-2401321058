package com.equilibra.app.ui.mysystem

import android.content.Context
import androidx.annotation.DrawableRes
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.domain.BalanceWheel
import com.equilibra.app.domain.DecisionDays
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.domain.Statistics
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.habits.labelRes
import java.time.DayOfWeek
import java.time.format.TextStyle
import com.equilibra.app.ui.habits.iconRes

/** One fact of the Statistics: the icon of the tab it comes from, what it is, and its value. */
data class StatItem(
    @param:DrawableRes val icon: Int,
    val label: String,
    val value: String,
)

/** The self-assessment questions shown in the Statistics: when I focus best, what distracts me most. */
private const val FOCUS_QUESTION = 0
private const val DISTRACTION_QUESTION = 7

/**
 * The Statistics in the order of the tabs, e.g. "Day with the most priorities" = "Wednesday (2)".
 * Only facts my data can support are included. Used on Balance and at the end of the report.
 * [tasks] are my priorities without the postponed ones; [wheelNow] / [wheelStart] are the
 * newest and the starting rating of the wheel.
 */
fun Context.statisticsItems(
    selfAnswers: String,
    tasks: List<TaskEntity>,
    findings: List<FindingEntity>,
    decisions: List<DecisionEntity>,
    wheelNow: WheelAssessmentWithRatings?,
    wheelStart: WheelAssessmentWithRatings?,
): List<StatItem> {
    val items = mutableListOf<StatItem>()
    val of = { part: Int, all: Int -> getString(R.string.c_of_format, part, all) }
    val withCount = { text: String, count: Int -> getString(R.string.s_count_format, text, count) }

    // Me: two of my own answers
    listOf(FOCUS_QUESTION to R.string.s_focus, DISTRACTION_QUESTION to R.string.s_distraction).forEach { (index, label) ->
        SelfAssessment.answerAt(selfAnswers, index)?.let { option ->
            items += StatItem(R.drawable.ic_person, getString(label), getString(SELF_QUESTIONS[index].options[option]))
        }
    }

    // Observations
    Statistics.tracedProblems(findings)?.let { (traced, all) -> items += StatItem(R.drawable.ic_schedule, getString(R.string.s_traced), of(traced, all)) }
    Statistics.habitsToChange(findings)?.let { (change, all) ->
        items += StatItem(R.drawable.ic_schedule, getString(R.string.s_habits_change), of(change, all))
    }
    Statistics.mostCommonNeedArea(findings)?.let { (area, count) ->
        items += StatItem(area.iconRes, getString(R.string.s_need_area), withCount(getString(area.labelRes), count))
    }

    // Priorities and My week
    Statistics.doNow(tasks)?.let { (now, all) -> items += StatItem(R.drawable.ic_list, getString(R.string.s_do_now), of(now, all)) }
    Statistics.busiestDay(tasks)?.let { (day, count) ->
        items += StatItem(R.drawable.ic_calendar, getString(R.string.s_busiest_day), withCount(dayName(day), count))
    }
    Statistics.freeDays(tasks)?.let { free ->
        val value = if (free.isEmpty()) getString(R.string.c_no_free_days) else free.joinToString(", ") { dayName(it) }
        items += StatItem(R.drawable.ic_calendar, getString(R.string.s_free_days), value)
    }

    // Decisions
    Statistics.bestKeptDecision(decisions)?.let { decision ->
        val value = getString(R.string.wheel_kind_date_format, decision.text, getString(R.string.s_days_of_7_format, DecisionDays.doneCount(decision.daysDone)))
        items += StatItem(R.drawable.ic_edit, getString(R.string.s_best_decision), value)
    }
    Statistics.bestKeptWeekday(decisions)?.let { day -> items += StatItem(R.drawable.ic_edit, getString(R.string.s_kept_weekday), dayName(day)) }

    // The wheel
    wheelNow?.let { now ->
        val scores = BalanceWheel.scoresOf(now)
        val (best, weakest) = Statistics.bestAndWeakest(scores)
        items += StatItem(R.drawable.ic_dashboard, getString(R.string.s_best_indicator), getString(best.labelRes))
        items += StatItem(R.drawable.ic_dashboard, getString(R.string.s_weakest_indicator), getString(weakest.labelRes))
        wheelStart?.let { start ->
            val improved = Statistics.improvedSinceStart(BalanceWheel.scoresOf(start), scores)
            items += StatItem(R.drawable.ic_dashboard, getString(R.string.s_improved), of(improved, WheelSector.entries.size))
        }
    }
    return items
}

/** The full name of a day in the app's language, e.g. "Wednesday" / "Сряда" (0 = Monday). */
private fun Context.dayName(day: Int): String =
    DayOfWeek
        .of(day + 1)
        .getDisplayName(TextStyle.FULL_STANDALONE, resources.configuration.locales[0])
        .replaceFirstChar { it.uppercase() }
