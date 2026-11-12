package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * One line of a day in My week, e.g. "09:00 – 18:00  Work". Either part of my usual
 * program ([everyWeek] = true: it shows on the same weekday of every week) or a one-time
 * task or important event (it shows only on [date]).
 */
@Entity(tableName = "day_items")
data class DayItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The day it was added for; for an every-week item only its weekday matters. */
    val date: LocalDate,
    /** Free text, e.g. "09:00" or "09:00 – 18:00"; can be empty. The day's lines are sorted by it. */
    val time: String,
    val text: String,
    val everyWeek: Boolean,
)
