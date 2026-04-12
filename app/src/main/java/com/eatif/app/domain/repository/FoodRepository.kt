package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<Food>>
    fun getFoodCount(): Flow<Int>
    suspend fun addFood(food: Food): Long
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(id: Long)
}
