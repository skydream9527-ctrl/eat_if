package com.eatif.app.ui.theme

import android.content.Context
import android.content.SharedPreferences

object ThemeManager {
    private const val PREFS_NAME = "eat_if_settings"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_FOLLOW_SYSTEM = "follow_system"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    var followSystem: Boolean
        get() = prefs.getBoolean(KEY_FOLLOW_SYSTEM, true)
        set(value) {
            prefs.edit().putBoolean(KEY_FOLLOW_SYSTEM, value).apply()
        }
}