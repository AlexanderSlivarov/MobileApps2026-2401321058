package com.equilibra.app.ui.mysystem

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DayItemEntity
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.ProfileEntity
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.domain.DecisionDays
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.EisenhowerGroup
import com.equilibra.app.domain.FiveWhys
import com.equilibra.app.domain.SelfAssessment
import com.equilibra.app.domain.WeekPlan
import com.equilibra.app.ui.common.SELF_QUESTIONS
import com.equilibra.app.ui.common.labelRes
import com.equilibra.app.ui.common.toDisplayString
import com.equilibra.app.ui.decisions.colorRes
import com.equilibra.app.ui.decisions.labelRes
import com.equilibra.app.ui.habits.colorRes
import com.equilibra.app.ui.habits.labelRes
import com.equilibra.app.ui.priorities.colorRes
import com.equilibra.app.ui.priorities.labelRes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Everything stored in the app, read once for the report. */
data class ReportData(
    val profile: ProfileEntity,
    /** My rating on the wheel (null = not rated yet). */
    val wheel: Map<WheelSector, Int>?,
    val findings: List<FindingEntity>,
    val tasks: List<TaskEntity>,
    val decisions: List<DecisionEntity>,
    val dayItems: List<DayItemEntity>,
)

/** One piece of the report. ReportPdf decides how each one looks. */
sealed interface ReportBlock {
    /** The title of a section, e.g. "Self-assessment". */
    data class Heading(
        val text: String,
    ) : ReportBlock

    /** A smaller title inside a section, optionally in a color (e.g. a group's color). */
    data class Subheading(
        val text: String,
        val color: Int? = null,
    ) : ReportBlock

    /** A line of text; [indent] moves it right, e.g. for the "Why?" answers under a problem. */
    data class Line(
        val text: String,
        val indent: Int = 0,
        val color: Int? = null,
        val italic: Boolean = false,
    ) : ReportBlock

    /** A label and its value side by side, e.g. "Focus  In the morning". */
    data class Row(
        val label: String,
        val value: String,
    ) : ReportBlock
}

/**
 * Builds the report from the app's data: a section per tab in the order of the steps,
 * then a short summary in numbers. Colors are taken from the light (day) theme, because
 * the report is printed on white paper.
 */
object BalanceReport {
    private const val KEPT = "●"
    private const val NOT_KEPT = "○"

