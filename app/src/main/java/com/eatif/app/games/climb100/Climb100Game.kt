package com.eatif.app.games.climb100

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangeDark
import com.eatif.app.ui.theme.OrangeLight
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun Climb100Game(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val gameState = remember { mutableStateOf(GameState.IDLE) }
    val currentFloor = remember { mutableStateOf(0) }
    val playerY = remember { Animatable(0f) }
    val platforms = remember {
        generatePlatforms(20).toMutableList()
    }
    var platformOffset by remember { mutableStateOf(0) }

    LaunchedEffect(gameState.value) {
        if (gameState.value == GameState.PLAYING) {
            while (gameState.value == GameState.PLAYING && currentFloor.value < 10) {
                delay(1500)
                val targetPlatform = platforms.getOrNull(currentFloor.value + 1) ?: return@LaunchedEffect
                val success = Random.nextFloat() > 0.2f

                if (success) {
                    currentFloor.value++
                    platformOffset = currentFloor.value
                    playerY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                    )
                    if (currentFloor.value >= 10) {
                        gameState.value = GameState.WON
                        delay(500)
                        val randomFood = foods.random().name
                        onResult(randomFood)
                    }
                } else {
                    gameState.value = GameState.LOST
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayMedium.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🧗 勇闯100层",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "当前楼层: ${currentFloor.value} / 10",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(280.dp, 350.dp)
                .background(White, RoundedCornerShape(16.dp))
                .pointerInput(gameState.value) {
                    if (gameState.value == GameState.PLAYING) {
                        detectTapGestures {
                            scope.launch {
                                val success = Random.nextFloat() > 0.15f
                                if (success) {
                                    if (currentFloor.value < 10) {
                                        currentFloor.value++
                                        platformOffset = currentFloor.value
                                    }
                                    if (currentFloor.value >= 10) {
                                        gameState.value = GameState.WON
                                        delay(500)
                                        val randomFood = foods.random().name
                                        onResult(randomFood)
                                    }
                                } else {
                                    playerY.animateTo(200f, tween(500))
                                    delay(300)
                                    gameState.value = GameState.LOST
                                }
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val platformWidth = 80f
                val platformHeight = 16f
                val platformSpacing = 45f
                val startY = size.height - 50f

                platforms.forEachIndexed { index, platformX ->
                    val displayFloor = index - platformOffset
                    if (displayFloor in 0..12) {
                        val y = startY - displayFloor * platformSpacing
                        val isCurrentPlatform = index == currentFloor.value
                        val isReached = index <= currentFloor.value

                        drawRoundRect(
                            color = when {
                                isCurrentPlatform -> OrangePrimary
                                isReached -> Green
                                else -> OrangeLight
                            },
                            topLeft = Offset(platformX, y),
                            size = Size(platformWidth, platformHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )

                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            drawText(
                                "${index + 1}",
                                platformX + platformWidth / 2,
                                y + platformHeight / 2 + 8f,
                                paint
                            )
                        }
                    }
                }

                if (gameState.value != GameState.IDLE) {
                    val playerCenterX = size.width / 2
                    val playerFloor = currentFloor.value - platformOffset
                    val playerCenterY = startY - playerFloor * platformSpacing - 30f

                    drawCircle(
                        color = OrangeDark,
                        radius = 20f,
                        center = Offset(playerCenterX, playerCenterY)
                    )

                    drawCircle(
                        color = White,
                        radius = 8f,
                        center = Offset(playerCenterX - 6f, playerCenterY - 5f)
                    )
                    drawCircle(
                        color = White,
                        radius = 8f,
                        center = Offset(playerCenterX + 6f, playerCenterY - 5f)
                    )
                }
            }

            if (gameState.value == GameState.WON) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 恭喜通关!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Green
                    )
                }
            } else if (gameState.value == GameState.LOST) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😵 掉下去了!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Red
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (gameState.value) {
            GameState.IDLE -> {
                Button(
                    onClick = {
                        currentFloor.value = 0
                        platformOffset = 0
                        gameState.value = GameState.PLAYING
                    },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text("开始攀登", style = MaterialTheme.typography.titleMedium)
                }
            }
            GameState.PLAYING -> {
                Text(
                    text = "👆 点击屏幕跳跃!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayMedium
                )
            }
            GameState.WON, GameState.LOST -> {
                Button(
                    onClick = {
                        currentFloor.value = 0
                        platformOffset = 0
                        playerY.value = 0f
                        gameState.value = GameState.PLAYING
                    },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text("再来一次", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

private fun generatePlatforms(count: Int): List<Float> {
    val platforms = mutableListOf<Float>()
    val centerX = 140f - 40f
    var lastX = centerX
    platforms.add(lastX)

    repeat(count - 1) {
        val offset = Random.nextFloat() * 60f - 30f
        lastX = (lastX + offset).coerceIn(30f, 250f - 80f)
        platforms.add(lastX)
    }
    return platforms
}

private enum class GameState {
    IDLE, PLAYING, WON, LOST
}
