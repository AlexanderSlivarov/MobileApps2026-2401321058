package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.WheelAssessmentWithRatings
import com.equilibra.app.data.local.entity.WheelKind
import com.equilibra.app.data.local.entity.WheelSector

/** Balance wheel rules, kept free of Android classes so they can be unit-tested. */
object BalanceWheel {
    const val MIN_SCORE = 1
    const val MAX_SCORE = 5

    /** The score a sector gets on a new rating: the middle of the scale. */
    const val DEFAULT_SCORE = 3

    /** The scores of one rating by sector. A sector without a stored score gets the default. */
    fun scoresOf(assessment: WheelAssessmentWithRatings): Map<WheelSector, Int> {
        val stored = assessment.ratings.associate { it.sector to it.score }
        return WheelSector.entries.associateWith { stored[it] ?: DEFAULT_SCORE }
    }

    /** Average over all sectors, e.g. 3.5. */
    fun average(scores: Map<WheelSector, Int>): Double = scores.values.average()

    /**
     * Which two ratings the wheel shows: the newest one, and the starting one to compare
     * with ("before"). The comparison is null when the newest rating is the start itself.
     * [assessments] must be sorted oldest first, as the repository returns them.
     */
    fun currentAndStart(assessments: List<WheelAssessmentWithRatings>): Pair<WheelAssessmentWithRatings?, WheelAssessmentWithRatings?> {
        val current = assessments.lastOrNull()
        val start = assessments.firstOrNull { it.assessment.kind == WheelKind.START }
        return current to start?.takeIf { it != current }
    }

    /**
     * START and FINAL can each exist only once, because they are the "before" and "after"
     * of the comparison. CHECK_IN can be used any number of times.
     */
    fun isKindTaken(
        assessments: List<WheelAssessmentWithRatings>,
        kind: WheelKind,
        editedId: Long,
    ): Boolean =
        kind != WheelKind.CHECK_IN &&
            assessments.any { it.assessment.kind == kind && it.assessment.id != editedId }
}
