package com.eatif.app.games.game2048

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlin.random.Random

private enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

private enum class GameState {
    IDLE, PLAYING, WON, LOST
}

@Composable
fun Game2048(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String) -> Unit
) {
    var grid by remember { mutableStateOf(Array(4) { IntArray(4) }) }
    var score by remember { mutableStateOf(0) }
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) {
            for (j in 0..3) {
                if (grid[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        if (emptyCells.isNotEmpty()) {
            val cell = emptyCells.random()
            grid[cell.first][cell.second] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun initGame() {
        grid = Array(4) { IntArray(4) }
        score = 0
        gameState = GameState.PLAYING
        addRandomTile()
        addRandomTile()
    }

    fun canMove(): Boolean {
        for (i in 0..3) {
            for (j in 0..3) {
                if (grid[i][j] == 0) return true
                if (j < 3 && grid[i][j] == grid[i][j + 1]) return true
                if (i < 3 && grid[i][j] == grid[i + 1][j]) return true
            }
        }
        return false
    }

    fun move(direction: Direction): Boolean {
        var moved = false
        val newGrid = Array(4) { IntArray(4) }

        when (direction) {
            Direction.LEFT -> {
                for (i in 0..3) {
                    var col = 0
                    var lastMerge = -1
                    for (j in 0..3) {
                        if (grid[i][j] != 0) {
                            if (col > 0 && newGrid[i][col - 1] == grid[i][j] && lastMerge != col - 1) {
                                newGrid[i][col - 1] *= 2
                                score += newGrid[i][col - 1]
                                lastMerge = col - 1
                                moved = true
                            } else {
                                newGrid[i][col] = grid[i][j]
                                if (col != j) moved = true
                                col++
                                lastMerge = -1
                            }
                        }
                    }
                }
            }
            Direction.RIGHT -> {
                for (i in 0..3) {
                    var col = 3
                    var lastMerge = -1
                    for (j in 3 downTo 0) {
                        if (grid[i][j] != 0) {
                            if (col < 3 && newGrid[i][col + 1] == grid[i][j] && lastMerge != col + 1) {
                                newGrid[i][col + 1] *= 2
                                score += newGrid[i][col + 1]
                                lastMerge = col + 1
                                moved = true
                            } else {
                                newGrid[i][col] = grid[i][j]
                                if (col != j) moved = true
                                col--
                                lastMerge = -1
                            }
                        }
                    }
                }
            }
            Direction.UP -> {
                for (j in 0..3) {
                    var row = 0
                    var lastMerge = -1
                    for (i in 0..3) {
                        if (grid[i][j] != 0) {
                            if (row > 0 && newGrid[row - 1][j] == grid[i][j] && lastMerge != row - 1) {
                                newGrid[row - 1][j] *= 2
                                score += newGrid[row - 1][j]
                                lastMerge = row - 1
                                moved = true
                            } else {
                                newGrid[row][j] = grid[i][j]
                                if (row != i) moved = true
                                row++
                                lastMerge = -1
                            }
                        }
                    }
                }
            }
            Direction.DOWN -> {
                for (j in 0..3) {
                    var row = 3
                    var lastMerge = -1
                    for (i in 3 downTo 0) {
                        if (grid[i][j] != 0) {
                            if (row < 3 && newGrid[row + 1][j] == grid[i][j] && lastMerge != row + 1) {
                                newGrid[row + 1][j] *= 2
                                score += newGrid[row + 1][j]
                                lastMerge = row + 1
                                moved = true
                            } else {
                                newGrid[row][j] = grid[i][j]
                                if (row != i) moved = true
                                row--
                                lastMerge = -1
                            }
                        }
                    }
                }
            }
        }

        if (moved) {
            grid = newGrid
            addRandomTile()
        }
        return moved
    }

    LaunchedEffect(Unit) {
        initGame()
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
            text = "🧩 2048",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "分数: $score",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(300.dp)
                .background(GrayMedium, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 0..3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (j in 0..3) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(getTileColor(grid[i][j]), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (grid[i][j] != 0) {
                                    Text(
                                        text = grid[i][j].toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (grid[i][j] >= 100) 18.sp else 24.sp,
                                        color = if (grid[i][j] <= 4) GrayMedium else White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { move(Direction.UP) },
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("↑", fontSize = 24.sp, color = White)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { move(Direction.LEFT) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("←", fontSize = 24.sp, color = White)
                }

                Spacer(modifier = Modifier.width(52.dp))

                Button(
                    onClick = { move(Direction.RIGHT) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                ) {
                    Text("→", fontSize = 24.sp, color = White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { move(Direction.DOWN) },
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("↓", fontSize = 24.sp, color = White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            score >= 64 -> {
                Text(
                    text = "🎉 恭喜通关!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Green
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val randomFood = foods.random().name
                        onResult(randomFood)
                    },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text("领取奖励", style = MaterialTheme.typography.titleMedium)
                }
            }
            !canMove() -> {
                Text(
                    text = "😵 游戏结束!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                onClick = { initGame() },
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
    }
}

private fun getTileColor(value: Int): Color {
    return when (value) {
        0 -> GrayLight
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        512 -> Color(0xFFEDC850)
        1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFFEDC22E)
        else -> Color(0xFF3C3A32)
    }
}

private enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

private enum class GameState {
    IDLE, PLAYING, WON, LOST
}
