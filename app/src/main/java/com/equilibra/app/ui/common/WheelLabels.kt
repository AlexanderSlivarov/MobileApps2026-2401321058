package com.equilibra.app.ui.common

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.WheelKind
import com.equilibra.app.data.local.entity.WheelSector
import com.equilibra.app.domain.BalanceWheel

// UI texts and colors for the balance wheel. Kept here so the entities stay free of Android resources.

@get:StringRes
val WheelSector.labelRes: Int
    get() =
        when (this) {
            WheelSector.STRESS -> R.string.sector_stress
            WheelSector.WORKLOAD -> R.string.sector_workload
            WheelSector.MOTIVATION -> R.string.sector_motivation
            WheelSector.ORGANIZATION -> R.string.sector_organization
            WheelSector.FREE_TIME -> R.string.sector_free_time
            WheelSector.STUDY_EFFICIENCY -> R.string.sector_study_efficiency
            WheelSector.REST_QUALITY -> R.string.sector_rest_quality
        }

/** Each sector has its own color, with a lighter version for the night theme (values-night). */
@get:ColorRes
val WheelSector.colorRes: Int
    get() =
        when (this) {
            WheelSector.STRESS -> R.color.sector_stress
            WheelSector.WORKLOAD -> R.color.sector_workload
            WheelSector.MOTIVATION -> R.color.sector_motivation
            WheelSector.ORGANIZATION -> R.color.sector_organization
            WheelSector.FREE_TIME -> R.color.sector_free_time
            WheelSector.STUDY_EFFICIENCY -> R.color.sector_study_efficiency
            WheelSector.REST_QUALITY -> R.color.sector_rest_quality
        }

/**
 * The sector's name for the legend and the sliders. Stress and workload get
 * "(lower is better)", because there a high score is not a good state (Lecture 1).
 */
fun Context.sectorLabel(sector: WheelSector): String {
    val name = getString(sector.labelRes)
    return if (BalanceWheel.isHighGood(sector)) name else getString(R.string.sector_lower_is_better_format, name)
}

@get:StringRes
val WheelKind.labelRes: Int
    get() =
        when (this) {
            WheelKind.START -> R.string.wheel_kind_start
            WheelKind.CHECK_IN -> R.string.wheel_kind_check_in
            WheelKind.FINAL -> R.string.wheel_kind_final
        }
