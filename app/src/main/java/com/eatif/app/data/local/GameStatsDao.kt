package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: GameStatsEntity)

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score_percent DESC LIMIT 5")
    fun getTopScoresForGame(gameId: String): Flow<List<GameStatsEntity>>

    @Query("SELECT COUNT(*) FROM game_stats")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT SUM(play_time_seconds) FROM game_stats")
    fun getTotalPlayTime(): Flow<Long?>

    @Query("SELECT * FROM game_stats ORDER BY score_percent DESC LIMIT 10")
    fun getGlobalTopScores(): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score_percent DESC LIMIT 1")
    fun getBestScoreForGame(gameId: String): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score_percent DESC LIMIT 1")
    suspend fun getBestScoreForGameOnce(gameId: String): GameStatsEntity?

    @Query("SELECT DISTINCT game_id FROM game_stats")
    fun getUniqueGamesPlayed(): Flow<List<String>>

    @Query("SELECT * FROM game_stats WHERE timestamp >= :fromTs ORDER BY timestamp DESC")
    fun getRecentGames(fromTs: Long): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryForGame(gameId: String): Flow<List<GameStatsEntity>>
}
