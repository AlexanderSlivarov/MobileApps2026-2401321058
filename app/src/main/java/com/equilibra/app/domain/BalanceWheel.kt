package com.equilibra.app.domain

import com.equilibra.app.data.local.entity.WheelScoreEntity
import com.equilibra.app.data.local.entity.WheelSector

/** Balance wheel rules, kept free of Android classes so they can be unit-tested. */
object BalanceWheel {
    const val MIN_SCORE = 1
    const val MAX_SCORE = 5

    /** The score a sector gets before I rate it: the middle of the scale. */
    const val DEFAULT_SCORE = 3

    /**
     * The stored scores by sector, or null when nothing is stored yet (no rating).
     * A sector without a stored score gets the default.
     */
    fun scoresOf(stored: List<WheelScoreEntity>): Map<WheelSector, Int>? {
        if (stored.isEmpty()) return null
        val bySector = stored.associate { it.sector to it.score }
        return WheelSector.entries.associateWith { bySector[it] ?: DEFAULT_SCORE }
    }

    /**
     * For stress and workload a high score is not a good state.
     * For these two, a lower score is the better state. For all others, higher is better.
     */
    val HIGH_IS_NOT_GOOD: Set<WheelSector> = setOf(WheelSector.STRESS, WheelSector.WORKLOAD)

    fun isHighGood(sector: WheelSector): Boolean = sector !in HIGH_IS_NOT_GOOD
}
