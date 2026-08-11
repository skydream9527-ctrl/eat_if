package com.eatif.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White

/**
 * 游戏结束时的"选美食"区块 - 公共组件
 *
 * 替代各游戏内重复的：
 * ```
 * foods.take(3).forEach { food ->
 *     Button(onClick = { onResult(food.name, score) }) { Text(food.name) }
 * }
 * ```
 *
 * 决策闭环：foods 列表已由 PlayViewModel 按推荐分排序，
 * take(3) 自动取到推荐 Top3 候选。
 *
 * @param foods 已排序的美食列表（推荐 Top3 在前）
 * @param scorePercent 选择后回调传入的分数
 * @param prompt 引导文案，如"选择一顿美食奖励自己吧"
 * @param accentColor 按钮主色，胜利用 Green，失败用 OrangePrimary
 * @param onResult (foodName, scorePercent) 回调
 */
@Composable
fun FoodChoiceSection(
    foods: List<Food>,
    scorePercent: Int,
    prompt: String,
    accentColor: Color = OrangePrimary,
    onResult: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (foods.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        foods.take(3).forEach { food ->
            Button(
                onClick = { onResult(food.name, scorePercent) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = White
                )
            ) {
                Text(text = food.name, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * 游戏结束提示 + 美食选择的组合区块（更常用的形态）
 */
@Composable
fun GameEndWithFoodChoice(
    title: String,
    titleColor: Color,
    foods: List<Food>,
    scorePercent: Int,
    prompt: String,
    accentColor: Color = OrangePrimary,
    onResult: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = titleColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        FoodChoiceSection(
            foods = foods,
            scorePercent = scorePercent,
            prompt = prompt,
            accentColor = accentColor,
            onResult = onResult
        )
    }
}
