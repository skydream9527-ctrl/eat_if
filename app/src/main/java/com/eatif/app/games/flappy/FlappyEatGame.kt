package com.eatif.app.games.flappy

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.withFrameMillis
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.random.Random

data class Pipe(
    val x: Float,
    val gapY: Float,
    val gapHeight: Float,
    val width: Float = 70f,
    val hasPassed: Boolean = false
)

@Composable
fun FlappyEatGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var birdY by remember { mutableStateOf(250f) }
    var birdVelocity by remember { mutableStateOf(0f) }
    var pipes by remember { mutableStateOf(emptyList<Pipe>()) }
    var score by remember { mutableStateOf(0) }
    var gameState by remember { mutableStateOf("ready") }
    var passedPipes by remember { mutableStateOf(0) }

    val birdX = 100f
    val birdSize = 30f
    val gravity = 0.6f
    val flapStrength = -10f
    val pipeSpeed = 4f
    val pipeSpawnInterval = 2000L
    val gameHeight = 500f
    val groundHeight = 60f

    val pipeGapHeight = 150f

    val GrayLight = Color(0xFFF5F5F7)

    LaunchedEffect(gameState) {
        if (gameState == "playing") {
            val initialPipe = Pipe(
                x = 400f,
                gapY = Random.nextFloat() * (gameHeight - groundHeight - pipeGapHeight - 100) + 50,
                gapHeight = pipeGapHeight
            )
            pipes = listOf(initialPipe)

            var lastFrameTime = -1L

            while (gameState == "playing") {
                withFrameMillis { frameTimeMs ->
                    if (lastFrameTime < 0L) {
                        lastFrameTime = frameTimeMs
                        return@withFrameMillis
                    }
                    val dt = (frameTimeMs - lastFrameTime).coerceIn(1L, 48L).toFloat() / 16f
                    lastFrameTime = frameTimeMs

                    // 物理更新（delta-time 归一化到 16ms 基准）
                    birdVelocity += gravity * dt
                    birdY += birdVelocity * dt

                    // 更新管道位置（单次 map，无额外 filter）
                    val updatedPipes = ArrayList<Pipe>(pipes.size + 1)
                    var needNewPipe = true
                    var passedCount = passedPipes

                    for (pipe in pipes) {
                        val newX = pipe.x - pipeSpeed * dt
                        if (newX <= -70f) continue  // 过滤掉出界管道

                        // 碰撞检测
                        val pipeLeft = newX
                        val pipeRight = newX + pipe.width
                        if (birdX + birdSize > pipeLeft && birdX < pipeRight) {
                            val gapTop = pipe.gapY
                            val gapBottom = pipe.gapY + pipe.gapHeight
                            if (birdY < gapTop || birdY + birdSize > gapBottom) {
                                gameState = "gameover"
                                return@withFrameMillis
                            }
                        }

                        // 计分
                        val hasPassed = !pipe.hasPassed && newX + pipe.width < birdX
                        if (hasPassed) passedCount++

                        // 新管道生成判断（最后一根管道 x < 300 时生成）
                        if (newX >= 300f) needNewPipe = false

                        updatedPipes.add(pipe.copy(x = newX, hasPassed = pipe.hasPassed || hasPassed))
                    }

                    if (needNewPipe) {
                        val lastX = updatedPipes.lastOrNull()?.x ?: 400f
                        updatedPipes.add(
                            Pipe(
                                x = (lastX + 250f).coerceAtLeast(450f),
                                gapY = Random.nextFloat() * (gameHeight - groundHeight - pipeGapHeight - 100) + 50,
                                gapHeight = pipeGapHeight
                            )
                        )
                    }

                    pipes = updatedPipes
                    passedPipes = passedCount
                    score = passedCount

                    // 边界检测
                    if (birdY + birdSize > gameHeight - groundHeight || birdY < 0) {
                        gameState = "gameover"
                        return@withFrameMillis
                    }

                    // 胜利条件
                    if (passedPipes >= 3 && gameState == "playing") {
                        val randomFood = foods.randomOrNull()
                        if (randomFood != null) {
                            onResult(randomFood.name)
                        }
                        gameState = "gameover"
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🐦 Flappy Eat",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "分数: $score",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(GrayLight, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (gameState == "playing") {
                                birdVelocity = flapStrength
                            } else if (gameState == "ready") {
                                gameState = "playing"
                                birdY = 250f
                                birdVelocity = flapStrength
                                passedPipes = 0
                                score = 0
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Green,
                    topLeft = Offset(0f, size.height - groundHeight),
                    size = Size(size.width, groundHeight)
                )

                for (pipe in pipes) {
                    drawRect(
                        color = Green,
                        topLeft = Offset(pipe.x, 0f),
                        size = Size(pipe.width, pipe.gapY)
                    )
                    drawRect(
                        color = Green,
                        topLeft = Offset(pipe.x, pipe.gapY + pipe.gapHeight),
                        size = Size(pipe.width, size.height - pipe.gapY - pipe.gapHeight - groundHeight)
                    )
                }

                if (gameState != "gameover") {
                    drawCircle(
                        color = OrangePrimary,
                        radius = birdSize / 2,
                        center = Offset(birdX + birdSize / 2, birdY + birdSize / 2)
                    )
                    drawCircle(
                        color = White,
                        radius = birdSize / 4,
                        center = Offset(birdX + birdSize / 2 + 5, birdY + birdSize / 2 - 3)
                    )
                }
            }

            if (gameState == "ready") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "点击开始",
                            style = MaterialTheme.typography.headlineLarge,
                            color = OrangePrimary
                        )
                        Text(
                            text = "点击屏幕让小鸟飞翔",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
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
                            text = "得分: $score",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (foods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "选择一顿美食安慰自己吧:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name) },
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
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (gameState == "ready") {
                    gameState = "playing"
                    birdY = 250f
                    birdVelocity = flapStrength
                    passedPipes = 0
                    score = 0
                } else if (gameState == "gameover") {
                    gameState = "ready"
                    birdY = 250f
                    birdVelocity = 0f
                    pipes = emptyList()
                    passedPipes = 0
                    score = 0
                } else if (gameState == "playing") {
                    birdVelocity = flapStrength
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
                    "playing" -> "继续游戏"
                    else -> "重新开始"
                },
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "点击屏幕或按钮让小鸟向上飞",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF86868B)
        )
    }
}
