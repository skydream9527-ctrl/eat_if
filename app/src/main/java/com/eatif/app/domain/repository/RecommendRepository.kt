package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import kotlinx.coroutines.flow.Flow

interface RecommendRepository {
    fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>>
    fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>>
    fun getAllFoods(): Flow<List<Food>>
}
