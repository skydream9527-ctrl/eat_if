package com.eatif.app.domain.usecase

import com.eatif.app.data.local.LevelProgressDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.LevelProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LevelManager @Inject constructor(
    private val levelProgressDao: LevelProgressDao
) {
    suspend fun getProgress(gameId: String): LevelProgress {
        val existing = levelProgressDao.getProgressOnce(gameId)
        return existing?.toDomain() ?: LevelProgress(gameId = gameId)
    }

    suspend fun isLevelUnlocked(gameId: String, levelNumber: Int): Boolean {
        if (levelNumber <= 1) return true
        val progress = getProgress(gameId)
        val prevLevel = levelNumber - 1
        val prevStars = progress.stars[prevLevel] ?: 0
        val requiredLevel = GameLevelRegistry.getLevel(gameId, levelNumber)
        return prevStars >= (requiredLevel?.requiredStars ?: 0)
    }

    suspend fun recordLevelResult(
        gameId: String,
        levelNumber: Int,
        scorePercent: Int
    ): LevelProgress {
        val progress = getProgress(gameId)
        val stars = when {
            scorePercent <= 0 -> 0
            scorePercent >= 90 -> 3
            scorePercent >= 70 -> 2
            else -> 1
        }

        val currentStars = progress.stars.toMutableMap()
        val prevStars = currentStars[levelNumber] ?: 0
        if (stars > prevStars) {
            currentStars[levelNumber] = stars
        }

        val bestScores = progress.bestScores.toMutableMap()
        val prevBest = bestScores[levelNumber] ?: 0
        if (scorePercent > prevBest) {
            bestScores[levelNumber] = scorePercent
        }

        val maxLevel = GameLevelRegistry.getMaxLevelForGame(gameId)
        var newCurrentLevel = progress.currentLevel
        for (lvl in 1..maxLevel) {
            val level = GameLevelRegistry.getLevel(gameId, lvl) ?: continue
            val reqStars = level.requiredStars
            val earnedStars = currentStars[lvl - 1] ?: 0
            if (lvl > 1 && earnedStars >= reqStars && lvl > newCurrentLevel) {
                newCurrentLevel = lvl
            }
        }

        val updated = progress.copy(
            currentLevel = newCurrentLevel,
            stars = currentStars,
            bestScores = bestScores
        )
        levelProgressDao.upsert(updated.toEntity())
        return updated
    }

    fun calculateStars(scorePercent: Int): Int = when {
        scorePercent <= 0 -> 0
        scorePercent >= 90 -> 3
        scorePercent >= 70 -> 2
        else -> 1
    }
}
