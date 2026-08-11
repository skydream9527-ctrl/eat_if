package com.eatif.app.domain.repository

import com.eatif.app.domain.model.FoodAdoption
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.model.History
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentHistory(): Flow<List<History>>
    fun getHistoryCount(): Flow<Int>
    suspend fun addHistory(history: History): Long
    suspend fun deleteHistory(id: Long)
    suspend fun clearHistory()
    fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>>
    fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>>
    /** 推荐采纳数（用户从推荐 Top3 中选择的次数） */
    fun getAdoptedRecommendationCount(): Flow<Int>
    /** 推荐展示数（游戏完成的次数，即有过推荐候选的次数） */
    fun getTotalRecommendationCount(): Flow<Int>
    /** 按美食分组的采纳统计 - 用于推荐算法反哺 */
    fun getFoodAdoptionStats(): Flow<List<FoodAdoption>>
}
