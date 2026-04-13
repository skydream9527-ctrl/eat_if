package com.eatif.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.OrangePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSelectScreen(
    onFoodSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: FoodSelectViewModel = hiltViewModel()
) {
    val foods by viewModel.foods.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎯 选择你的美食",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                text = "恭喜通关！请从下方选择你想要的美食：",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (foods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "美食库为空，请先添加美食",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(foods) { food ->
                        FoodCard(
                            food = food,
                            onClick = { onFoodSelected(food.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCard(
    food: Food,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getFoodEmoji(food.name),
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = food.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getFoodEmoji(foodName: String): String {
    return when {
        foodName.contains("火锅") -> "🍲"
        foodName.contains("寿司") -> "🍣"
        foodName.contains("汉堡") -> "🍔"
        foodName.contains("披萨") || foodName.contains("Pizza") -> "🍕"
        foodName.contains("拉面") || foodName.contains("面条") -> "🍜"
        foodName.contains("饺子") || foodName.contains("包子") -> "🥟"
        foodName.contains("沙拉") -> "🥗"
        foodName.contains("炸鸡") -> "🍗"
        foodName.contains("烤肉") || foodName.contains("烧烤") -> "🥩"
        foodName.contains("咖喱") -> "🍛"
        foodName.contains("串") -> "🍡"
        foodName.contains("粥") -> "🥣"
        foodName.contains("三明治") -> "🥪"
        foodName.contains("蛋糕") || foodName.contains("甜点") -> "🍰"
        foodName.contains("冰淇淋") -> "🍦"
        foodName.contains("咖啡") -> "☕"
        foodName.contains("奶茶") -> "🧋"
        foodName.contains("海鲜") -> "🦐"
        foodName.contains("粤菜") -> "🦐"
        foodName.contains("川菜") -> "🌶️"
        foodName.contains("日餐") || foodName.contains("日式") -> "🍱"
        foodName.contains("西餐") || foodName.contains("西式") -> "🥐"
        foodName.contains("中餐") || foodName.contains("中式") -> "🍜"
        else -> "🍽️"
    }
}
