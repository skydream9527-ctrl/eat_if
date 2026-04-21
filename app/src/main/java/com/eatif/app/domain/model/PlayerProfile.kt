package com.eatif.app.domain.model

data class PlayerProfile(
    val id: Long = 0,
    val totalGamesPlayed: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val playerLevel: Int = 1,
    val playerXP: Int = 0,
    val lastPlayedDate: String = ""
)
