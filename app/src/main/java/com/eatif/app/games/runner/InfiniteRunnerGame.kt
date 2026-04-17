package com.eatif.app.games.runner

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import kotlinx.coroutines.launch
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.random.Random

data class Obstacle(
    val x: Float,
    val width: Float,
    val height: Float,
    val isHigh: Boolean,
    var passed: Boolean = false
)

@Composable
fun InfiniteRunnerGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    var score by remember { mutableStateOf(0) }
    var gameState by remember { mutableStateOf("ready") }
    var obstaclesPassed by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }

    val characterY = remember { Animatable(0f) }
    val characterX = remember { mutableStateOf(100f) }
    var isJumping by remember { mutableStateOf(false) }
    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
    var groundOffset by remember { mutableStateOf(0f) }

    val groundY = 400f
    val characterSize = 50f
    val gameWidth = 800f
    val scope = rememberCoroutineScope()

    val actualPaused = isPaused || internalPaused

    LaunchedEffect(Unit) {
        characterY.snapTo(groundY - characterSize)
    }

    LaunchedEffect(gameState, actualPaused) {
        if (gameState == "playing") {
            val startTime = System.currentTimeMillis()
            var lastFrameTime = -1L
            var obstacleTimer = 0f
            val obstacleInterval = 1800f

            while (gameState == "playing") {
                if (actualPaused) {
                    kotlinx.coroutines.delay(100)
                    continue
                }
                withFrameMillis { frameTimeMs ->
                    if (actualPaused) {
                        lastFrameTime = -1L
                        return@withFrameMillis
                    }
                    if (lastFrameTime < 0L) {
                        lastFrameTime = frameTimeMs
                        return@withFrameMillis
                    }
                    val rawDt = (frameTimeMs - lastFrameTime).coerceIn(1L, 48L).toFloat()
                    val dt = rawDt / 16f
                    lastFrameTime = frameTimeMs
                    obstacleTimer += rawDt

                    groundOffset = (groundOffset + 8f * dt) % 40f

                    val currentSec = ((frameTimeMs - startTime) / 1000).toInt()
                    if (currentSec > elapsedSeconds) elapsedSeconds = currentSec

                    val updatedObstacles = ArrayList<Obstacle>(obstacles.size)
                    var passed = obstaclesPassed
                    for (obs in obstacles) {
                        val newX = obs.x - 12f * dt
                        if (newX + obs.width < -100f) continue
                        val obsRight = newX + obs.width
                        if (obsRight < characterX.value && !obs.passed) {
                            obs.passed = true
                            passed++
                        }
                        updatedObstacles.add(obs.copy(x = newX))
                    }
                    obstacles = updatedObstacles
                    obstaclesPassed = passed

                    val charLeft = characterX.value
                    val charRight = characterX.value + characterSize
                    val charTop = characterY.value
                    val charBottom = characterY.value + characterSize
                    for (obs in obstacles) {
                        val obsLeft = obs.x
                        val obsRight2 = obs.x + obs.width
                        val obsTop = if (obs.isHigh) groundY - obs.height - 20 else groundY - obs.height
                        if (charRight > obsLeft && charLeft < obsRight2 && charBottom > obsTop && charTop < groundY) {
                            gameState = "gameover"
                            return@withFrameMillis
                        }
                    }

                    if (obstacleTimer >= obstacleInterval) {
                        obstacleTimer = 0f
                        val lastObs = obstacles.lastOrNull()
                        val spawnX = if (lastObs != null) {
                            (lastObs.x + Random.nextFloat() * 200 + 200).coerceAtLeast(gameWidth)
                        } else {
                            gameWidth
                        }
                        obstacles = obstacles + Obstacle(
                            x = spawnX,
                            width = Random.nextFloat() * 30 + 40,
                            height = Random.nextFloat() * 40 + 50,
                            isHigh = false
                        )
                    }

                    if (elapsedSeconds >= 30 || obstaclesPassed >= 15) {
                        gameState = "won"
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isJumping) {
        if (isJumping) {
            characterY.animateTo(
                targetValue = groundY - characterSize - 150f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            characterY.animateTo(
                targetValue = groundY - characterSize,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
            isJumping = false
        }
    }
    


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏃 无限跑酷",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "距离: ${elapsedSeconds}s | 障碍: $obstaclesPassed/15",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Green, RoundedCornerShape(16.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                for (i in 0..20) {
                    drawRect(
                        color = Color(0xFF228B22),
                        topLeft = Offset(i * 40f - groundOffset, groundY),
                        size = Size(40f, 10f)
                    )
                }
                
                drawRect(
                    color = Color(0xFF8B4513),
                    topLeft = Offset(0f, groundY + 10f),
                    size = Size(size.width, 20f)
                )

                drawCircle(
                        color = Red,
                        radius = characterSize / 2,
                        center = Offset(characterX.value + characterSize / 2, characterY.value + characterSize / 2)
                    )

                obstacles.forEach { obstacle ->
                    val obsY = if (obstacle.isHigh) groundY - obstacle.height - 20 else groundY - obstacle.height
                    drawRect(
                        color = Color(0xFF333333),
                        topLeft = Offset(obstacle.x, obsY),
                        size = Size(obstacle.width, obstacle.height)
                    )
                }
            }

            if (gameState == "gameover") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "游戏结束",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Red
                        )
                        Text(
                            text = "坚持了 ${elapsedSeconds} 秒",
                            style = MaterialTheme.typography.titleLarge,
                            color = White
                        )
                        if (foods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "选择一顿美食安慰自己吧:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name, (obstaclesPassed * 100 / 15).coerceIn(0, 100)) },
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
                }
            }

            if (gameState == "won") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "通关成功！",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Green
                        )
                        Text(
                            text = "坚持了 ${elapsedSeconds} 秒 | 通过 ${obstaclesPassed} 个障碍",
                            style = MaterialTheme.typography.titleLarge,
                            color = White
                        )
                        if (foods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "选择一顿美食奖励自己吧:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name, (obstaclesPassed * 100 / 15).coerceIn(0, 100)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green,
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameState == "playing") {
            Button(
                onClick = {
                    internalPaused = !internalPaused
                    onPauseToggle?.invoke(internalPaused)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF86868B),
                    contentColor = White
                )
            ) {
                Text(
                    text = if (internalPaused) "继续游戏" else "暂停",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (gameState == "ready" || gameState == "gameover" || gameState == "won") {
                    score = 0
                    obstaclesPassed = 0
                    elapsedSeconds = 0
                    obstacles = emptyList()
                    isJumping = false
                    scope.launch { characterY.snapTo(groundY - characterSize) }
                    groundOffset = 0f
                    internalPaused = false
                    gameState = "playing"
                } else if (!isJumping) {
                    isJumping = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = White
            )
        ) {
            Text(
                text = when (gameState) {
                    "ready" -> "开始游戏"
                    "gameover" -> "重新开始"
                    "won" -> "再玩一次"
                    else -> if (internalPaused) "继续" else "跳跃"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击跳跃躲避障碍，坚持30秒或通过15个障碍即可通关",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF86868B)
        )
    }
}
