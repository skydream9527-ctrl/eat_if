package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_stats",
    indices = [Index(value = ["game_id"]), Index(value = ["timestamp"])]
)
data class GameStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game_id: String,
    val food_name: String,
    val score: Int,
    val score_percent: Int,
    val difficulty: String = "NORMAL",
    val level: Int = 1,
    val play_time_seconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

fun GameStatsEntity.toDomain() = com.eatif.app.domain.model.GameStats(
    id = id, gameId = game_id, foodName = food_name,
    score = score, scorePercent = score_percent, difficulty = difficulty,
    level = level, playTimeSeconds = play_time_seconds, timestamp = timestamp
)

fun com.eatif.app.domain.model.GameStats.toEntity() = GameStatsEntity(
    id = id, game_id = gameId, food_name = foodName,
    score = score, score_percent = scorePercent, difficulty = difficulty,
    level = level, play_time_seconds = playTimeSeconds, timestamp = timestamp
)
