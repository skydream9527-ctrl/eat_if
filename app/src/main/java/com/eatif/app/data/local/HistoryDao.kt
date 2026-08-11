package com.eatif.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 50")
    fun getRecentHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM history")
    fun getHistoryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    data class FoodFrequencyEntity(val foodName: String, val count: Int)

    @Query("SELECT foodName, COUNT(*) as count FROM history WHERE timestamp >= :fromTimestamp GROUP BY foodName ORDER BY count DESC")
    fun getFoodFrequencySinceRaw(fromTimestamp: Long): Flow<List<FoodFrequencyEntity>>

    @Query("SELECT foodName, COUNT(*) as count FROM history WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp GROUP BY foodName ORDER BY count DESC")
    fun getFoodFrequencyBetweenRaw(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequencyEntity>>

    /** 推荐采纳统计：被推荐且采纳的次数 / 被推荐的总次数 */
    @Query("SELECT COUNT(*) FROM history WHERE wasRecommended = 1")
    fun getAdoptedRecommendationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM history WHERE scorePercent >= 0")
    fun getTotalRecommendationCount(): Flow<Int>

    /** 按美食分组的采纳统计 - 用于推荐算法反哺 */
    data class FoodAdoptionEntity(val foodName: String, val adoptedCount: Int, val totalCount: Int)

    @Query("""
        SELECT foodName,
            SUM(CASE WHEN wasRecommended = 1 THEN 1 ELSE 0 END) as adoptedCount,
            COUNT(*) as totalCount
        FROM history
        WHERE scorePercent >= 0
        GROUP BY foodName
    """)
    fun getFoodAdoptionStats(): Flow<List<FoodAdoptionEntity>>
}
