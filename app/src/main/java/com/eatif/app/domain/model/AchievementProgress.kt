package com.eatif.app.domain.model

data class AchievementProgress(
    val achievementId: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
