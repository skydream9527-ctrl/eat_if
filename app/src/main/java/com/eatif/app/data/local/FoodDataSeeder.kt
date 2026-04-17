package com.eatif.app.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object FoodDataSeeder {

    fun getCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                defaultFoods.forEach { food ->
                    db.execSQL(
                        """INSERT INTO foods (name, category, imageUrl, weight, tags)
                           VALUES (?, ?, ?, ?, ?)""",
                        arrayOf(food.name, food.category, food.imageUrl, food.weight, food.tags)
                    )
                }
            }
        }
    }

    private val defaultFoods = listOf(
        FoodEntity(name = "火锅", category = "中餐", imageUrl = null, weight = 3, tags = "[\"HOTPOT\",\"SPICY\"]"),
        FoodEntity(name = "川菜", category = "中餐", imageUrl = null, weight = 3, tags = "[\"SPICY\"]"),
        FoodEntity(name = "粤菜", category = "中餐", imageUrl = null, weight = 2, tags = "[\"LIGHT\"]"),
        FoodEntity(name = "湘菜", category = "中餐", imageUrl = null, weight = 2, tags = "[\"SPICY\"]"),
        FoodEntity(name = "日料", category = "日餐", imageUrl = null, weight = 3, tags = "[\"SEAFOOD\",\"LIGHT\"]"),
        FoodEntity(name = "寿司", category = "日餐", imageUrl = null, weight = 2, tags = "[\"SEAFOOD\"]"),
        FoodEntity(name = "拉面", category = "日餐", imageUrl = null, weight = 2, tags = "[\"NOODLE\"]"),
        FoodEntity(name = "汉堡", category = "西餐", imageUrl = null, weight = 3, tags = "[\"FAST_FOOD\"]"),
        FoodEntity(name = "披萨", category = "西餐", imageUrl = null, weight = 3, tags = "[\"FAST_FOOD\"]"),
        FoodEntity(name = "牛排", category = "西餐", imageUrl = null, weight = 2, tags = "[\"RICE\"]"),
        FoodEntity(name = "意面", category = "西餐", imageUrl = null, weight = 2, tags = "[\"NOODLE\"]"),
        FoodEntity(name = "沙拉", category = "轻食", imageUrl = null, weight = 2, tags = "[\"LIGHT\"]"),
        FoodEntity(name = "三明治", category = "轻食", imageUrl = null, weight = 1, tags = "[\"FAST_FOOD\",\"LIGHT\"]"),
        FoodEntity(name = "炸鸡", category = "快餐", imageUrl = null, weight = 2, tags = "[\"FAST_FOOD\"]"),
        FoodEntity(name = "烤肉", category = "中餐", imageUrl = null, weight = 2, tags = "[\"BBQ\"]"),
        FoodEntity(name = "烧烤", category = "中餐", imageUrl = null, weight = 2, tags = "[\"BBQ\",\"SPICY\"]"),
        FoodEntity(name = "麻辣烫", category = "中餐", imageUrl = null, weight = 2, tags = "[\"SPICY\",\"HOTPOT\"]"),
        FoodEntity(name = "串串", category = "中餐", imageUrl = null, weight = 2, tags = "[\"SPICY\",\"BBQ\"]"),
        FoodEntity(name = "饺子", category = "中餐", imageUrl = null, weight = 2, tags = "[\"RICE\"]"),
        FoodEntity(name = "包子", category = "中餐", imageUrl = null, weight = 1, tags = "[\"RICE\"]"),
        FoodEntity(name = "粥", category = "中餐", imageUrl = null, weight = 1, tags = "[\"LIGHT\"]"),
        FoodEntity(name = "面条", category = "中餐", imageUrl = null, weight = 2, tags = "[\"NOODLE\"]"),
        FoodEntity(name = "咖喱", category = "西餐", imageUrl = null, weight = 1, tags = "[\"SPICY\",\"RICE\"]"),
        FoodEntity(name = "海鲜", category = "中餐", imageUrl = null, weight = 2, tags = "[\"SEAFOOD\"]")
    )
}
