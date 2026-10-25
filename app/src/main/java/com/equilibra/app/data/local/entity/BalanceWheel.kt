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
    /** 1-5, 5 ALWAYS means a good state. */
    val score: Int,
)

/** An assessment together with all its ratings, loaded by Room in one transaction. */
data class WheelAssessmentWithRatings(
    @Embedded val assessment: WheelAssessmentEntity,
    @Relation(parentColumn = "id", entityColumn = "assessmentId")
    val ratings: List<WheelRatingEntity>,
)

/** START = before (STEAM day 1), FINAL = after (day 7), CHECK_IN = anything in between. */
enum class WheelKind { START, CHECK_IN, FINAL }

/** Sectors are phrased so that a high score is always good ("calm", not "workload"). */
enum class WheelSector {
    STUDY_MOTIVATION,
    WORK_MOTIVATION,
    CALM,
    FRIENDS,
    HOBBIES,
    REST,
}
