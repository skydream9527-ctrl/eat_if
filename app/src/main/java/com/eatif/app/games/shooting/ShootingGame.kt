package com.eatif.app.games.shooting

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun ShootingGame(
    foods: List<Food>,
    onResult: (String, Int) -> Unit
) {
    var shotsRemaining by remember { mutableStateOf(5) }
    var totalScore by remember { mutableStateOf(0) }
    var lastShotScore by remember { mutableStateOf(0) }
    var lastHitOffset by remember { mutableStateOf(Offset.Zero) }
    var isGameOver by remember { mutableStateOf(false) }
    var showHitEffect by remember { mutableStateOf(false) }
    val hitEffectScale = remember { Animatable(0f) }

    val targetRadius = 120f
    val ringWidth = targetRadius / 5

    val ringScores = listOf(100, 75, 50, 25, 10)
    val ringColors = listOf(
        Red,
        Color(0xFFFF6B6B),
        Color(0xFFFF8E8E),
        Color(0xFFFFB4B4),
        Color(0xFFFFD4D4)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎯 打靶",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "剩余: $shotsRemaining 发 | 总分: $totalScore",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showHitEffect && !isGameOver) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    lastShotScore >= 100 -> "🎯 完美! +$lastShotScore"
                    lastShotScore >= 75 -> "✨ 很好! +$lastShotScore"
                    lastShotScore >= 50 -> "👍 不错! +$lastShotScore"
                    lastShotScore >= 25 -> "😅 一般 +$lastShotScore"
                    else -> "💨 脱靶 +$lastShotScore"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(280.dp)
                .pointerInput(shotsRemaining, isGameOver) {
                    if (shotsRemaining > 0 && !isGameOver) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val center = Offset(centerX, centerY)

                            val distance = sqrt(
                                (offset.x - center.x).pow(2) +
                                (offset.y - center.y).pow(2)
                            )

                            lastHitOffset = offset - center

                            val score = when {
                                distance <= ringWidth -> ringScores[0]
                                distance <= ringWidth * 2 -> ringScores[1]
                                distance <= ringWidth * 3 -> ringScores[2]
                                distance <= ringWidth * 4 -> ringScores[3]
                                distance <= targetRadius -> ringScores[4]
                                else -> 0
                            }

                            lastShotScore = score
                            totalScore += score
                            shotsRemaining--

                            showHitEffect = true

                            if (shotsRemaining == 0) {
                                isGameOver = true
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.size(260.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                for (i in 4 downTo 0) {
                    val radius = ringWidth * (i + 1)
                    drawCircle(
                        color = ringColors[4 - i],
                        radius = radius,
                        center = Offset(centerX, centerY)
                    )
                }

                drawCircle(
                    color = White,
                    radius = 6f,
                    center = Offset(centerX, centerY)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isGameOver) {
            val passThreshold = 250
            val passed = totalScore >= passThreshold

            Text(
                text = if (passed) "🎉 通过! 总分: $totalScore" else "😢 未通过 总分: $totalScore",
                style = MaterialTheme.typography.headlineSmall,
                color = if (passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    shotsRemaining = 5
                    totalScore = 0
                    lastShotScore = 0
                    isGameOver = false
                    showHitEffect = false
                },
                modifier = Modifier.size(width = 200.dp, height = 56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = White
                )
            ) {
                Text(
                    text = "重新开始",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (passed && foods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val selectedFood = foods.random().name
                        // 满分 500 分，转换为 0-100 百分比
                        val scorePercent = (totalScore * 100 / 500).coerceIn(0, 100)
                        onResult(selectedFood, scorePercent)
                    },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = "领取食物",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            Text(
                text = "点击靶子射击",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
