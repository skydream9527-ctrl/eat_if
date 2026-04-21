package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences

object SkinSettingsManager {
    private const val PREFS_NAME = "eat_if_skins"
    private const val KEY_ACTIVE_SKIN_PREFIX = "active_skin_"
    private const val KEY_UNLOCKED = "unlocked_skin_ids"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getActiveSkinId(gameId: String): String? {
        return prefs.getString(KEY_ACTIVE_SKIN_PREFIX + gameId, null)
    }

    fun setActiveSkinId(gameId: String, skinId: String) {
        prefs.edit().putString(KEY_ACTIVE_SKIN_PREFIX + gameId, skinId).apply()
    }

    fun getUnlockedSkinIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED, emptySet())?.toSet() ?: emptySet()
    }

    fun unlockSkin(skinId: String) {
        val current = getUnlockedSkinIds().toMutableSet()
        current.add(skinId)
        prefs.edit().putStringSet(KEY_UNLOCKED, current).apply()
    }

    fun isSkinUnlocked(skinId: String): Boolean = getUnlockedSkinIds().contains(skinId)
}
