package com.eatif.app.ui.onboarding

import android.content.Context
import android.content.SharedPreferences

object OnboardingManager {
    private const val PREFS_NAME = "eat_if_onboarding"
    private const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    private const val KEY_CURRENT_VERSION = "app_version"
    private const val KEY_SHOW_WHATS_NEW = "show_whats_new"
    private const val CURRENT_VERSION = 2

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    val hasSeenOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)

    fun setOnboardingComplete() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
    }

    fun shouldShowWhatsNew(): Boolean {
        val lastVersion = prefs.getInt(KEY_CURRENT_VERSION, 0)
        return lastVersion < CURRENT_VERSION
    }

    fun setWhatsNewShown() {
        prefs.edit().putInt(KEY_CURRENT_VERSION, CURRENT_VERSION).apply()
    }

    val showWhatsNew: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WHATS_NEW, false)

    fun setShowWhatsNew(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WHATS_NEW, show).apply()
    }
}

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

object OnboardingPages {
    val pages = listOf(
        OnboardingPage(
            emoji = "🍽️",
            title = "今天吃什么？",
            description = "还在为每天吃什么而纠结吗？让小游戏帮你做决定！"
        ),
        OnboardingPage(
            emoji = "🎮",
            title = "选择游戏",
            description = "从15款趣味小游戏中挑选一个来挑战，有精准类、益智类、运气类..."
        ),
        OnboardingPage(
            emoji = "🏆",
            title = "获得推荐",
            description = "根据游戏得分，获得专属的美食推荐！发挥越好，奖励越丰富~"
        ),
        OnboardingPage(
            emoji = "👥",
            title = "双人竞技",
            description = "和朋友一起玩？没问题！双人模式让你们一起决定吃什么！"
        )
    )
}

data class WhatsNewItem(
    val emoji: String,
    val title: String,
    val description: String
)

object WhatsNewItems {
    val items = listOf(
        WhatsNewItem(
            emoji = "🎯",
            title = "游戏难度选择",
            description = "简单/普通/困难三种难度，影响通关分数要求"
        ),
        WhatsNewItem(
            emoji = "📖",
            title = "游戏教程",
            description = "复杂游戏新增新手教程，首次进入自动显示"
        ),
        WhatsNewItem(
            emoji = "❤️",
            title = "收藏游戏",
            description = "长按游戏卡片收藏，常玩游戏一触即达"
        ),
        WhatsNewItem(
            emoji = "🔊",
            title = "音效与振动",
            description = "游戏音效和触感反馈可自由开关"
        )
    )
}