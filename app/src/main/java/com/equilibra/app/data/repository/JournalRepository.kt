package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.DecisionDao
import com.equilibra.app.data.local.dao.JournalDao
import com.equilibra.app.data.local.entity.DecisionEntity
import com.equilibra.app.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Balance tracking data: daily journal entries and decisions with their results. */
@Singleton
class JournalRepository
    @Inject
    constructor(
        private val journalDao: JournalDao,
        private val decisionDao: DecisionDao,
    ) {
        fun observeEntries(): Flow<List<JournalEntryEntity>> = journalDao.observeAll()

        fun observeEntriesBetween(
            from: LocalDate,
            to: LocalDate,
        ): Flow<List<JournalEntryEntity>> = journalDao.observeBetween(from, to)

        suspend fun getEntry(date: LocalDate): JournalEntryEntity? = journalDao.getByDate(date)

        /**
         * One entry per day: if that date already has an entry it is updated,
         * otherwise a new one is created.
         */
        suspend fun saveEntry(entry: JournalEntryEntity) {
            val existing = journalDao.getByDate(entry.date)
            if (existing == null) {
                journalDao.insert(entry.copy(id = 0))
            } else {
                journalDao.update(entry.copy(id = existing.id))
            }
        }

        suspend fun deleteEntry(entry: JournalEntryEntity) = journalDao.delete(entry)

        fun observeDecisions(): Flow<List<DecisionEntity>> = decisionDao.observeAll()

        suspend fun addDecision(decision: DecisionEntity): Long = decisionDao.insert(decision)

        suspend fun updateDecision(decision: DecisionEntity) = decisionDao.update(decision)

        suspend fun deleteDecision(decision: DecisionEntity) = decisionDao.delete(decision)
    }
