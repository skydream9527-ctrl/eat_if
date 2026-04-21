package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences

object AchievementSettingsManager {
    private const val PREFS_NAME = "eat_if_achievements"
    private const val KEY_UNLOCKED = "unlocked_ids"
    private const val KEY_LAST_PLAYED = "last_played_date"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUnlockedIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()
    }

    fun markUnlocked(id: String) {
        val current = getUnlockedIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_UNLOCKED, current).apply()
    }

    fun isUnlocked(id: String): Boolean = getUnlockedIds().contains(id)

    fun setLastPlayedDate(date: String) {
        prefs.edit().putString(KEY_LAST_PLAYED, date).apply()
    }

    fun getLastPlayedDate(): String {
        return prefs.getString(KEY_LAST_PLAYED, "") ?: ""
    }
}
