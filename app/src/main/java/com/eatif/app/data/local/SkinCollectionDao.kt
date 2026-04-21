package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinCollectionDao {
    @Query("SELECT * FROM skin_collection WHERE skin_id = :skinId")
    fun getSkin(skinId: String): Flow<SkinCollectionEntity?>

    @Query("SELECT * FROM skin_collection WHERE game_id = :gameId")
    fun getSkinsForGame(gameId: String): Flow<List<SkinCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(skin: SkinCollectionEntity)

    @Query("UPDATE skin_collection SET is_active = 0 WHERE game_id = :gameId")
    suspend fun deactivateAllForGame(gameId: String)

    @Query("UPDATE skin_collection SET is_active = 1 WHERE skin_id = :skinId")
    suspend fun activateSkin(skinId: String)
}
