package com.eatif.app.games.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.random.Random

@Composable
fun SnakeGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val gridWidth = 15
    val gridHeight = 15
    val cellSize = 20.dp

    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var score by remember { mutableStateOf(0) }
    val snake = remember { ArrayDeque<Offset>().also { it.addFirst(Offset(7f, 7f)) } }
    var snakeVersion by remember { mutableStateOf(0) }
    var direction by remember { mutableStateOf(Direction.RIGHT) }
    var nextDirection by remember { mutableStateOf(Direction.RIGHT) }
    var food by remember { mutableStateOf(Offset(5f, 5f)) }
    var internalPaused by remember { mutableStateOf(false) }

    val actualPaused = isPaused || internalPaused

    fun generateFood(): Offset {
        val occupied = snake.toHashSet()
        var newFood: Offset
        do {
            newFood = Offset(
                Random.nextInt(gridWidth).toFloat(),
                Random.nextInt(gridHeight).toFloat()
            )
        } while (newFood in occupied)
        return newFood
    }

    fun resetGame() {
        snake.clear()
        snake.addFirst(Offset(7f, 7f))
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        food = generateFood()
        score = 0
        snakeVersion++
        gameState = GameState.PLAYING
    }

    LaunchedEffect(gameState, actualPaused) {
        if (gameState == GameState.PLAYING) {
            while (gameState == GameState.PLAYING) {
                if (actualPaused) {
                    delay(100)
                    continue
                }
                delay(max(80, 150 - score * 7).toLong())
                direction = nextDirection
                val head = snake.first()
                val newHead = when (direction) {
                    Direction.UP -> head.copy(y = head.y - 1)
                    Direction.DOWN -> head.copy(y = head.y + 1)
                    Direction.LEFT -> head.copy(x = head.x - 1)
                    Direction.RIGHT -> head.copy(x = head.x + 1)
                }

                if (newHead.x < 0 || newHead.x >= gridWidth || newHead.y < 0 || newHead.y >= gridHeight) {
                    gameState = GameState.GAME_OVER
                    break
                }

                if (newHead in snake) {
                    gameState = GameState.GAME_OVER
                    break
                }

                snake.addFirst(newHead)

                if (newHead == food) {
                    score++
                    food = generateFood()
                    if (score >= 10) {
                        gameState = GameState.WON
                        snakeVersion++
                    }
                } else {
                    snake.removeLast()
                }
                snakeVersion++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayLight.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🐍 贪吃蛇",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "得分: $score / 10",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size((gridWidth * cellSize.value).dp)
                .background(White, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val (dx, dy) = dragAmount
                        if (kotlin.math.abs(dx) > 30f || kotlin.math.abs(dy) > 30f) {
                            if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                                if (dx > 0 && direction != Direction.LEFT) nextDirection = Direction.RIGHT
                                else if (dx < 0 && direction != Direction.RIGHT) nextDirection = Direction.LEFT
                            } else {
                                if (dy > 0 && direction != Direction.UP) nextDirection = Direction.DOWN
                                else if (dy < 0 && direction != Direction.DOWN) nextDirection = Direction.UP
                            }
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellWidth = size.width / gridWidth
                val cellHeight = size.height / gridHeight

                @Suppress("UNUSED_EXPRESSION")
                snakeVersion

                snake.forEachIndexed { index, segment ->
                    val alpha = (0.7f - index * 0.04f).coerceAtLeast(0.2f)
                    val color = if (index == 0) Green else Green.copy(alpha = alpha)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(segment.x * cellWidth, segment.y * cellHeight),
                        size = Size(cellWidth - 2f, cellHeight - 2f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                }

                drawCircle(
                    color = Red,
                    radius = cellWidth / 2 - 2f,
                    center = Offset(food.x * cellWidth + cellWidth / 2, food.y * cellHeight + cellHeight / 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameState == GameState.IDLE || gameState == GameState.GAME_OVER || gameState == GameState.WON) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gameState == GameState.GAME_OVER) {
                    Text(
                        text = "💀 游戏结束!",
                        style = MaterialTheme.typography.titleLarge,
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
                                onClick = { onResult(food.name, (score * 100 / 10).coerceIn(0, 100)) },
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (gameState == GameState.WON) {
                    Text(
                        text = "🎉 恭喜通关!",
                        style = MaterialTheme.typography.titleLarge,
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Button(
                    onClick = { resetGame() },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = if (gameState == GameState.GAME_OVER) "重新开始" else if (gameState == GameState.WON) "再玩一次" else "开始游戏",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        if (gameState == GameState.PLAYING) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎮 使用方向按钮控制",
                    style = MaterialTheme.typography.bodyLarge,
                    color = GrayMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { if (direction != Direction.DOWN) nextDirection = Direction.UP },
                    modifier = Modifier.size(width = 80.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("↑", color = White)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { if (direction != Direction.RIGHT) nextDirection = Direction.LEFT },
                        modifier = Modifier.size(width = 80.dp, height = 50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("←", color = White)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Button(
                        onClick = { if (direction != Direction.LEFT) nextDirection = Direction.RIGHT },
                        modifier = Modifier.size(width = 80.dp, height = 50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("→", color = White)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { if (direction != Direction.UP) nextDirection = Direction.DOWN },
                    modifier = Modifier.size(width = 80.dp, height = 50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("↓", color = White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            internalPaused = !internalPaused
                            onPauseToggle?.invoke(internalPaused)
                        },
                        modifier = Modifier.size(width = 120.dp, height = 48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayMedium,
                            contentColor = White
                        )
                    ) {
                        Text(
                            text = if (internalPaused) "继续" else "暂停",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = { resetGame() },
                        modifier = Modifier.size(width = 120.dp, height = 48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayMedium,
                            contentColor = White
                        )
                    ) {
                        Text("重新开始", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

private enum class GameState {
    IDLE, PLAYING, WON, GAME_OVER
}
