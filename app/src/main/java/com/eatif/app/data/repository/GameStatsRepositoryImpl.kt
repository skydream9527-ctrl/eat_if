package com.eatif.app.data.repository

import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameStatsRepositoryImpl @Inject constructor(
    private val dao: GameStatsDao
) : GameStatsRepository {
    override suspend fun insert(stats: GameStats) {
        dao.insert(stats.toEntity())
    }
    override fun getTopScoresForGame(gameId: String): Flow<List<GameStats>> =
        dao.getTopScoresForGame(gameId).map { it.map { e -> e.toDomain() } }
    override fun getTotalGamesCount(): Flow<Int> = dao.getTotalGamesCount()
    override fun getTotalPlayTime(): Flow<Long?> = dao.getTotalPlayTime()
    override fun getGlobalTopScores(): Flow<List<GameStats>> =
        dao.getGlobalTopScores().map { it.map { e -> e.toDomain() } }
    override fun getBestScoreForGame(gameId: String): Flow<GameStats?> =
        dao.getBestScoreForGame(gameId).map { it?.toDomain() }
    override fun getUniqueGamesPlayed(): Flow<List<String>> = dao.getUniqueGamesPlayed()
    override fun getRecentGames(fromTs: Long): Flow<List<GameStats>> =
        dao.getRecentGames(fromTs).map { it.map { e -> e.toDomain() } }
    override fun getHistoryForGame(gameId: String): Flow<List<GameStats>> =
        dao.getHistoryForGame(gameId).map { it.map { e -> e.toDomain() } }
}
