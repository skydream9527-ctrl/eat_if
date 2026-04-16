package com.eatif.app.data.repository

import com.eatif.app.data.local.HistoryDao
import com.eatif.app.data.local.HistoryEntity
import com.eatif.app.domain.model.History
import com.eatif.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getRecentHistory(): Flow<List<History>> {
        return historyDao.getRecentHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getHistoryCount(): Flow<Int> {
        return historyDao.getHistoryCount()
    }

    override suspend fun addHistory(history: History): Long {
        return historyDao.insertHistory(history.toEntity())
    }

    override suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistory(id)
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    private fun HistoryEntity.toDomain(): History {
        return History(
            id = id,
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = timestamp
        )
    }

    private fun History.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = timestamp
        )
    }
}