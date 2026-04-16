package com.eatif.app.domain.usecase

import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFoodCountUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    operator fun invoke(): Flow<Int> = repository.getFoodCount()
}