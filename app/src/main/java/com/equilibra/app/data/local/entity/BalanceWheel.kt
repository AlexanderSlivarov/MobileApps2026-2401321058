package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * My state on the wheel: one score per indicator. There is only one rating, so the
 * indicator itself is the key; saving again overwrites the old score.
 */
@Entity(tableName = "wheel_scores")
data class WheelScoreEntity(
    @PrimaryKey val sector: WheelSector,
    /** 1 = low, 5 = high. For STRESS and WORKLOAD a high score is not a good state. */
    val score: Int,
)

/**
 * The seven indicators of "Where am I now?", in this order.
 * Room stores each one by its name (TEXT).
 */
enum class WheelSector {
    STRESS,
    WORKLOAD,
    MOTIVATION,
    ORGANIZATION,
    FREE_TIME,
    STUDY_EFFICIENCY,
    REST_QUALITY,
}
