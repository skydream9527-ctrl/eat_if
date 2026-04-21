package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE game_id = :gameId")
    fun getProgress(gameId: String): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress WHERE game_id = :gameId")
    suspend fun getProgressOnce(gameId: String): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)
}
