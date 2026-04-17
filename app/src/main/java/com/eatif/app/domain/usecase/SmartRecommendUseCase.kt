package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.model.TimeSlot
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

class SmartRecommendUseCase @Inject constructor(
    private val repository: RecommendRepository
) {
    operator fun invoke(count: Int = 5): Flow<Result<List<Recommendation>>> {
        val currentTimeSlot = TimeSlot.current()
        val threeDaysAgo = System.currentTimeMillis() - THREE_DAYS_MILLIS
        val timeRange = getTimeRangeForSlot(currentTimeSlot)

        return combine(
            repository.getFoodFrequencyBetween(timeRange.first, timeRange.second),
            repository.getFoodFrequencySince(threeDaysAgo),
            repository.getAllFoods()
        ) { slotFrequencies, recentFrequencies, foods ->
            if (foods.isEmpty()) {
                Result.failure(IllegalStateException("美食库为空"))
            } else {
                val recentNames = recentFrequencies.map { it.foodName }.toSet()
                val slotFreqMap = slotFrequencies.associate { it.foodName to it.count }
                val scored = foods.map { food ->
                    val slotScore = (slotFreqMap[food.name] ?: 0).toDouble()
                    val tagBonus = calculateTagBonus(food, slotFreqMap, foods)
                    val recentPenalty = if (food.name in recentNames) RECENT_PENALTY else 0.0
                    val weightBonus = food.weight * WEIGHT_MULTIPLIER
                    val randomFactor = Random.nextDouble(RANDOM_MIN, RANDOM_MAX)
                    val totalScore = slotScore + tagBonus + recentPenalty + weightBonus + randomFactor
                    val reason = buildReason(food, currentTimeSlot, slotFreqMap)
                    Recommendation(food = food, reason = reason, score = totalScore)
                }
                Result.success(scored.sortedByDescending { it.score }.take(count))
            }
        }
    }

    private fun calculateTagBonus(food: Food, slotFreqMap: Map<String, Int>, allFoods: List<Food>): Double {
        if (food.tags.isEmpty()) return 0.0
        val sameTagFoods = allFoods.filter { it.tags.any { tag -> tag in food.tags } }
        val sameTagFreq = sameTagFoods.sumOf { slotFreqMap[it.name] ?: 0 }
        return sameTagFreq * TAG_BONUS_MULTIPLIER
    }

    private fun buildReason(food: Food, timeSlot: TimeSlot, slotFreqMap: Map<String, Int>): String {
        val freq = slotFreqMap[food.name] ?: 0
        return when {
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
        private const val RECENT_PENALTY = -5.0
        private const val WEIGHT_MULTIPLIER = 0.5
        private const val TAG_BONUS_MULTIPLIER = 0.3
        private const val RANDOM_MIN = 0.0
        private const val RANDOM_MAX = 2.0
    }
}
