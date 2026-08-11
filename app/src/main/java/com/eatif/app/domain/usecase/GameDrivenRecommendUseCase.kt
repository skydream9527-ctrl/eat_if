package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodAdoption
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.model.TimeSlot
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * 游戏驱动推荐 UseCase - 决策闭环的核心
 *
 * 在 SmartRecommendUseCase 评分基础上，叠加游戏分数维度：
 * - 高分(>=70)：奖励型 - 偏好"重口味/高热量"（SPICY/BBQ/HOTPOT/FAST_FOOD/DESSERT）
 * - 中分(30-69)：中性 - 弱化游戏影响，沿用基础推荐
 * - 低分(<30)：安慰型 - 偏好"清淡/甜"（LIGHT/SWEET/RICE/NOODLE）
 *
 * 输出 Top3 候选，并保证类别多样性（Top3 不全部来自同一 category）。
 */
class GameDrivenRecommendUseCase @Inject constructor(
    private val repository: RecommendRepository,
    private val smartRecommendUseCase: SmartRecommendUseCase
) {
    /**
     * 生成 Top3 推荐候选。
     * @param scorePercent 游戏得分百分比 [0, 100]
     */
    operator fun invoke(scorePercent: Int, count: Int = 3): Flow<Result<List<Recommendation>>> {
        require(scorePercent in 0..100) { "scorePercent 必须在 0..100 之间" }
        val currentTimeSlot = TimeSlot.current()
        val threeDaysAgo = System.currentTimeMillis() - THREE_DAYS_MILLIS
        val sevenDaysAgo = System.currentTimeMillis() - SEVEN_DAYS_MILLIS
        val timeRange = getTimeRangeForSlot(currentTimeSlot)

        return combine(
            repository.getFoodFrequencyBetween(timeRange.first, timeRange.second),
            repository.getFoodFrequencySince(threeDaysAgo),
            repository.getFoodFrequencySince(sevenDaysAgo),
            repository.getAllFoods(),
            repository.getFoodAdoptionStats()
        ) { slotFrequencies, recentFrequencies, weekFrequencies, foods, adoptionStats ->
            if (foods.isEmpty()) {
                Result.failure(IllegalStateException("美食库为空"))
            } else {
                val baseScored = smartRecommendUseCase.scoreFoods(
                    foods = foods,
                    slotFrequencies = slotFrequencies,
                    recentFrequencies = recentFrequencies,
                    weekFrequencies = weekFrequencies,
                    currentTimeSlot = currentTimeSlot,
                    adoptionStats = adoptionStats
                )
                val gameAdjusted = applyGameScoreDimension(baseScored, scorePercent)
                val diversified = ensureCategoryDiversity(gameAdjusted, count)
                Result.success(diversified.take(count))
            }
        }
    }

    /**
     * 纯函数：根据游戏分数调整推荐评分。便于测试。
     */
    internal fun applyGameScoreDimension(
        recommendations: List<Recommendation>,
        scorePercent: Int
    ): List<Recommendation> {
        val mood = GameMood.fromScore(scorePercent)
        return recommendations.map { rec ->
            val moodBonus = calculateMoodBonus(rec.food, mood)
            rec.copy(
                score = rec.score + moodBonus,
                reason = appendMoodReason(rec.reason, mood, scorePercent)
            )
        }
    }

    /**
     * 保证类别多样性：从排序结果中选取，避免 Top-N 全部来自同一 category。
     * 策略：贪心选取，每个 category 最多取 ceil(N/2) 个。
     */
    internal fun ensureCategoryDiversity(
        recommendations: List<Recommendation>,
        targetCount: Int
    ): List<Recommendation> {
        if (recommendations.size <= targetCount) return recommendations
        val maxPerCategory = (targetCount + 1) / 2
        val categoryCount = mutableMapOf<String, Int>()
        val result = mutableListOf<Recommendation>()
        for (rec in recommendations.sortedByDescending { it.score }) {
            val cat = rec.food.category
            val current = categoryCount[cat] ?: 0
            if (current < maxPerCategory) {
                result.add(rec)
                categoryCount[cat] = current + 1
                if (result.size == targetCount) break
            }
        }
        // 若多样性限制导致结果不足，回填剩余高分项
        if (result.size < targetCount) {
            val picked = result.map { it.food.name }.toSet()
            recommendations.sortedByDescending { it.score }
                .filter { it.food.name !in picked }
                .take(targetCount - result.size)
                .forEach { result.add(it) }
        }
        return result
    }

    private fun calculateMoodBonus(food: Food, mood: GameMood): Double {
        if (food.tags.isEmpty()) return 0.0
        return food.tags.sumOf { tag ->
            when (mood) {
                GameMood.REWARD -> if (tag in REWARD_TAGS) MOOD_BONUS else 0.0
                GameMood.COMFORT -> if (tag in COMFORT_TAGS) MOOD_BONUS else 0.0
                GameMood.NEUTRAL -> 0.0
            }
        }
    }

    private fun appendMoodReason(baseReason: String, mood: GameMood, scorePercent: Int): String {
        val moodSuffix = when (mood) {
            GameMood.REWARD -> "·高分奖励"
            GameMood.COMFORT -> "·低分安慰"
            GameMood.NEUTRAL -> ""
        }
        return if (moodSuffix.isEmpty()) baseReason else "$baseReason $moodSuffix"
    }

    private fun getTimeRangeForSlot(slot: TimeSlot): Pair<Long, Long> {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.set(java.util.Calendar.HOUR_OF_DAY, slot.hours.first)
        val start = cal.timeInMillis
        val end = start + (slot.hours.last - slot.hours.first + 1) * 3600_000L
        return Pair(start, end)
    }

    enum class GameMood {
        REWARD,    // 高分 - 奖励型
        NEUTRAL,   // 中分 - 中性
        COMFORT;   // 低分 - 安慰型

        companion object {
            fun fromScore(scorePercent: Int): GameMood = when {
                scorePercent >= REWARD_THRESHOLD -> REWARD
                scorePercent < COMFORT_THRESHOLD -> COMFORT
                else -> NEUTRAL
            }
        }
    }

    companion object {
        private const val THREE_DAYS_MILLIS = 3L * 24 * 3600_000L
        private const val SEVEN_DAYS_MILLIS = 7L * 24 * 3600_000L
        const val REWARD_THRESHOLD = 70
        const val COMFORT_THRESHOLD = 30
        const val MOOD_BONUS = 2.0
        val REWARD_TAGS = setOf(FoodTag.SPICY, FoodTag.BBQ, FoodTag.HOTPOT, FoodTag.FAST_FOOD, FoodTag.DESSERT)
        val COMFORT_TAGS = setOf(FoodTag.LIGHT, FoodTag.SWEET, FoodTag.RICE, FoodTag.NOODLE)
    }
}
