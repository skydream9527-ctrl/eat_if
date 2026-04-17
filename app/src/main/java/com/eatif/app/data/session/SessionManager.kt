package com.eatif.app.data.session

import android.content.Context
import android.content.SharedPreferences
import com.eatif.app.domain.model.RecommendationResult

object SessionManager {

    private const val PREFS_NAME = "eat_if_session"
    private const val KEY_SHOP_OPTIONS = "shop_options"
    private const val DELIMITER = "||"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var shopOptions: List<String>
        get() {
            val raw = prefs?.getString(KEY_SHOP_OPTIONS, null) ?: return emptyList()
            return raw.split(DELIMITER).filter { it.isNotBlank() }
        }
        set(value) {
            prefs?.edit()?.putString(KEY_SHOP_OPTIONS, value.joinToString(DELIMITER))?.apply()
        }

    val isConfigured: Boolean get() = shopOptions.isNotEmpty()

    fun getRecommendation(scorePercent: Float): RecommendationResult? {
        if (shopOptions.isEmpty()) return null

        val shops = shopOptions
        val shop: String
        val reason: String
        val emoji: String

        when {
            scorePercent >= 0.8f -> {
                shop = shops.first()
                reason = "你发挥超群！今天奖励自己去"
                emoji = "🏆"
            }
            scorePercent >= 0.4f -> {
                val midShops = if (shops.size > 2) shops.subList(1, shops.size - 1) else shops
                shop = midShops.random()
                reason = "发挥不错，今天就去"
                emoji = "😋"
            }
            else -> {
                shop = shops.last()
                reason = "继续努力！今天的安慰奖是"
                emoji = "💪"
            }
        }

        return RecommendationResult(
            shopName = shop,
            reason = reason,
            scorePercent = scorePercent,
            emoji = emoji
        )
    }

    fun reset() {
        prefs?.edit()?.remove(KEY_SHOP_OPTIONS)?.apply()
    }
}
