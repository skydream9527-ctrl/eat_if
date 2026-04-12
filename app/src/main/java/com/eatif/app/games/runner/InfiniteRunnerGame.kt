package com.eatif.app.games.runner

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.LinearEasing
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
    val isHigh: Boolean
)

@Composable
fun InfiniteRunnerGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var gameState by remember { mutableStateOf("playing") }
    var obstaclesPassed by remember { mutableStateOf(0) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    
    val characterY = remember { Animatable(0f) }
    val characterX = remember { mutableStateOf(100f) }
    var isJumping by remember { mutableStateOf(false) }
    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
    var groundOffset by remember { mutableStateOf(0f) }
    
    val groundY = 400f
    val characterSize = 50f
    val gameWidth = 800f
    val gameHeight = 500f
    
    LaunchedEffect(Unit) {
        characterY.snapTo(groundY - characterSize)
    }
    
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            val startTime = System.currentTimeMillis()
            while (gameState == "playing") {
                kotlinx.coroutines.delay(100)
                groundOffset = (groundOffset + 8f) % 40f
                
                val currentTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                if (currentTime > elapsedSeconds) {
                    elapsedSeconds = currentTime
                }
                
                if (elapsedSeconds >= 30 || obstaclesPassed >= 10) {
                    gameState = "won"
                    val randomFood = foods.randomOrNull()
                    if (randomFood != null) {
                        onResult(randomFood.name)
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isJumping) {
        if (isJumping) {
            characterY.animateTo(
                targetValue = groundY - characterSize - 150f,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            )
            characterY.animateTo(
                targetValue = groundY - characterSize,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            )
            isJumping = false
        }
    }
    
    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            var lastObstacleX = gameWidth
            while (gameState == "playing") {
                kotlinx.coroutines.delay(1500)
                
                if (gameState != "playing") break
                
                val newObstacle = Obstacle(
                    x = lastObstacleX + Random.nextFloat() * 200 + 300,
                    width = Random.nextFloat() * 30 + 40,
                    height = Random.nextFloat() * 40 + 50,
                    isHigh = Random.nextBoolean()
                )
                obstacles = obstacles + newObstacle
                lastObstacleX = newObstacle.x
                
                obstacles = obstacles.map { it.copy(x = it.x - 50f) }
                    .filter { it.x > -100f }
            }
        }
    }
    
    LaunchedEffect(obstacles, characterY.value) {
        if (gameState != "playing") return@LaunchedEffect
        
        val charLeft = characterX.value
        val charRight = characterX.value + characterSize
        val charTop = characterY.value
        val charBottom = characterY.value + characterSize
        
        obstacles.forEach { obstacle ->
            val obsLeft = obstacle.x
            val obsRight = obstacle.x + obstacle.width
            val obsTop = if (obstacle.isHigh) groundY - obstacle.height - 20 else groundY - obstacle.height
            val obsBottom = groundY
            
            if (charRight > obsLeft && charLeft < obsRight && charBottom > obsTop && charTop < obsBottom) {
                gameState = "gameover"
            }
        }
        
        obstacles.filter { obstacle ->
            val obsRight = obstacle.x + obstacle.width
            obsRight < characterX.value && !obstacle.passed
        }.forEach { 
            obstaclesPassed++
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
            text = "距离: ${elapsedSeconds}s | 障碍: $obstaclesPassed/10",
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

                if (gameState != "gameover") {
                    drawCircle(
                        color = Red,
                        radius = characterSize / 2,
                        center = Offset(characterX.value + characterSize / 2, characterY.value + characterSize / 2)
                    )
                }

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                    }
                }
            }

            if (gameState == "won") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "通关成功！",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Green
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (gameState == "gameover" || gameState == "won") {
                    score = 0
                    obstaclesPassed = 0
                    elapsedSeconds = 0
                    obstacles = emptyList()
                    isJumping = false
                    characterY.value = groundY - characterSize
                    groundOffset = 0f
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
                    "gameover" -> "重新开始"
                    "won" -> "再玩一次"
                    else -> "跳跃"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击跳跃躲避障碍，坚持30秒或通过10个障碍即可通关",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF86868B)
        )
    }
}
