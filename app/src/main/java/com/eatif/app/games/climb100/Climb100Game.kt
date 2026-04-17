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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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

private const val TARGET_FLOOR = 20

@Composable
fun Climb100Game(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val scope = rememberCoroutineScope()
    val gameState = remember { mutableStateOf(GameState.IDLE) }
    val currentFloor = remember { mutableStateOf(0) }
    val playerY = remember { Animatable(0f) }
    val platforms = remember {
        generatePlatforms(TARGET_FLOOR).toMutableList()
    }
    val obstacleFloors = remember {
        generateObstacleFloors(TARGET_FLOOR).toMutableList()
    }
    var platformOffset by remember { mutableStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }
    var obstacleWarning by remember { mutableStateOf(false) }

    val actualPaused = isPaused || internalPaused

    LaunchedEffect(gameState.value, currentFloor.value, actualPaused) {
        if (gameState.value == GameState.PLAYING && !actualPaused) {
            val nextFloor = currentFloor.value + 1
            if (nextFloor < TARGET_FLOOR && nextFloor in obstacleFloors) {
                obstacleWarning = true
                delay(500)
                obstacleWarning = false
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
            text = "当前楼层: ${currentFloor.value} / $TARGET_FLOOR",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(280.dp, 350.dp)
                .background(
                    if (obstacleWarning && gameState.value == GameState.PLAYING) Red.copy(alpha = 0.15f) else White,
                    RoundedCornerShape(16.dp)
                )
                .pointerInput(gameState.value) {
                    if (gameState.value == GameState.PLAYING) {
                        detectTapGestures {
                            scope.launch {
                                val nextFloor = currentFloor.value + 1
                                if (nextFloor in obstacleFloors) {
                                    playerY.animateTo(200f, tween(500))
                                    delay(300)
                                    gameState.value = GameState.LOST
                                } else {
                                    if (currentFloor.value < TARGET_FLOOR) {
                                        currentFloor.value++
                                        platformOffset = currentFloor.value
                                    }
                                    if (currentFloor.value >= TARGET_FLOOR) {
                                        gameState.value = GameState.WON
                                    }
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
                        val isObstacle = index in obstacleFloors

                        drawRoundRect(
                            color = when {
                                isCurrentPlatform -> OrangePrimary
                                isReached -> Green
                                isObstacle -> Red
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
                                if (isObstacle && !isReached) "⚠" else "${index + 1}",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    if (foods.isNotEmpty()) {
                        Text(
                            text = "选择一顿美食奖励自己吧:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        foods.take(3).forEach { food ->
                            Button(
                                onClick = { onResult(food.name, 100) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 4.dp),
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
            } else if (gameState.value == GameState.LOST) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😵 掉下去了!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (foods.isNotEmpty()) {
                        Text(
                            text = "选择一顿美食安慰自己吧:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        foods.take(3).forEach { food ->
                            Button(
                                onClick = { onResult(food.name, (currentFloor.value * 100 / TARGET_FLOOR).coerceIn(0, 100)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 4.dp),
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
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (gameState.value) {
            GameState.IDLE -> {
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

                    Button(
                        onClick = {
                            currentFloor.value = 0
                            platformOffset = 0
                            obstacleFloors.clear()
                            obstacleFloors.addAll(generateObstacleFloors(TARGET_FLOOR))
                            platforms.clear()
                            platforms.addAll(generatePlatforms(TARGET_FLOOR))
                            gameState.value = GameState.PLAYING
                        },
                        modifier = Modifier.size(width = 160.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text("开始攀登", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            GameState.PLAYING -> {
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

                    Text(
                        text = if (obstacleWarning) "🚫 小心障碍!" else "👆 点击屏幕跳跃!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (obstacleWarning) Red else GrayMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            GameState.WON, GameState.LOST -> {
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

                    Button(
                        onClick = {
                            currentFloor.value = 0
                            platformOffset = 0
                            obstacleWarning = false
                            scope.launch { playerY.snapTo(0f) }
                            obstacleFloors.clear()
                            obstacleFloors.addAll(generateObstacleFloors(TARGET_FLOOR))
                            platforms.clear()
                            platforms.addAll(generatePlatforms(TARGET_FLOOR))
                            gameState.value = GameState.PLAYING
                        },
                        modifier = Modifier.size(width = 160.dp, height = 56.dp),
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

private fun generateObstacleFloors(totalFloors: Int): List<Int> {
    val obstacles = mutableListOf<Int>()
    var nextObstacle = Random.nextInt(3, 6)
    while (nextObstacle < totalFloors) {
        obstacles.add(nextObstacle)
        nextObstacle += Random.nextInt(3, 6)
    }
    return obstacles
}

private enum class GameState {
    IDLE, PLAYING, WON, LOST
}
