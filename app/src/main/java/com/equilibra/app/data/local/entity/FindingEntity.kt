package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A finding from the habit analysis, optionally linked to an activity.
 * If the linked activity is deleted, the finding stays and the link becomes null.
 */
@Entity(
    tableName = "findings",
    foreignKeys = [
        ForeignKey(
            entity = DayActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("activityId")],
)
data class FindingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val type: FindingType,
    val activityId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class FindingType { PROBLEM, GOOD_HABIT, HARMFUL_HABIT, NEED }
