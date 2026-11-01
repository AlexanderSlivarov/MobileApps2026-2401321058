package com.equilibra.app.ui.priorities

import androidx.annotation.AttrRes
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.TaskStatus
import com.equilibra.app.domain.Quadrant
import com.google.android.material.R as MaterialR

// UI texts and colors for the domain enums. Kept here so the domain stays free of Android.

@get:StringRes
val Quadrant.titleRes: Int
    get() =
        when (this) {
            Quadrant.DO_FIRST -> R.string.quadrant_do_first
            Quadrant.SCHEDULE -> R.string.quadrant_schedule
            Quadrant.DELEGATE -> R.string.quadrant_delegate
            Quadrant.ELIMINATE -> R.string.quadrant_eliminate
        }

@get:StringRes
val Quadrant.hintRes: Int
    get() =
        when (this) {
            Quadrant.DO_FIRST -> R.string.quadrant_do_first_hint
            Quadrant.SCHEDULE -> R.string.quadrant_schedule_hint
            Quadrant.DELEGATE -> R.string.quadrant_delegate_hint
            Quadrant.ELIMINATE -> R.string.quadrant_eliminate_hint
        }

/** Theme color roles, so each quadrant looks right in both day and night themes. */
@get:AttrRes
val Quadrant.colorAttr: Int
    get() =
        when (this) {
            Quadrant.DO_FIRST -> MaterialR.attr.colorPrimaryContainer
            Quadrant.SCHEDULE -> MaterialR.attr.colorSecondaryContainer
            Quadrant.DELEGATE -> MaterialR.attr.colorTertiaryContainer
            Quadrant.ELIMINATE -> MaterialR.attr.colorSurfaceContainerHighest
        }

@get:StringRes
val TaskStatus.labelRes: Int
    get() =
        when (this) {
            TaskStatus.TODO -> R.string.status_todo
            TaskStatus.IN_PROGRESS -> R.string.status_in_progress
            TaskStatus.DONE -> R.string.status_done
        }
