package com.eatif.app.domain.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val condition: AchievementCondition,
    val xpReward: Int,
    val unlockSkinId: String? = null
)

enum class AchievementCategory {
    MILESTONE, STREAK, SKILL, EXPLORATION
}

sealed class AchievementCondition {
    data class TotalGames(val count: Int) : AchievementCondition()
    data class GameHighScore(val gameId: String, val score: Int) : AchievementCondition()
    data class ConsecutiveDays(val days: Int) : AchievementCondition()
    data class PlayAllGames(val count: Int) : AchievementCondition()
    data class TotalPlayTime(val seconds: Long) : AchievementCondition()
}
