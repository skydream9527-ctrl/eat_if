package com.eatif.app.domain.model

/**
 * 每周美食周报
 */
data class WeeklyReport(
    val weekStart: String,      // yyyy-MM-dd
    val weekEnd: String,
    val totalGames: Int,         // 本周游戏次数
    val totalFoods: Int,         // 本周吃了多少种不同的美食
    val topFoodName: String,     // 最常吃的美食
    val topFoodCount: Int,       // 最常吃美食的次数
    val avgScorePercent: Int,    // 平均游戏分数
    val bestScorePercent: Int,   // 最高分
    val adoptionRate: Float,     // 推荐采纳率
    val tagDistribution: Map<FoodTag, Int>,  // 标签分布
    val nutritionBalanceScore: Int,  // 营养均衡度 0-100
    val newFoodsTried: Int       // 尝试的新美食数量
) {
    /** 生成周报摘要文案 */
    fun summary(): String = buildString {
        append("本周玩了 $totalGames 局游戏，吃了 $totalFoods 种美食。")
        if (topFoodName.isNotEmpty()) {
            append("最常吃的是「$topFoodName」($topFoodCount 次)。")
        }
        if (nutritionBalanceScore >= 70) {
            append("饮食比较均衡 👍")
        } else if (nutritionBalanceScore >= 40) {
            append("饮食种类可以更丰富一些")
        } else {
            append("饮食比较单一，建议多尝试不同种类 🍽️")
        }
    }
}
