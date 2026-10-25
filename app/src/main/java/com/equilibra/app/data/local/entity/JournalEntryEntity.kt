package com.equilibra.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/** One day in the balance-tracking journal. At most one entry per date (unique index). */
@Entity(tableName = "journal_entries", indices = [Index(value = ["date"], unique = true)])
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val tasksPlanned: Int,
    val tasksDone: Int,
    /** 1-5, 5 = best. */
    val energy: Int,
    /** 1-5, 5 = best. */
    val motivation: Int,
    val hadRest: Boolean,
    val correctionNeeded: Boolean,
    val note: String = "",
)
