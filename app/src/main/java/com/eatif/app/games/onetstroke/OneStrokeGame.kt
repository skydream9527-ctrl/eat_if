package com.eatif.app.games.onetstroke

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf<Int>
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gray
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.math.sqrt

@Composable
fun OneStrokeGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    val gridSize = 4
    val dotCount = gridSize * gridSize
    val dotRadius = 20f
    val hitRadius = 60f

    var visitedDots by remember { mutableStateOf(setOf<Int>()) }
    var pathDots by remember { mutableStateOf(listOf<Int>()) }
    var currentDragPos by remember { mutableStateOf<Offset?>(null) }
    var gameState by remember { mutableStateOf(GameState2.PLAYING) }

    fun getDotCenter(index: Int, canvasSize: Float): Offset {
        val row = index / gridSize
        val col = index % gridSize
        val spacing = canvasSize / gridSize
        val padding = spacing / 2
        return Offset(padding + col * spacing, padding + row * spacing)
    }

    fun findDotAtPosition(pos: Offset, canvasSize: Float): Int? {
        val spacing = canvasSize / gridSize
        val padding = spacing / 2
        val col = ((pos.x - padding + spacing / 2) / spacing).toInt().coerceIn(0, gridSize - 1)
        val row = ((pos.y - padding + spacing / 2) / spacing).toInt().coerceIn(0, gridSize - 1)
        val index = row * gridSize + col
        val center = getDotCenter(index, canvasSize)
        val distance = sqrt((pos.x - center.x) * (pos.x - center.x) + (pos.y - center.y) * (pos.y - center.y))
        return if (distance < hitRadius) index else null
    }

    fun resetGame() {
        visitedDots = emptySet()
        pathDots = emptyList()
        currentDragPos = null
        gameState = GameState2.PLAYING
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✏️ 一笔画",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "已连接: ${pathDots.size} / $dotCount",
            style = MaterialTheme.typography.titleMedium,
            color = GrayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Gray, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            var canvasSize by remember { mutableStateOf(0f) }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (gameState == GameState2.PLAYING) {
                                    val dotIndex = findDotAtPosition(offset, canvasSize)
                                    if (dotIndex != null && dotIndex !in visitedDots) {
                                        visitedDots = visitedDots + dotIndex
                                        pathDots = listOf(dotIndex)
                                        currentDragPos = offset
                                    }
                                }
                            },
                            onDrag = { change, _ ->
                                if (gameState == GameState2.PLAYING) {
                                    currentDragPos = change.position
                                    val dotIndex = findDotAtPosition(change.position, canvasSize)
                                    if (dotIndex != null && dotIndex !in visitedDots) {
                                        val lastDot = pathDots.lastOrNull()
                                        if (lastDot != null) {
                                            val lastCenter = getDotCenter(lastDot, canvasSize)
                                            val newCenter = getDotCenter(dotIndex, canvasSize)
                                            val dist = sqrt(
                                                (newCenter.x - lastCenter.x) * (newCenter.x - lastCenter.x) +
                                                        (newCenter.y - lastCenter.y) * (newCenter.y - lastCenter.y)
                                            )
                                            val step = canvasSize / gridSize
                                            if (dist <= step * 1.5) {
                                                visitedDots = visitedDots + dotIndex
                                                pathDots = pathDots + dotIndex
                                            }
                                        }
                                    } else if (dotIndex != null && dotIndex in visitedDots && pathDots.size > 1) {
                                        gameState = GameState2.FAILED
                                    }
                                }
                            },
                            onDragEnd = {
                                if (gameState == GameState2.PLAYING) {
                                    if (pathDots.size == dotCount) {
                                        gameState = GameState2.WON
                                        if (foods.isNotEmpty()) {
                                            onResult(foods.random().name)
                                        }
                                    } else if (pathDots.size < dotCount) {
                                        gameState = GameState2.FAILED
                                    }
                                }
                                currentDragPos = null
                            },
                            onDragCancel = {
                                currentDragPos = null
                            }
                        )
                    }
            ) {
                canvasSize = size.minDimension
                val spacing = canvasSize / gridSize
                val dotDrawRadius = dotRadius * (canvasSize / 400f).coerceIn(0.5f, 1.5f)

                for (i in 0 until dotCount) {
                    val center = getDotCenter(i, canvasSize)
                    val isVisited = i in visitedDots
                    drawCircle(
                        color = if (isVisited) OrangePrimary else GrayLight,
                        radius = if (isVisited) dotDrawRadius * 1.3f else dotDrawRadius,
                        center = center
                    )
                }

                if (pathDots.size >= 2) {
                    for (i in 0 until pathDots.size - 1) {
                        val start = getDotCenter(pathDots[i], canvasSize)
                        val end = getDotCenter(pathDots[i + 1], canvasSize)
                        drawLine(
                            color = OrangePrimary,
                            start = start,
                            end = end,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }

                currentDragPos?.let { dragPos ->
                    val lastDotIndex = pathDots.lastOrNull()
                    if (lastDotIndex != null) {
                        val lastCenter = getDotCenter(lastDotIndex, canvasSize)
                        drawLine(
                            color = OrangePrimary.copy(alpha = 0.5f),
                            start = lastCenter,
                            end = dragPos,
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (gameState) {
            GameState2.WON -> {
                Text(
                    text = "🎉 恭喜通关!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            GameState2.FAILED -> {
                Text(
                    text = "❌ 连接失败!",
                    style = MaterialTheme.typography.titleLarge,
                    color = Red
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            else -> {}
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
                text = "重新开始",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private enum class GameState2 {
    PLAYING, WON, FAILED
}
