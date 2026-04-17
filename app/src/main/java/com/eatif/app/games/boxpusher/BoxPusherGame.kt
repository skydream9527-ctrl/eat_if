package com.eatif.app.games.boxpusher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gray
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White

@Composable
fun BoxPusherGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    var gameState by remember { mutableStateOf(GameState.PLAYING) }
    var moveCount by remember { mutableStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    var playerPos by remember { mutableStateOf(Position(2, 0)) }
    var box1Pos by remember { mutableStateOf(Position(2, 3)) }
    var box2Pos by remember { mutableStateOf(Position(3, 3)) }

    val target1 = Position(2, 4)
    val target2 = Position(4, 3)

    val gridSize = 5
    val maxMoves = 30

    data class HistoryEntry(val playerPos: Position, val box1Pos: Position, val box2Pos: Position)
    var history by remember { mutableStateOf(listOf<HistoryEntry>()) }

    fun move(dr: Int, dc: Int) {
        if (gameState != GameState.PLAYING || actualPaused) return

        val newPlayerRow = playerPos.row + dr
        val newPlayerCol = playerPos.col + dc

        if (newPlayerRow !in 0 until gridSize || newPlayerCol !in 0 until gridSize) return

        val newPlayerPos = Position(newPlayerRow, newPlayerCol)

        val isBox1 = newPlayerPos == box1Pos
        val isBox2 = newPlayerPos == box2Pos

        if (isBox1 || isBox2) {
            val newBoxRow = newPlayerRow + dr
            val newBoxCol = newPlayerCol + dc
            if (newBoxRow !in 0 until gridSize || newBoxCol !in 0 until gridSize) return

            val newBoxPos = Position(newBoxRow, newBoxCol)
            if (newBoxPos == box1Pos || newBoxPos == box2Pos) return

            history = (history + HistoryEntry(playerPos, box1Pos, box2Pos)).takeLast(20)
            if (isBox1) box1Pos = newBoxPos else box2Pos = newBoxPos
            playerPos = newPlayerPos
        } else {
            history = (history + HistoryEntry(playerPos, box1Pos, box2Pos)).takeLast(20)
            playerPos = newPlayerPos
        }

        moveCount++

        val box1OnTarget = box1Pos == target1 || box1Pos == target2
        val box2OnTarget = box2Pos == target1 || box2Pos == target2

        if (box1OnTarget && box2OnTarget) {
            gameState = GameState.WON
        } else if (moveCount >= maxMoves) {
            gameState = GameState.LOST
        }
    }

    fun undo() {
        if (history.isEmpty() || gameState != GameState.PLAYING) return
        val prev = history.last()
        history = history.dropLast(1)
        playerPos = prev.playerPos
        box1Pos = prev.box1Pos
        box2Pos = prev.box2Pos
        moveCount--
    }

    fun restart() {
        playerPos = Position(2, 0)
        box1Pos = Position(2, 3)
        box2Pos = Position(3, 3)
        gameState = GameState.PLAYING
        moveCount = 0
        history = emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📦 推箱子",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "移动次数: $moveCount / $maxMoves",
            style = MaterialTheme.typography.titleMedium,
            color = GrayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        val cells = remember(playerPos, box1Pos, box2Pos) {
            buildList {
                for (row in 0 until gridSize) {
                    for (col in 0 until gridSize) {
                        val pos = Position(row, col)
                        when {
                            pos == playerPos -> add(CellType.PLAYER)
                            pos == box1Pos -> if (box1Pos == target1 || box1Pos == target2) add(CellType.BOX_ON_TARGET) else add(CellType.BOX)
                            pos == box2Pos -> if (box2Pos == target1 || box2Pos == target2) add(CellType.BOX_ON_TARGET) else add(CellType.BOX)
                            pos == target1 || pos == target2 -> add(CellType.TARGET)
                            else -> add(CellType.EMPTY)
                        }
                    }
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(Gray),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            userScrollEnabled = false
        ) {
            items(cells) { cellType ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(if (cellType == CellType.TARGET) Green else GrayMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (cellType) {
                            CellType.PLAYER -> "😀"
                            CellType.BOX -> "📦"
                            CellType.BOX_ON_TARGET -> "✅"
                            CellType.TARGET -> "🎯"
                            CellType.EMPTY -> ""
                        },
                        fontSize = 28.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { move(-1, 0) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(text = "↑", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(60.dp)
            ) {
                Button(
                    onClick = { move(0, -1) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(text = "←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { move(0, 1) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(text = "→", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.Center) {
                Button(
                    onClick = { move(1, 0) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(text = "↓", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else if (gameState == GameState.LOST) {
            Text(
                text = "😵 超过移动次数限制!",
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
                        onClick = { onResult(food.name, 0) },
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
                onClick = { undo() },
                enabled = history.isNotEmpty() && gameState == GameState.PLAYING,
                modifier = Modifier.size(width = 80.dp, height = 56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = White,
                    disabledContainerColor = GrayMedium,
                    disabledContentColor = White
                )
            ) {
                Text(
                    text = "撤销",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = { restart() },
                modifier = Modifier.size(width = 120.dp, height = 56.dp),
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
}

private data class Position(val row: Int, val col: Int)

private enum class CellType {
    EMPTY, PLAYER, BOX, BOX_ON_TARGET, TARGET
}

private enum class GameState {
    PLAYING, WON, LOST
}
