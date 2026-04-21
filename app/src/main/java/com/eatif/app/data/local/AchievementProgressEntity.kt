package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey val achievement_id: String,
    val current_progress: Int = 0,
    val required_progress: Int,
    val is_unlocked: Boolean = false,
    val unlocked_at: Long? = null
)

fun AchievementProgressEntity.toDomain() = com.eatif.app.domain.model.AchievementProgress(
    achievementId = achievement_id, currentProgress = current_progress,
    requiredProgress = required_progress, isUnlocked = is_unlocked, unlockedAt = unlocked_at
)

fun com.eatif.app.domain.model.AchievementProgress.toEntity() = AchievementProgressEntity(
    achievement_id = achievementId, current_progress = currentProgress,
    required_progress = requiredProgress, is_unlocked = isUnlocked, unlocked_at = unlockedAt
)
