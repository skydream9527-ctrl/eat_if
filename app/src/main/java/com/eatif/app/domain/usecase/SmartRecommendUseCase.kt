package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.model.TimeSlot
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

/**
 * 智能推荐 UseCase
 *
 * 评分维度（加权求和）：
 * 1. 时间槽位历史频次 - 同一时段常吃的加分
 * 2. 标签加成 - 同标签历史频次累计加分
 * 3. 近期惩罚 - 3 天内吃过的降权（避免重复）
 * 4. 营养均衡 - 近期缺失的标签加分（避免口味单一）
 * 5. 用户权重 - food.weight 加分
 * 6. 冷启动 - 无历史时按标签多样化推荐
 * 7. 随机扰动 - 小幅随机避免结果固化
 */
class SmartRecommendUseCase @Inject constructor(
    private val repository: RecommendRepository
) {
    /** 测试可注入随机源，生产环境用默认 Random */
    @Volatile
    internal var random: Random = Random.Default
    operator fun invoke(count: Int = 5): Flow<Result<List<Recommendation>>> {
        val currentTimeSlot = TimeSlot.current()
        val threeDaysAgo = System.currentTimeMillis() - THREE_DAYS_MILLIS
        val sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_MILLIS
        val timeRange = getTimeRangeForSlot(currentTimeSlot)

        return combine(
            repository.getFoodFrequencyBetween(timeRange.first, timeRange.second),
            repository.getFoodFrequencySince(threeDaysAgo),
            repository.getFoodFrequencySince(sevenDaysAgo),
            repository.getAllFoods()
        ) { slotFrequencies, recentFrequencies, weekFrequencies, foods ->
            if (foods.isEmpty()) {
                Result.failure(IllegalStateException("美食库为空"))
            } else {
                val scored = scoreFoods(
                    foods = foods,
                    slotFrequencies = slotFrequencies,
                    recentFrequencies = recentFrequencies,
                    weekFrequencies = weekFrequencies,
                    currentTimeSlot = currentTimeSlot
                )
                Result.success(scored.sortedByDescending { it.score }.take(count))
            }
        }
    }

    /**
     * 纯函数：对美食列表评分。便于单元测试，无 Flow/IO 依赖。
     */
    internal fun scoreFoods(
        foods: List<Food>,
        slotFrequencies: List<FoodFrequency>,
        recentFrequencies: List<FoodFrequency>,
        weekFrequencies: List<FoodFrequency>,
        currentTimeSlot: TimeSlot
    ): List<Recommendation> {
        val recentNames = recentFrequencies.map { it.foodName }.toSet()
        val slotFreqMap = slotFrequencies.associate { it.foodName to it.count }
        val weekTagFreq = computeTagFrequencies(weekFrequencies, foods)
        val isColdStart = recentFrequencies.isEmpty()

        return foods.map { food ->
            val slotScore = (slotFreqMap[food.name] ?: 0).toDouble()
            val tagBonus = calculateTagBonus(food, slotFreqMap, foods)
            val recentPenalty = if (food.name in recentNames) RECENT_PENALTY else 0.0
            val weightBonus = food.weight * WEIGHT_MULTIPLIER
            val nutritionBonus = calculateNutritionBonus(food, weekTagFreq, isColdStart)
            val coldStartBonus = if (isColdStart) calculateColdStartBonus(food, foods, currentTimeSlot) else 0.0
            val randomFactor = random.nextDouble(RANDOM_MIN, RANDOM_MAX)
            val totalScore = slotScore + tagBonus + recentPenalty + weightBonus +
                    nutritionBonus + coldStartBonus + randomFactor
            val reason = buildReason(food, currentTimeSlot, slotFreqMap, isColdStart)
            Recommendation(food = food, reason = reason, score = totalScore)
        }
    }

    /**
     * 营养均衡：近期(7天)吃过的标签频次越低，对应标签的美食加分越多。
     * 冷启动场景下，所有标签频次为 0，该维度退化为均分。
     */
    private fun calculateNutritionBonus(
        food: Food,
        weekTagFreq: Map<FoodTag, Int>,
        isColdStart: Boolean
    ): Double {
        if (food.tags.isEmpty()) return 0.0
        val totalTagCount = weekTagFreq.values.sum().coerceAtLeast(1)
        val maxCount = (weekTagFreq.values.maxOrNull() ?: 0).coerceAtLeast(1)
        return food.tags.sumOf { tag ->
            val count = weekTagFreq[tag] ?: 0
            // 频次越低加分越高，归一化到 [0, NUTRITION_BONUS_MAX]
            val rarity = 1.0 - (count.toDouble() / maxCount)
            rarity * NUTRITION_BONUS_PER_TAG
        }.let {
            if (isColdStart) it / 2 else it // 冷启动时减半，避免压过其他维度
        }
    }

    /**
     * 冷启动：无历史时，按"标签多样化 + 时段匹配"加分。
     * - 早餐时段：LIGHT/RICE/SWEET 加分
     * - 午餐时段：FAST_FOOD/NOODLE/RICE 加分
     * - 晚餐时段：HOTPOT/BBQ/SEAFOOD 加分
     * - 夜宵时段：SPICY/BBQ/NOODLE 加分
     */
    private fun calculateColdStartBonus(food: Food, allFoods: List<Food>, slot: TimeSlot): Double {
        val slotPreferredTags = when (slot) {
            TimeSlot.MORNING -> setOf(FoodTag.LIGHT, FoodTag.RICE, FoodTag.SWEET, FoodTag.DESSERT)
            TimeSlot.LUNCH -> setOf(FoodTag.FAST_FOOD, FoodTag.NOODLE, FoodTag.RICE)
            TimeSlot.DINNER -> setOf(FoodTag.HOTPOT, FoodTag.BBQ, FoodTag.SEAFOOD)
            TimeSlot.LATE_NIGHT -> setOf(FoodTag.SPICY, FoodTag.BBQ, FoodTag.NOODLE)
        }
        val matchCount = food.tags.count { it in slotPreferredTags }
        return matchCount * COLD_START_TAG_MATCH_BONUS
    }

    private fun calculateTagBonus(food: Food, slotFreqMap: Map<String, Int>, allFoods: List<Food>): Double {
        if (food.tags.isEmpty()) return 0.0
        val sameTagFoods = allFoods.filter { it.tags.any { tag -> tag in food.tags } }
        val sameTagFreq = sameTagFoods.sumOf { slotFreqMap[it.name] ?: 0 }
        return sameTagFreq * TAG_BONUS_MULTIPLIER
    }

    /**
     * 基于 7 天历史频次，统计各标签出现次数。
     * 频次来源是 foodName，需映射回 food.tags。
     */
    private fun computeTagFrequencies(
        frequencies: List<FoodFrequency>,
        foods: List<Food>
    ): Map<FoodTag, Int> {
        val foodByName = foods.associateBy { it.name }
        val tagCount = mutableMapOf<FoodTag, Int>()
        frequencies.forEach { freq ->
            val food = foodByName[freq.foodName] ?: return@forEach
            food.tags.forEach { tag ->
                tagCount[tag] = (tagCount[tag] ?: 0) + freq.count
            }
        }
        return tagCount
    }

    private fun buildReason(
        food: Food,
        timeSlot: TimeSlot,
        slotFreqMap: Map<String, Int>,
        isColdStart: Boolean
    ): String {
        val freq = slotFreqMap[food.name] ?: 0
        return when {
            isColdStart && food.tags.isNotEmpty() ->
                "${timeSlot.emoji} ${timeSlot.label}推荐·${food.tags.first().label}类"
            freq >= 3 -> "${timeSlot.emoji} ${timeSlot.label}常选"
            freq >= 1 -> "${timeSlot.emoji} ${timeSlot.label}偶尔吃"
            food.tags.isNotEmpty() -> "${food.tags.first().emoji} ${food.tags.first().label}类"
            else -> "为你推荐"
        }
    }

    private fun getTimeRangeForSlot(slot: TimeSlot): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, slot.hours.first)
        val start = cal.timeInMillis
        val end = start + (slot.hours.last - slot.hours.first + 1) * 3600_000L
        return Pair(start, end)
    }

    companion object {
        private const val THREE_DAYS_MILLIS = 3L * 24 * 3600_000L
        private const val SEVEN_DAYS_MILLIS = 7L * 24 * 3600_000L
        private const val RECENT_PENALTY = -5.0
        private const val WEIGHT_MULTIPLIER = 0.5
        private const val TAG_BONUS_MULTIPLIER = 0.3
        private const val NUTRITION_BONUS_PER_TAG = 1.2
        private const val COLD_START_TAG_MATCH_BONUS = 1.5
        private const val RANDOM_MIN = 0.0
        private const val RANDOM_MAX = 2.0
    }
}
