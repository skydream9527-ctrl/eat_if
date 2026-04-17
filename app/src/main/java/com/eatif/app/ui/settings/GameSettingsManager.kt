package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.eatif.app.domain.model.GameDifficulty
import com.eatif.app.domain.model.GameRuleConfig

object GameSettingsManager {
    private const val PREFS_NAME = "eat_if_game_settings"
    private const val KEY_DIFFICULTY = "difficulty"
    private const val KEY_SOUND_ENABLED = "sound_enabled"
    private const val KEY_FAVORITE_GAMES = "favorite_games"
    private const val KEY_TUTORIAL_SHOWN = "tutorial_shown_"
    private const val KEY_GAME_RULE_PREFIX = "game_rule_"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var difficulty: GameDifficulty
        get() {
            val ordinal = prefs.getInt(KEY_DIFFICULTY, GameDifficulty.NORMAL.ordinal)
            return GameDifficulty.entries.getOrElse(ordinal) { GameDifficulty.NORMAL }
        }
        set(value) {
            prefs.edit().putInt(KEY_DIFFICULTY, value.ordinal).apply()
        }

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()
        }

    fun getFavoriteGames(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITE_GAMES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(gameId: String): Boolean {
        val favorites = getFavoriteGames().toMutableSet()
        val isFavorite = if (favorites.contains(gameId)) {
            favorites.remove(gameId)
            false
        } else {
            favorites.add(gameId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITE_GAMES, favorites).apply()
        return isFavorite
    }

    fun isFavorite(gameId: String): Boolean {
        return getFavoriteGames().contains(gameId)
    }

    fun hasSeenTutorial(gameId: String): Boolean {
        return prefs.getBoolean(KEY_TUTORIAL_SHOWN + gameId, false)
    }

    fun setTutorialShown(gameId: String) {
        prefs.edit().putBoolean(KEY_TUTORIAL_SHOWN + gameId, true).apply()
    }

    fun getGameRuleConfig(gameId: String): GameRuleConfig {
        val json = prefs.getString(KEY_GAME_RULE_PREFIX + gameId, null)
        return if (json != null) {
            com.google.gson.Gson().fromJson(json, GameRuleConfig::class.java)
        } else {
            GameRuleConfig(gameId = gameId)
        }
    }

    fun setGameRuleConfig(config: GameRuleConfig) {
        val json = com.google.gson.Gson().toJson(config)
        prefs.edit().putString(KEY_GAME_RULE_PREFIX + config.gameId, json).apply()
    }

    fun getDifficultyThresholds(): DifficultyThresholds {
        return when (difficulty) {
            GameDifficulty.EASY -> DifficultyThresholds(
                winThreshold = 0.5f,
                middleThreshold = 0.25f
            )
            GameDifficulty.NORMAL -> DifficultyThresholds(
                winThreshold = 0.7f,
                middleThreshold = 0.35f
            )
            GameDifficulty.HARD -> DifficultyThresholds(
                winThreshold = 0.85f,
                middleThreshold = 0.5f
            )
        }
    }
}

data class DifficultyThresholds(
    val winThreshold: Float,
    val middleThreshold: Float
)