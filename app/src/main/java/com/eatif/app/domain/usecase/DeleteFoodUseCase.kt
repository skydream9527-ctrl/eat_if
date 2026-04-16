package com.eatif.app.domain.usecase

import com.eatif.app.domain.repository.FoodRepository
import javax.inject.Inject

class DeleteFoodUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.deleteFood(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}