package com.eatif.app.data.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.repository.FoodRepository
import com.eatif.app.domain.repository.HistoryRepository
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecommendRepositoryImpl @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val foodRepository: FoodRepository
) : RecommendRepository {

    override fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyRepository.getFoodFrequencySince(fromTimestamp)
    }

    override fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyRepository.getFoodFrequencyBetween(fromTimestamp, toTimestamp)
    }

    override fun getAllFoods(): Flow<List<Food>> {
        return foodRepository.getAllFoods()
    }
}
