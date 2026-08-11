package com.eatif.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTaskDao {
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY id")
    fun getTasksByDate(date: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date")
    suspend fun getTasksByDateOnce(date: String): List<DailyTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<DailyTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: DailyTaskEntity)

    @Query("DELETE FROM daily_tasks WHERE date < :date")
    suspend fun deleteOlderThan(date: String)

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE date = :date AND isRewardClaimed = 1")
    fun getClaimedCountForDate(date: String): Flow<Int>
}
