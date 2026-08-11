package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodAdoption
import com.eatif.app.domain.model.FoodFrequency
import kotlinx.coroutines.flow.Flow

interface RecommendRepository {
    fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>>
    fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>>
    fun getAllFoods(): Flow<List<Food>>
    /** 按美食分组的推荐采纳统计 - 用于反哺算法权重 */
    fun getFoodAdoptionStats(): Flow<List<FoodAdoption>>
}
