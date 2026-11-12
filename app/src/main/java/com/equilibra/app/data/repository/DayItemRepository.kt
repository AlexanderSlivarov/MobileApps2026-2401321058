package com.equilibra.app.data.repository

import com.equilibra.app.data.local.dao.DayItemDao
import com.equilibra.app.data.local.entity.DayItemEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** My week: my usual program by days, and one-time tasks and events. */
@Singleton
class DayItemRepository
    @Inject
    constructor(
        private val dayItemDao: DayItemDao,
    ) {
        fun observeItems(): Flow<List<DayItemEntity>> = dayItemDao.observeAll()

        suspend fun addItem(item: DayItemEntity): Long = dayItemDao.insert(item)

        suspend fun updateItem(item: DayItemEntity) = dayItemDao.update(item)

        suspend fun deleteItem(item: DayItemEntity) = dayItemDao.delete(item)
    }
