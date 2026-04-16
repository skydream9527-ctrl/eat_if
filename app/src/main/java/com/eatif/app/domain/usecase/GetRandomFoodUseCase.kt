package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRandomFoodUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    operator fun invoke(count: Int = 1): Flow<Result<List<Food>>> {
        return repository.getAllFoods().map { foods ->
            if (foods.isEmpty()) {
                Result.failure(IllegalStateException("美食库为空"))
            } else {
                val totalWeight = foods.sumOf { it.weight }
                val selected = mutableListOf<Food>()
                repeat(count.coerceAtMost(foods.size)) {
                    var random = (0 until totalWeight).random()
                    for (food in foods) {
                        random -= food.weight
                        if (random < 0) {
                            selected.add(food)
                            break
                        }
                    }
                }
                Result.success(selected.distinctBy { it.id })
            }
        }
    }
}