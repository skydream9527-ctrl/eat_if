package com.eatif.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.ui.theme.OrangePrimary

/**
 * Onboarding 美食导入页 - 让用户快速勾选常吃的美食，降低冷启动流失
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingFoodImportScreen(
    onImportComplete: (List<Food>) -> Unit,
    onSkip: () -> Unit
) {
    // 预设美食池：按类别组织
    val presetFoods = remember { OnboardingPresetFoods.list }
    val selectedNames = remember { mutableStateOf(setOf<String>()) }
    var selectedCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "🍽️ 选择你常吃的美食",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "勾选 3-5 道菜，让推荐更懂你（已选 $selectedCount 道）",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(presetFoods) { categoryGroup ->
                Text(
                    text = categoryGroup.category,
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryGroup.foods.forEach { food ->
                        val isSelected = food.name in selectedNames.value
                        FoodChip(
                            name = food.name,
                            isSelected = isSelected,
                            onClick = {
                                val current = selectedNames.value
                                val newSet = if (isSelected) {
                                    current - food.name
                                } else {
                                    current + food.name
                                }
                                selectedNames.value = newSet
                                selectedCount = newSet.size
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSkip,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("跳过")
            }
            Button(
                onClick = {
                    val selectedFoods = presetFoods
                        .flatMap { it.foods }
                        .filter { it.name in selectedNames.value }
                    onImportComplete(selectedFoods)
                },
                modifier = Modifier.weight(2f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = Color.White
                )
            ) {
                Text("导入 ${if (selectedCount > 0) "($selectedCount)" else ""}")
            }
        }
    }
}

@Composable
private fun FoodChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) OrangePrimary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) OrangePrimary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = if (isSelected) "✓ $name" else name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) OrangePrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** 预设美食池 */
data class FoodCategoryGroup(val category: String, val foods: List<Food>)

object OnboardingPresetFoods {
    val list = listOf(
        FoodCategoryGroup(
            category = "🌶️ 中餐",
            foods = listOf(
                Food(name = "火锅", category = "中餐", tags = listOf(FoodTag.HOTPOT, FoodTag.SPICY)),
                Food(name = "川菜", category = "中餐", tags = listOf(FoodTag.SPICY)),
                Food(name = "湘菜", category = "中餐", tags = listOf(FoodTag.SPICY)),
                Food(name = "粤菜", category = "中餐", tags = listOf(FoodTag.LIGHT)),
                Food(name = "烤肉", category = "中餐", tags = listOf(FoodTag.BBQ)),
                Food(name = "烧烤", category = "中餐", tags = listOf(FoodTag.BBQ, FoodTag.SPICY)),
                Food(name = "麻辣烫", category = "中餐", tags = listOf(FoodTag.SPICY, FoodTag.HOTPOT)),
                Food(name = "饺子", category = "中餐", tags = listOf(FoodTag.RICE)),
                Food(name = "面条", category = "中餐", tags = listOf(FoodTag.NOODLE)),
                Food(name = "粥", category = "中餐", tags = listOf(FoodTag.LIGHT))
            )
        ),
        FoodCategoryGroup(
            category = "🍣 日餐",
            foods = listOf(
                Food(name = "日料", category = "日餐", tags = listOf(FoodTag.SEAFOOD, FoodTag.LIGHT)),
                Food(name = "寿司", category = "日餐", tags = listOf(FoodTag.SEAFOOD)),
                Food(name = "拉面", category = "日餐", tags = listOf(FoodTag.NOODLE))
            )
        ),
        FoodCategoryGroup(
            category = "🍔 西餐/快餐",
            foods = listOf(
                Food(name = "汉堡", category = "西餐", tags = listOf(FoodTag.FAST_FOOD)),
                Food(name = "披萨", category = "西餐", tags = listOf(FoodTag.FAST_FOOD)),
                Food(name = "牛排", category = "西餐", tags = listOf(FoodTag.RICE)),
                Food(name = "意面", category = "西餐", tags = listOf(FoodTag.NOODLE)),
                Food(name = "炸鸡", category = "快餐", tags = listOf(FoodTag.FAST_FOOD))
            )
        ),
        FoodCategoryGroup(
            category = "🥗 轻食",
            foods = listOf(
                Food(name = "沙拉", category = "轻食", tags = listOf(FoodTag.LIGHT)),
                Food(name = "三明治", category = "轻食", tags = listOf(FoodTag.FAST_FOOD, FoodTag.LIGHT))
            )
        )
    )
}
