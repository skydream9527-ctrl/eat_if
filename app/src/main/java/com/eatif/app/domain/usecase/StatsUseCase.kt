package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import com.eatif.app.domain.usecase.AchievementRegistry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class StatsOverview(
    val totalGames: Int,
    val totalPlayTimeSeconds: Long,
    val bestScore: Int,
    val achievementsUnlocked: Int,
    val totalAchievements: Int
)

@Singleton
class StatsUseCase @Inject constructor(
    private val gameStatsRepository: GameStatsRepository
) {
    fun getStatsOverview(): Flow<StatsOverview> {
        return combine(
            gameStatsRepository.getTotalGamesCount(),
            gameStatsRepository.getTotalPlayTime(),
            gameStatsRepository.getGlobalTopScores()
        ) { totalGames, totalPlayTime, topScores ->
            StatsOverview(
                totalGames = totalGames,
                totalPlayTimeSeconds = totalPlayTime ?: 0L,
                bestScore = topScores.firstOrNull()?.scorePercent ?: 0,
                achievementsUnlocked = 0,
                totalAchievements = AchievementRegistry.all.size
            )
        }
    }

    fun getTopScoresForGame(gameId: String): Flow<List<GameStats>> {
        return gameStatsRepository.getTopScoresForGame(gameId)
    }

    fun getGlobalTopScores(): Flow<List<GameStats>> {
        return gameStatsRepository.getGlobalTopScores()
    }

    fun getGamesPlayed(): Flow<List<String>> {
        return gameStatsRepository.getUniqueGamesPlayed()
    }

    fun getRecentGames(fromTs: Long = 0): Flow<List<GameStats>> {
        return gameStatsRepository.getRecentGames(fromTs)
    }

    fun getGameStatsDetail(gameId: String): Flow<GameStats?> {
        return gameStatsRepository.getBestScoreForGame(gameId)
    }
}
