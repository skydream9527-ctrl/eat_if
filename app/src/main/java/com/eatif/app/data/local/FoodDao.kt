package com.eatif.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY id DESC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY id DESC")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE tags LIKE '%' || :tagName || '%' ORDER BY id DESC")
    fun getFoodsByTag(tagName: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFoodById(id: Long)

    @Query("SELECT COUNT(*) FROM foods")
    fun getFoodCount(): Flow<Int>
}
