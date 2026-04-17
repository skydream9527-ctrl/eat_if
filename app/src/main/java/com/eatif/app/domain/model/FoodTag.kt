package com.eatif.app.domain.model

enum class FoodTag(val label: String, val emoji: String) {
    SPICY("辣", "🌶️"),
    SWEET("甜", "🍰"),
    FAST_FOOD("快餐", "🍔"),
    HOTPOT("火锅", "🍲"),
    BBQ("烧烤", "🍖"),
    NOODLE("面食", "🍜"),
    RICE("米饭", "🍚"),
    LIGHT("清淡", "🥗"),
    SEAFOOD("海鲜", "🦐"),
    DESSERT("甜点", "🍦")
}
