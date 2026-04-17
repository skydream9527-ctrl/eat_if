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
}