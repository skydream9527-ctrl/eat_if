package com.eatif.app.domain.model

data class Food(
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val weight: Int = 1,
    val tags: List<FoodTag> = emptyList()
)

object DefaultFoods {
    val list = listOf(
        Food(name = "火锅", category = "中餐"),
        Food(name = "寿司", category = "日餐"),
        Food(name = "汉堡", category = "西餐"),
        Food(name = "披萨", category = "西餐"),
        Food(name = "拉面", category = "日餐"),
        Food(name = "饺子", category = "中餐"),
        Food(name = "沙拉", category = "西餐"),
        Food(name = "炸鸡", category = "西餐")
    )
}
