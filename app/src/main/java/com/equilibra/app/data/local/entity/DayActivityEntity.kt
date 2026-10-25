package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One block of time in "My 24 hours" (e.g. 23:00-07:00 Sleep).
 * Times are minutes from midnight (0-1439). If [endMinute] < [startMinute]
 * the activity crosses midnight.
 * Named "DayActivity" to avoid confusion with Android's Activity class.
 */
@Entity(tableName = "day_activities", indices = [Index("date")])
data class DayActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val title: String,
    val category: ActivityCategory,
    val startMinute: Int,
    val endMinute: Int,
    val tag: ActivityTag = ActivityTag.NONE,
    /** true = how I planned the day, false = what actually happened. */
    val isPlanned: Boolean = false,
)

enum class ActivityCategory {
    SLEEP,
    STUDY,
    WORK,
    MEALS,
    SPORT,
    HOBBIES,
    FRIENDS_FAMILY,
    REST,
    SCREEN_TIME,
    COMMUTE,
    CHORES,
    OTHER,
}

enum class ActivityTag { NONE, LOST_TIME, MOST_VALUABLE_HOUR, WOULD_CHANGE }
