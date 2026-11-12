package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.FindingEntity
import com.equilibra.app.data.local.entity.FindingType
import com.equilibra.app.data.local.entity.HabitPlan
import com.equilibra.app.data.local.entity.NeedArea
import com.equilibra.app.data.local.entity.TaskEntity
import com.equilibra.app.data.local.entity.WheelSector

/**
 * The Statistics on the Balance page (and at the end of the report): numbers and facts worked
 * out from my own data. A function returns null when there is not enough data for it, so only
 * what can really be said is shown. Kept free of Android classes so it can be unit-tested.
 */
object Statistics {
    /** Problems traced to a cause with the 5 Whys, and all problems. Null without problems. */
    fun tracedProblems(findings: List<FindingEntity>): Pair<Int, Int>? {
        val problems = findings.count { it.type == FindingType.PROBLEM }
        return if (problems == 0) null else FiveWhys.countAnalyzed(findings) to problems
    }

    /** Of my habits with a plan, how many I want to change or stop, and how many have a plan. */
    fun habitsToChange(findings: List<FindingEntity>): Pair<Int, Int>? {
        val planned = findings.filter { it.type == FindingType.HABIT }.mapNotNull { it.habitPlan }
        return if (planned.isEmpty()) null else planned.count { it != HabitPlan.KEEP } to planned.size
    }

    /** The area most of my needs are for, and how many needs it has. */
    fun mostCommonNeedArea(findings: List<FindingEntity>): Pair<NeedArea, Int>? =
        findings
            .filter { it.type == FindingType.NEED }
            .mapNotNull { it.needArea }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.toPair()

    /** How many of my priorities are in "Do now" (important and urgent), and all of them. */
    fun doNow(tasks: List<TaskEntity>): Pair<Int, Int>? =
        if (tasks.isEmpty()) null else tasks.count { Eisenhower.groupOf(it) == EisenhowerGroup.I } to tasks.size

    /** The decision kept on the most days (at least one). */
    fun bestKeptDecision(decisions: List<DecisionEntity>): DecisionEntity? =
        decisions
            .filter { DecisionDays.doneCount(it.daysDone) > 0 }
            .maxByOrNull { DecisionDays.doneCount(it.daysDone) }

    /**
     * The day of the week on which I keep my decisions most often (0 = Monday), counted over
     * the kept days of all decisions. Null when no day is kept yet.
     */
    fun bestKeptWeekday(decisions: List<DecisionEntity>): Int? =
        decisions
            .flatMap { decision ->
                (0 until DecisionDays.DAYS)
                    .filter { DecisionDays.isDone(decision.daysDone, it) }
                    .map { decision.startDate.plusDays(it.toLong()).dayOfWeek.value - 1 }
            }.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    /**
     * My best and my weakest indicator on the wheel. "Good" means a high score
     * for most indicators, a low one for stress and workload.
     */
    fun bestAndWeakest(scores: Map<WheelSector, Int>): Pair<WheelSector, WheelSector> {
        val goodness = { sector: WheelSector -> goodness(sector, scores.getValue(sector)) }
        return WheelSector.entries.maxBy(goodness) to WheelSector.entries.minBy(goodness)
    }

    /** A score turned so that higher is always better (stress and workload are turned around). */
    private fun goodness(
        sector: WheelSector,
        score: Int,
    ): Int = if (BalanceWheel.isHighGood(sector)) score else BalanceWheel.MAX_SCORE + BalanceWheel.MIN_SCORE - score
}
