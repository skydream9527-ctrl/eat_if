package com.eatif.app.domain.usecase

import com.eatif.app.data.local.AchievementProgressDao
import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCondition
import com.eatif.app.domain.model.AchievementProgress
import com.eatif.app.ui.settings.AchievementSettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val profileDao: PlayerProfileDao,
    private val statsDao: GameStatsDao,
    private val progressDao: AchievementProgressDao
) {
    data class GameEndEvent(
        val gameId: String,
        val score: Int,
        val scorePercent: Int,
        val playTimeSeconds: Long,
        val difficulty: String
    )

    suspend fun checkAndUnlock(event: GameEndEvent): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val profile = profileDao.getOrCreate().toDomain()
        val unlockedIds = AchievementSettingsManager.getUnlockedIds()

        val totalGames = profile.totalGamesPlayed
        val totalPlayTime = profile.totalPlayTimeSeconds
        val currentStreak = profile.currentStreak
        val uniqueGames = statsDao.getUniqueGamesPlayed().first().size

        for (achievement in AchievementRegistry.all) {
            if (achievement.id in unlockedIds) continue

            val current = when (val condition = achievement.condition) {
                is AchievementCondition.TotalGames -> totalGames
                is AchievementCondition.GameHighScore -> {
                    if (condition.gameId == "any") {
                        val bestScore = statsDao.getBestScoreForGame(event.gameId).first()
                        if (bestScore != null && bestScore.score_percent >= condition.score) condition.score else 0
                    } else {
                        val best = statsDao.getBestScoreForGame(condition.gameId).first()
                        if (best != null) best.score_percent else 0
                    }
                }
                is AchievementCondition.ConsecutiveDays -> currentStreak
                is AchievementCondition.PlayAllGames -> uniqueGames
                is AchievementCondition.TotalPlayTime -> totalPlayTime.toInt()
            }

            val required = getRequiredProgress(achievement.condition)

            val progress = AchievementProgress(
                achievementId = achievement.id,
                currentProgress = current,
                requiredProgress = required,
                isUnlocked = current >= required,
                unlockedAt = if (current >= required) System.currentTimeMillis() else null
            )
            progressDao.upsert(progress.toEntity())

            if (current >= required && achievement.id !in unlockedIds) {
                AchievementSettingsManager.markUnlocked(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }

        return newlyUnlocked
    }

    private fun getRequiredProgress(condition: AchievementCondition): Int = when (condition) {
        is AchievementCondition.TotalGames -> condition.count
        is AchievementCondition.GameHighScore -> condition.score
        is AchievementCondition.ConsecutiveDays -> condition.days
        is AchievementCondition.PlayAllGames -> condition.count
        is AchievementCondition.TotalPlayTime -> condition.seconds.toInt()
    }
}