    fun build(
        context: Context,
        data: ReportData,
    ): List<ReportBlock> {
        val day = dayContext(context)
        val color = { res: Int -> ContextCompat.getColor(day, res) }
        val none = ReportBlock.Line(context.getString(R.string.report_none), italic = true)
        val blocks = mutableListOf<ReportBlock>()

        // 1. Me: the answers, my own words and the wheel.
        blocks += ReportBlock.Heading(context.getString(R.string.section_self_assessment))
        listOf(
            Triple(R.string.self_who_am_i, SelfAssessment.WHO_AM_I, data.profile.whoAmINote),
            Triple(R.string.self_manage_time, SelfAssessment.HOW_I_MANAGE_TIME, data.profile.manageTimeNote),
        ).forEach { (title, questions, note) ->
            blocks += ReportBlock.Subheading(context.getString(title))
            questions.forEach { index ->
                val texts = SELF_QUESTIONS[index]
                val answer = SelfAssessment.answerAt(data.profile.selfAnswers, index)
                blocks += ReportBlock.Row(context.getString(texts.label), answer?.let { context.getString(texts.options[it]) } ?: "–")
            }
            if (note.isNotBlank()) blocks += ReportBlock.Line(context.getString(R.string.self_note_format, note), italic = true)
        }
        blocks += ReportBlock.Subheading(context.getString(R.string.wheel_title))
        if (data.wheel == null) {
            blocks += none
        } else {
            WheelSector.entries.forEach { sector ->
                blocks += ReportBlock.Row(context.getString(sector.labelRes), data.wheel.getValue(sector).toString())
            }
        }

        // 2. Observations, by type.
        blocks += ReportBlock.Heading(context.getString(R.string.section_habits))
        if (data.findings.isEmpty()) blocks += none
        FindingType.entries.forEach { type ->
            val ofType = data.findings.filter { it.type == type }
            if (ofType.isEmpty()) return@forEach
            blocks += ReportBlock.Subheading(context.getString(type.labelRes), color(type.colorRes))
            ofType.forEach { finding ->
                blocks += ReportBlock.Line("• " + finding.text)
                when (type) {
                    FindingType.PROBLEM -> {
                        finding.whys.filter { it.isNotBlank() }.forEachIndexed { i, why ->
                            blocks += ReportBlock.Line("${i + 1}. ${context.getString(R.string.why_hint)} ${why.trim()}", indent = 1)
                        }
                        FiveWhys.rootCause(finding.whys)?.let {
                            blocks += ReportBlock.Line(context.getString(R.string.root_cause_title) + ": " + it, indent = 1, italic = true)
                        }
                    }
                    FindingType.HABIT ->
                        finding.habitPlan?.let { blocks += ReportBlock.Line(context.getString(it.labelRes), indent = 1, color = color(it.colorRes)) }
                    FindingType.NEED ->
                        finding.needArea?.let { blocks += ReportBlock.Line(context.getString(it.labelRes), indent = 1, color = color(R.color.type_need)) }
                }
            }
        }

        // 3. Priorities: my priorities by group in my order, then the postponed ones.
        blocks += ReportBlock.Heading(context.getString(R.string.section_priorities))
        val active = Eisenhower.inGroupOrder(data.tasks.filter { !it.isNotStarted })
        if (active.isEmpty()) blocks += none
        var number = 0
        EisenhowerGroup.entries.forEach { group ->
            val inGroup = active.filter { Eisenhower.groupOf(it) == group }
            if (inGroup.isEmpty()) return@forEach
            blocks += ReportBlock.Subheading(context.getString(group.labelRes), color(group.colorRes))
            inGroup.forEach { task ->
                number++
                blocks += ReportBlock.Line("$number. ${task.title}" + (task.date?.let { "  ·  " + it.toDisplayString() } ?: ""))
            }
        }
        val postponed = data.tasks.filter { it.isNotStarted }
        if (postponed.isNotEmpty()) {
            blocks += ReportBlock.Subheading(context.getString(R.string.not_started_title))
            postponed.forEach { blocks += ReportBlock.Line("• " + it.title) }
        }

        // 4. My week: this week's days, each with its lines (program, tasks, events) and priorities.
        blocks += ReportBlock.Heading(context.getString(R.string.week_title))
        val dayAndMonth = DateTimeFormatter.ofPattern("EEEE, d MMMM")
        WeekPlan.datesOfWeek(LocalDate.now()).forEach { date ->
            blocks += ReportBlock.Subheading(date.format(dayAndMonth).replaceFirstChar { it.uppercase() })
            val lines = WeekPlan.linesFor(data.dayItems, date)
            val planned = WeekPlan.tasksFor(active, date)
            lines.forEach { blocks += ReportBlock.Row(it.time, it.text) }
            if (planned.isNotEmpty()) blocks += ReportBlock.Line(planned.joinToString(", ") { it.title }, indent = 1)
            if (lines.isEmpty() && planned.isEmpty()) blocks += ReportBlock.Line(context.getString(R.string.week_free), italic = true)
        }

        // 5. Decisions: result, the 7 days and what changed.
        blocks += ReportBlock.Heading(context.getString(R.string.section_decisions))
        if (data.decisions.isEmpty()) blocks += none
        data.decisions.forEach { decision ->
            blocks += ReportBlock.Subheading(decision.text)
            blocks +=
                ReportBlock.Line(
                    context.getString(decision.result.labelRes) + "  ·  " +
                        context.getString(R.string.decision_since_format, decision.startDate.toDisplayString()),
                    color = color(decision.result.colorRes),
                )
            val dots = (0 until DecisionDays.DAYS).joinToString(" ") { if (DecisionDays.isDone(decision.daysDone, it)) KEPT else NOT_KEPT }
            blocks += ReportBlock.Line(dots + "   " + context.getString(R.string.decision_kept_format, DecisionDays.doneCount(decision.daysDone)))
            if (decision.note.isNotBlank()) {
                blocks += ReportBlock.Line(context.getString(R.string.change_title) + ": " + decision.note, italic = true)
            }
        }

        // 6. Statistics: numbers and facts from all of it, the same as on the Balance page.
        val statistics = context.statisticsItems(data.profile.selfAnswers, active, data.findings, data.decisions, data.wheel)
        if (statistics.isNotEmpty()) {
            blocks += ReportBlock.Heading(context.getString(R.string.stats_title))
            statistics.forEach { blocks += ReportBlock.Row(it.label, it.value) }
        }
        return blocks
    }

    /** The same resources, but with the light theme's colors (for white paper). */
    fun dayContext(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
        return context.createConfigurationContext(config)
    }

}
