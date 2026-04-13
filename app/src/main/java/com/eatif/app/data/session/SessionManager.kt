package com.eatif.app.data.session

import com.eatif.app.domain.model.RecommendationResult

/**
 * 会话级单例，存储用户本次游戏选择的店铺列表，并根据游戏得分给出推荐建议。
 * 生命周期与 App 进程一致，无需持久化。
 */
object SessionManager {

    /** 用户在 SetupScreen 配置的候选店铺名列表（3–10个） */
    var shopOptions: List<String> = emptyList()

    /** 是否已完成店铺配置 */
    val isConfigured: Boolean get() = shopOptions.isNotEmpty()

    /**
     * 根据游戏得分百分比（0.0 – 1.0）从候选店铺中推荐一家，并附带理由。
     * 映射规则：
     *   - 得分 ≥ 80% → 排名第一的店铺（奖励自己）
     *   - 得分 40–79% → 随机中间选项
     *   - 得分 < 40% → 最后一个选项（安慰奖）
     *
     * 若未配置店铺，返回 null（由调用方回退旧逻辑）。
     */
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

    /** 重置配置（可选，用于下次游戏重新配置） */
    fun reset() {
        shopOptions = emptyList()
    }
}
