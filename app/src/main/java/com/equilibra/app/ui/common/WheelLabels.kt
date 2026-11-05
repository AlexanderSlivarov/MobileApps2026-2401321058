package com.equilibra.app.ui.common

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.WheelKind
import com.equilibra.app.data.local.entity.WheelSector

// UI texts and colors for the balance wheel. Kept here so the entities stay free of Android resources.

@get:StringRes
val WheelSector.labelRes: Int
    get() =
        when (this) {
            WheelSector.STUDY_MOTIVATION -> R.string.sector_study_motivation
            WheelSector.WORK_MOTIVATION -> R.string.sector_work_motivation
            WheelSector.CALM -> R.string.sector_calm
            WheelSector.FRIENDS -> R.string.sector_friends
            WheelSector.HOBBIES -> R.string.sector_hobbies
            WheelSector.REST -> R.string.sector_rest
        }

/** Each sector has its own color, with a lighter version for the night theme (values-night). */
@get:ColorRes
val WheelSector.colorRes: Int
    get() =
        when (this) {
            WheelSector.STUDY_MOTIVATION -> R.color.sector_study_motivation
            WheelSector.WORK_MOTIVATION -> R.color.sector_work_motivation
            WheelSector.CALM -> R.color.sector_calm
            WheelSector.FRIENDS -> R.color.sector_friends
            WheelSector.HOBBIES -> R.color.sector_hobbies
            WheelSector.REST -> R.color.sector_rest
        }

@get:StringRes
val WheelKind.labelRes: Int
    get() =
        when (this) {
            WheelKind.START -> R.string.wheel_kind_start
            WheelKind.CHECK_IN -> R.string.wheel_kind_check_in
            WheelKind.FINAL -> R.string.wheel_kind_final
        }
