package com.equilibra.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate

/**
 * One dated rating of the balance wheel (the "set"). Its sector scores live in
 * [WheelRatingEntity] (one-to-many), so before/after sets can be compared.
 */
@Entity(tableName = "wheel_assessments")
data class WheelAssessmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val kind: WheelKind,
)

/** One sector's score inside an assessment. Deleting the assessment deletes its ratings. */
@Entity(
    tableName = "wheel_ratings",
    primaryKeys = ["assessmentId", "sector"],
    foreignKeys = [
        ForeignKey(
            entity = WheelAssessmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("assessmentId")],
)
data class WheelRatingEntity(
    val assessmentId: Long,
    val sector: WheelSector,
    /** 1 = low, 5 = high. For STRESS and WORKLOAD a high score is not a good state. */
    val score: Int,
)

/** An assessment together with all its ratings, loaded by Room in one transaction. */
data class WheelAssessmentWithRatings(
    @Embedded val assessment: WheelAssessmentEntity,
    @Relation(parentColumn = "id", entityColumn = "assessmentId")
    val ratings: List<WheelRatingEntity>,
)

/** START = before, FINAL = after, CHECK_IN = anything in between. */
enum class WheelKind { START, CHECK_IN, FINAL }

/**
 * The seven indicators of "Where am I now?" from Lecture 1, in the lecture's order.
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
