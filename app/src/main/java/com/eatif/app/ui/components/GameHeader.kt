package com.eatif.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 游戏顶部标题 + 分数显示 - 公共组件
 *
 * 替代各游戏内重复的：
 * ```
 * Text(text = "🎮 游戏名", style = headlineMedium)
 * Spacer(...)
 * Text(text = "分数: $score", style = titleMedium, color = OrangePrimary)
 * ```
 */
@Composable
fun GameHeader(
    title: String,
    scoreText: String? = null,
    scoreColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        if (scoreText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scoreText,
                style = MaterialTheme.typography.titleMedium,
                color = scoreColor
            )
        }
    }
}
