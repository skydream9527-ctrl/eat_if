package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import javax.inject.Inject

class UpdateFoodUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(food: Food): Result<Unit> {
        return try {
            if (food.name.isBlank()) {
                Result.failure(IllegalArgumentException("美食名称不能为空"))
            } else if (food.category.isBlank()) {
                Result.failure(IllegalArgumentException("分类不能为空"))
            } else {
                repository.updateFood(food)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}