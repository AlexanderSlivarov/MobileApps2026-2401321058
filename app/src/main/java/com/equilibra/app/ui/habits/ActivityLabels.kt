package com.equilibra.app.ui.habits

import android.content.Context
import androidx.annotation.StringRes
import com.equilibra.app.R
import com.equilibra.app.data.local.entity.ActivityCategory

// UI texts for activities. Kept here so the entity stays free of Android resources.

@get:StringRes
val ActivityCategory.labelRes: Int
    get() =
        when (this) {
            ActivityCategory.SLEEP -> R.string.category_sleep
            ActivityCategory.STUDY -> R.string.category_study
            ActivityCategory.WORK -> R.string.category_work
            ActivityCategory.MEALS -> R.string.category_meals
            ActivityCategory.SPORT -> R.string.category_sport
            ActivityCategory.HOBBIES -> R.string.category_hobbies
            ActivityCategory.FRIENDS_FAMILY -> R.string.category_friends_family
            ActivityCategory.REST -> R.string.category_rest
            ActivityCategory.SCREEN_TIME -> R.string.category_screen_time
            ActivityCategory.COMMUTE -> R.string.category_commute
            ActivityCategory.CHORES -> R.string.category_chores
            ActivityCategory.OTHER -> R.string.category_other
        }

/** A duration like "2 h 30 min", "8 h" or "45 min". */
fun Context.formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> getString(R.string.duration_minutes, rest)
        rest == 0 -> getString(R.string.duration_hours, hours)
        else -> getString(R.string.duration_hours_minutes, hours, rest)
    }
}
