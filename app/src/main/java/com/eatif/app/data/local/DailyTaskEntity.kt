package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eatif.app.domain.model.DailyTask
import com.eatif.app.domain.model.DailyTaskType

@Entity(tableName = "daily_tasks")
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskType: String,
    val description: String,
    val targetProgress: Int,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val isRewardClaimed: Boolean,
    val xpReward: Int,
    val date: String
)

fun DailyTaskEntity.toDomain(): DailyTask = DailyTask(
    id = id,
    taskType = DailyTaskType.fromId(taskType),
    description = description,
    targetProgress = targetProgress,
    currentProgress = currentProgress,
    isCompleted = isCompleted,
    isRewardClaimed = isRewardClaimed,
    xpReward = xpReward,
    date = date
)

fun DailyTask.toEntity(): DailyTaskEntity = DailyTaskEntity(
    id = id,
    taskType = taskType.id,
    description = description,
    targetProgress = targetProgress,
    currentProgress = currentProgress,
    isCompleted = isCompleted,
    isRewardClaimed = isRewardClaimed,
    xpReward = xpReward,
    date = date
)
