package com.eatif.app.domain.usecase

import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.GameDifficulty
import com.eatif.app.domain.model.PlayerProfile
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerProfileUseCase @Inject constructor(
    private val profileDao: PlayerProfileDao
) {
    fun xpForLevel(level: Int): Int = 100 * level

    fun calculateXP(scorePercent: Int, difficulty: GameDifficulty, playTimeSeconds: Long): Int {
        val baseXP = (scorePercent * 0.5).toInt().coerceIn(0, 50)
        val difficultyMultiplier = when (difficulty) {
            GameDifficulty.EASY -> 1.0f
            GameDifficulty.NORMAL -> 1.5f
            GameDifficulty.HARD -> 2.0f
        }
        val timeBonus = (playTimeSeconds / 60).toInt().coerceAtMost(20)
        return (baseXP * difficultyMultiplier + timeBonus).toInt().coerceAtLeast(1)
    }

    suspend fun recordGameSession(xpEarned: Int, playTimeSeconds: Long): PlayerProfile {
        val profile = profileDao.getOrCreate().toDomain()
        val today = LocalDate.now().toString()

        val newStreak = if (profile.lastPlayedDate == today) {
            profile.currentStreak
        } else if (profile.lastPlayedDate == LocalDate.now().minusDays(1).toString()) {
            profile.currentStreak + 1
        } else {
            1
        }

        val newXP = profile.playerXP + xpEarned
        var newLevel = profile.playerLevel
        var remainingXP = newXP
        var levelUpXP = xpForLevel(newLevel)

        while (remainingXP >= levelUpXP && newLevel < 50) {
            remainingXP -= levelUpXP
            newLevel++
            levelUpXP = xpForLevel(newLevel)
        }

        val updated = profile.copy(
            totalGamesPlayed = profile.totalGamesPlayed + 1,
            totalPlayTimeSeconds = profile.totalPlayTimeSeconds + playTimeSeconds,
            currentStreak = newStreak,
            maxStreak = maxOf(newStreak, profile.maxStreak),
            playerLevel = newLevel,
            playerXP = remainingXP,
            lastPlayedDate = today
        )
        profileDao.update(updated.toEntity())
        return updated
    }

    suspend fun getProfile(): PlayerProfile {
        return profileDao.getOrCreate().toDomain()
    }

    fun xpNeededForNextLevel(profile: PlayerProfile): Int {
        return xpForLevel(profile.playerLevel) - profile.playerXP
    }

    fun xpProgressPercent(profile: PlayerProfile): Float {
        val needed = xpForLevel(profile.playerLevel)
        return if (needed > 0) (profile.playerXP.toFloat() / needed) * 100f else 0f
    }
}
