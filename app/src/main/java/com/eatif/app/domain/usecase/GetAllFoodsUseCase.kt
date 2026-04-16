package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllFoodsUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    operator fun invoke(): Flow<List<Food>> = repository.getAllFoods()
}