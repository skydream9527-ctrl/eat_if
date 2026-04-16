package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import javax.inject.Inject

class UpdateFoodWeightUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(food: Food, newWeight: Int): Result<Unit> {
        return try {
            if (newWeight < 1 || newWeight > 10) {
                Result.failure(IllegalArgumentException("权重必须在1-10之间"))
            } else {
                repository.updateFood(food.copy(weight = newWeight))
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}