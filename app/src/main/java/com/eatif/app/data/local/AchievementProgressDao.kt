package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementProgressDao {
    @Query("SELECT * FROM achievement_progress")
    fun getAllProgress(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT achievement_id FROM achievement_progress WHERE is_unlocked = 1")
    fun getUnlockedIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM achievement_progress WHERE is_unlocked = 1")
    fun getUnlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: AchievementProgressEntity)
}
