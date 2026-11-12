package com.equilibra.app.ui.priorities

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.DayItemEntity
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.databinding.ItemDayLineBinding
import com.equilibra.app.databinding.ItemWeekDayBinding
import com.equilibra.app.databinding.ItemWeekDotBinding
import com.equilibra.app.databinding.ItemWeekTaskBinding
import com.equilibra.app.databinding.ViewWeekBinding
import com.equilibra.app.domain.Eisenhower
import com.equilibra.app.domain.WeekPlan
import com.google.android.material.color.MaterialColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** The short names of the days, Monday first. */
private val DAY_NAMES =
    listOf(R.string.day_1, R.string.day_2, R.string.day_3, R.string.day_4, R.string.day_5, R.string.day_6, R.string.day_7)

/** At most this many priority dots under a day; the chosen day below shows all of them. */
private const val MAX_DOTS = 4

/**
 * Fills My week for the week of [chosenDate]: the dates on top, a strip of the 7 days (today's
 * date filled, the chosen day tinted, a dot per planned priority in its group color and one
 * for one-time events), and below it the chosen day: its lines (my program, tasks and events),
 * the priorities planned for that date, or "Free". Tapping a day calls [onChooseDay], tapping a
 * line calls [onLineClick]. [tasks] must already be in my order.
 */
fun ViewWeekBinding.showWeek(
    tasks: List<TaskEntity>,
    items: List<DayItemEntity>,
    inflater: LayoutInflater,
    chosenDate: LocalDate,
    onChooseDay: (LocalDate) -> Unit,
    onLineClick: (DayItemEntity) -> Unit,
) {
    val context = root.context
    val today = LocalDate.now()
    val dates = WeekPlan.datesOfWeek(chosenDate)

    // "2 – 8 November", or "30 November – 6 December" when the week spans two months.
    val dayAndMonth = DateTimeFormatter.ofPattern("d MMMM")
    val sameMonth = dates.first().month == dates.last().month
    val start = if (sameMonth) dates.first().dayOfMonth.toString() else dates.first().format(dayAndMonth)
    textRange.text = context.getString(R.string.week_range_format, start, dates.last().format(dayAndMonth))

    val color = { attr: Int -> MaterialColors.getColor(root, attr) }
    val primary = color(androidx.appcompat.R.attr.colorPrimary)
    layoutDays.removeAllViews()
    dates.forEachIndexed { dayIndex, date ->
        val day = ItemWeekDayBinding.inflate(inflater, layoutDays, true)
        day.textDay.setText(DAY_NAMES[dayIndex])
        day.textDate.text = date.dayOfMonth.toString()
        // Today: filled with the accent color. The chosen day: a soft tint. Others: no circle.
        val (circle, onCircle) =
            when {
                date == today -> primary to color(com.google.android.material.R.attr.colorOnPrimary)
                date == chosenDate ->
                    color(com.google.android.material.R.attr.colorPrimaryContainer) to
                        color(com.google.android.material.R.attr.colorOnPrimaryContainer)
                else -> Color.TRANSPARENT to color(com.google.android.material.R.attr.colorOnSurface)
            }
        day.textDate.backgroundTintList = ColorStateList.valueOf(circle)
        day.textDate.setTextColor(onCircle)
        if (date == chosenDate) day.textDay.setTextColor(primary)
        WeekPlan.tasksFor(tasks, date).take(MAX_DOTS).forEach { task ->
            val dot = ItemWeekDotBinding.inflate(inflater, day.layoutDots, true).root
            dot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Eisenhower.groupOf(task).colorRes))
        }
        if (WeekPlan.hasOneTime(items, date)) {
            ItemWeekDotBinding.inflate(inflater, day.layoutDots, true).root.backgroundTintList = ColorStateList.valueOf(primary)
        }
        day.root.setOnClickListener { onChooseDay(date) }
    }

    // The chosen day: its full name and date, its lines, then its priorities as pills.
    textChosenDay.text = chosenDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")).replaceFirstChar { it.uppercase() }
    val lines = WeekPlan.linesFor(items, chosenDate)
    layoutLines.removeAllViews()
    lines.forEach { item ->
        val line = ItemDayLineBinding.inflate(inflater, layoutLines, true)
        line.textTime.text = item.time
        line.textLine.text = item.text
        line.root.setOnClickListener { onLineClick(item) }
    }

    val planned = WeekPlan.tasksFor(tasks, chosenDate)
    groupTasks.isVisible = planned.isNotEmpty()
    textFree.isVisible = planned.isEmpty() && lines.isEmpty()
    groupTasks.removeAllViews()
    val onPill = color(com.google.android.material.R.attr.colorSurface)
    planned.forEach { task ->
        val pill = ItemWeekTaskBinding.inflate(inflater, groupTasks, true).root
        pill.text = task.title
        pill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, Eisenhower.groupOf(task).colorRes))
        pill.setTextColor(onPill)
    }
}
