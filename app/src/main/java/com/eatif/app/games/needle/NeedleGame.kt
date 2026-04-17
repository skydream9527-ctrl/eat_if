package com.eatif.app.games.needle

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gray
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.math.cos
import kotlin.math.sin

private enum class NeedleGameState { PLAYING, WON, LOST }

@Composable
fun NeedleGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val needles = remember { mutableStateOf<List<Float>>(emptyList()) }
    val currentAngle = remember { mutableStateOf(0f) }
    val spinning = remember { mutableStateOf(false) }
    val animatableAngle = remember { Animatable(0f) }
    val score = remember { mutableStateOf(0) }
    var gameResult by remember { mutableStateOf<NeedleGameState>(NeedleGameState.PLAYING) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused
    val targetScore = 5

    LaunchedEffect(spinning.value) {
        if (spinning.value) {
            val targetAngle = animatableAngle.value + 1440f
            animatableAngle.animateTo(
                targetValue = targetAngle,
                animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
            )
            currentAngle.value = (animatableAngle.value % 360f + 360f) % 360f
            spinning.value = false

            val newNeedleAngle = currentAngle.value
            val collision = needles.value.any { existingAngle ->
                val diff = kotlin.math.abs(newNeedleAngle - existingAngle)
                val minDiff = minOf(diff, 360f - diff)
                minDiff < 25f
            }

            if (collision) {
                gameResult = NeedleGameState.LOST
            } else {
                needles.value = needles.value + newNeedleAngle
                score.value++

                if (score.value >= targetScore) {
                    gameResult = NeedleGameState.WON
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎯 见缝插针",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "分数: ${score.value} / $targetScore",
            style = MaterialTheme.typography.titleLarge,
            color = when (gameResult) {
                NeedleGameState.LOST -> Red
                NeedleGameState.WON -> Green
                else -> Green
            }
        )

        when (gameResult) {
            NeedleGameState.WON -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🎉 通关!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green
                )
                if (foods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "选择一顿美食奖励自己吧:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    foods.take(3).forEach { food ->
                        Button(
                            onClick = { onResult(food.name, (score.value * 100 / targetScore).coerceIn(0, 100)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = White
                            )
                        ) {
                            Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            NeedleGameState.LOST -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💥 碰撞! 游戏结束",
                    style = MaterialTheme.typography.titleMedium,
                    color = Red
                )
                if (foods.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "选择一顿美食安慰自己吧:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    foods.take(3).forEach { food ->
                        Button(
                            onClick = { onResult(food.name, (score.value * 100 / targetScore).coerceIn(0, 100)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = White
                            )
                        ) {
                            Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(280.dp)) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val diskRadius = minOf(size.width, size.height) / 2 - 20

                drawCircle(
                    color = Gray.copy(alpha = 0.3f),
                    radius = diskRadius,
                    center = Offset(centerX, centerY)
                )

                drawCircle(
                    color = Gray,
                    radius = diskRadius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = 4f)
                )

                needles.value.forEach { angle ->
                    val collisionStartAngle = angle - 25f
                    val sweepAngle = 50f
                    val arcRadius = diskRadius * 0.7f
                    drawArc(
                        color = Red.copy(alpha = 0.15f),
                        startAngle = collisionStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(centerX - arcRadius, centerY - arcRadius),
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)
                    )
                }

                rotate(animatableAngle.value, Offset(centerX, centerY)) {
                    needles.value.forEach { angle ->
                        val radians = Math.toRadians(angle.toDouble())
                        val needleLength = diskRadius * 0.85f
                        val endX = centerX + (needleLength * cos(radians)).toFloat()
                        val endY = centerY + (needleLength * sin(radians)).toFloat()

                        drawLine(
                            color = OrangePrimary,
                            start = Offset(centerX, centerY),
                            end = Offset(endX, endY),
                            strokeWidth = 6f
                        )

                        drawCircle(
                            color = Red,
                            radius = 8f,
                            center = Offset(endX, endY)
                        )
                    }
                }

                drawCircle(
                    color = OrangePrimary,
                    radius = 15f,
                    center = Offset(centerX, centerY)
                )
            }

            if (spinning.value) {
                Canvas(modifier = Modifier.size(280.dp)) {
                    val centerX = size.width / 2
                    val trianglePath = Path().apply {
                        moveTo(centerX, 20f)
                        lineTo(centerX - 15f, 45f)
                        lineTo(centerX + 15f, 45f)
                        close()
                    }
                    drawPath(trianglePath, Red)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { internalPaused = !internalPaused },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (actualPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (actualPaused) "继续" else "暂停",
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            when (gameResult) {
                NeedleGameState.LOST -> {
                    Button(
                        onClick = {
                            needles.value = emptyList()
                            currentAngle.value = 0f
                            score.value = 0
                            gameResult = NeedleGameState.PLAYING
                        },
                        modifier = Modifier.size(width = 160.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text("重新开始", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> {
                    Button(
                        onClick = {
                            if (!spinning.value && gameResult == NeedleGameState.PLAYING && score.value < targetScore) {
                                spinning.value = true
                            }
                        },
                        enabled = !spinning.value && gameResult == NeedleGameState.PLAYING && score.value < targetScore && foods.isNotEmpty(),
                        modifier = Modifier.size(width = 160.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White,
                            disabledContainerColor = Gray.copy(alpha = 0.5f),
                            disabledContentColor = White.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = when {
                                gameResult == NeedleGameState.WON -> "完成!"
                                else -> "插入"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
