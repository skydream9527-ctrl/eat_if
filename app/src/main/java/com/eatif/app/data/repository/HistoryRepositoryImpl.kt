package com.eatif.app.data.repository

import com.eatif.app.data.local.HistoryDao
import com.eatif.app.data.local.HistoryEntity
import com.eatif.app.domain.model.FoodAdoption
import com.eatif.app.domain.model.FoodFrequency
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

    override fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyDao.getFoodFrequencySinceRaw(fromTimestamp).map { list ->
            list.map { FoodFrequency(it.foodName, it.count) }
        }
    }

    override fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyDao.getFoodFrequencyBetweenRaw(fromTimestamp, toTimestamp).map { list ->
            list.map { FoodFrequency(it.foodName, it.count) }
        }
    }

    override fun getAdoptedRecommendationCount(): Flow<Int> = historyDao.getAdoptedRecommendationCount()

    override fun getTotalRecommendationCount(): Flow<Int> = historyDao.getTotalRecommendationCount()

    override fun getFoodAdoptionStats(): Flow<List<FoodAdoption>> =
        historyDao.getFoodAdoptionStats().map { list ->
            list.map { FoodAdoption(it.foodName, it.adoptedCount, it.totalCount) }
        }

    private fun HistoryEntity.toDomain(): History {
        return History(
            id = id,
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = timestamp,
            wasRecommended = wasRecommended
        )
    }

    private fun History.toEntity(): HistoryEntity {
        return HistoryEntity(
            id = id,
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = timestamp,
            wasRecommended = wasRecommended
        )
    }
}
