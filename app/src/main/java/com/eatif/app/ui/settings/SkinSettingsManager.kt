package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences
import com.eatif.app.data.local.SkinCollectionDao
import com.eatif.app.data.local.SkinCollectionEntity

object SkinSettingsManager {
    private const val PREFS_NAME = "eat_if_skins"
    private const val KEY_UNLOCKED = "unlocked_skin_ids"

    private lateinit var prefs: SharedPreferences
    private var dao: SkinCollectionDao? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setDao(skinCollectionDao: SkinCollectionDao) {
        dao = skinCollectionDao
    }

    fun getUnlockedSkinIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()
    }

    fun unlockSkin(skinId: String) {
        val current = getUnlockedSkinIds().toMutableSet()
        current.add(skinId)
        prefs.edit().putStringSet(KEY_UNLOCKED, current).apply()
    }

    fun isSkinUnlocked(skinId: String): Boolean = getUnlockedSkinIds().contains(skinId)
}
