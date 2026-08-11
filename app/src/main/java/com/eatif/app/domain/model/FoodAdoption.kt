package com.eatif.app.domain.model

/**
 * 美食推荐采纳统计 - 用于推荐算法反哺
 * @param foodName 美食名
 * @param adoptedCount 用户从推荐 Top3 中选择该美食的次数
 * @param totalCount 该美食在历史记录中出现的总次数（含自主选择）
 */
data class FoodAdoption(
    val foodName: String,
    val adoptedCount: Int,
    val totalCount: Int
) {
    /** 采纳率 [0, 1] */
    val rate: Float get() = if (totalCount == 0) 0f else adoptedCount.toFloat() / totalCount
}
