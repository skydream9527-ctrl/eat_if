package com.eatif.app.data.repository

import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodEntity
import com.eatif.app.data.local.FoodTagTypeConverter
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {

    override fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFoodsByTag(tag: FoodTag): Flow<List<Food>> {
        return foodDao.getFoodsByTag(tag.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFoodCount(): Flow<Int> {
        return foodDao.getFoodCount()
    }

    override suspend fun addFood(food: Food): Long {
        return foodDao.insertFood(food.toEntity())
    }

    override suspend fun updateFood(food: Food) {
        foodDao.updateFood(food.toEntity())
    }

    override suspend fun deleteFood(id: Long) {
        foodDao.deleteFoodById(id)
    }

    private fun FoodEntity.toDomain(): Food {
        val converter = FoodTagTypeConverter()
        return Food(
            id = id,
            name = name,
            category = category,
            imageUrl = imageUrl,
            weight = weight,
            tags = converter.toFoodTags(tags)
        )
    }

    private fun Food.toEntity(): FoodEntity {
        val converter = FoodTagTypeConverter()
        return FoodEntity(
            id = id,
            name = name,
            category = category,
            imageUrl = imageUrl,
            weight = weight,
            tags = converter.fromFoodTags(tags)
        )
    }
}
