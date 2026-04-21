package com.eatif.app.domain.repository

import com.eatif.app.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

interface GameStatsRepository {
    suspend fun insert(stats: GameStats)
    fun getTopScoresForGame(gameId: String): Flow<List<GameStats>>
    fun getTotalGamesCount(): Flow<Int>
    fun getTotalPlayTime(): Flow<Long?>
    fun getGlobalTopScores(): Flow<List<GameStats>>
    fun getBestScoreForGame(gameId: String): Flow<GameStats?>
    fun getUniqueGamesPlayed(): Flow<List<String>>
    fun getRecentGames(fromTs: Long = 0): Flow<List<GameStats>>
    fun getHistoryForGame(gameId: String): Flow<List<GameStats>>
}
