package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodTag
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<Food>>
    fun getFoodsByTag(tag: FoodTag): Flow<List<Food>>
    fun getFoodCount(): Flow<Int>
    suspend fun addFood(food: Food): Long
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(id: Long)
}
