package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val total_games_played: Int = 0,
    val total_play_time_seconds: Long = 0,
    val current_streak: Int = 0,
    val max_streak: Int = 0,
    val player_level: Int = 1,
    val player_xp: Int = 0,
    val last_played_date: String = ""
)

fun PlayerProfileEntity.toDomain() = com.eatif.app.domain.model.PlayerProfile(
    id = id,
    totalGamesPlayed = total_games_played,
    totalPlayTimeSeconds = total_play_time_seconds,
    currentStreak = current_streak,
    maxStreak = max_streak,
    playerLevel = player_level,
    playerXP = player_xp,
    lastPlayedDate = last_played_date
)

fun com.eatif.app.domain.model.PlayerProfile.toEntity() = PlayerProfileEntity(
    id = id,
    total_games_played = totalGamesPlayed,
    total_play_time_seconds = totalPlayTimeSeconds,
    current_streak = currentStreak,
    max_streak = maxStreak,
    player_level = playerLevel,
    player_xp = playerXP,
    last_played_date = lastPlayedDate
)
