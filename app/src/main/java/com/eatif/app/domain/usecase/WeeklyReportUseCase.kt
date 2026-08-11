package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodAdoption
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.domain.model.WeeklyReport
import com.eatif.app.domain.repository.FoodRepository
import com.eatif.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import javax.inject.Inject

/**
 * 每周美食周报 UseCase
 *
 * 聚合过去 7 天的历史数据，生成周报。
 */
class WeeklyReportUseCase @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val foodRepository: FoodRepository
) {
    /**
     * 获取本周周报（过去 7 天）
     */
    operator fun invoke() = combine(
        historyRepository.getFoodFrequencySince(weekStartMillis()),
        historyRepository.getFoodAdoptionStats(),
        foodRepository.getAllFoods()
    ) { weekFrequencies, adoptionStats, foods ->
        generateReport(weekFrequencies, adoptionStats, foods)
    }

    /**
     * 纯函数：生成周报。便于测试。
     */
    internal fun generateReport(
        weekFrequencies: List<FoodFrequency>,
        adoptionStats: List<FoodAdoption>,
        foods: List<Food>
    ): WeeklyReport {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)

        val totalGames = weekFrequencies.sumOf { it.count }
        val sortedByCount = weekFrequencies.sortedByDescending { it.count }
        val topFood = sortedByCount.firstOrNull()
        val totalFoods = weekFrequencies.size

        // 标签分布
        val foodByName = foods.associateBy { it.name }
        val tagDistribution = mutableMapOf<FoodTag, Int>()
        weekFrequencies.forEach { freq ->
            val food = foodByName[freq.foodName] ?: return@forEach
            food.tags.forEach { tag ->
                tagDistribution[tag] = (tagDistribution[tag] ?: 0) + freq.count
            }
        }

        // 营养均衡度：基于标签多样性
        val nutritionBalanceScore = calculateNutritionBalance(tagDistribution, totalGames)

        // 推荐采纳率
        val adopted = adoptionStats.sumOf { it.adoptedCount }
        val totalAdoption = adoptionStats.sumOf { it.totalCount }
        val adoptionRate = if (totalAdoption == 0) 0f else adopted.toFloat() / totalAdoption

        return WeeklyReport(
            weekStart = weekStart.toString(),
            weekEnd = today.toString(),
            totalGames = totalGames,
            totalFoods = totalFoods,
            topFoodName = topFood?.foodName ?: "",
            topFoodCount = topFood?.count ?: 0,
            avgScorePercent = 0,  // 需要 GameStats 数据，暂设 0
            bestScorePercent = 0,
            adoptionRate = adoptionRate,
            tagDistribution = tagDistribution,
            nutritionBalanceScore = nutritionBalanceScore,
            newFoodsTried = 0  // 需要对比历史，暂设 0
        )
    }

    /**
     * 营养均衡度计算：
     * - 基于标签分布的熵（越均匀越高）
     * - 归一化到 0-100
     */
    private fun calculateNutritionBalance(
        tagDistribution: Map<FoodTag, Int>,
        totalGames: Int
    ): Int {
        if (totalGames == 0 || tagDistribution.isEmpty()) return 0
        if (tagDistribution.size == 1) return 20  // 只吃一类，很低分

        // 使用 Shannon 熵
        val entropy = tagDistribution.values.sumOf { count ->
            val p = count.toDouble() / totalGames
            if (p > 0) -p * Math.log(p) else 0.0
        }
        // 最大熵 = log(标签种类数)
        val maxEntropy = Math.log(tagDistribution.size.toDouble())
        val normalized = if (maxEntropy > 0) entropy / maxEntropy else 0.0
        return (normalized * 100).toInt().coerceIn(0, 100)
    }

    private fun weekStartMillis(): Long {
        val weekStart = LocalDate.now().minusDays(6)
        return weekStart.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}
