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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gray
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White

@Composable
fun BoxPusherGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.PLAYING) }
    var moveCount by remember { mutableStateOf<Int>(0) }

    var playerPos by remember { mutableStateOf(Position(2, 0)) }
    var box1Pos by remember { mutableStateOf(Position(2, 3)) }
    var box2Pos by remember { mutableStateOf(Position(3, 3)) }

    val target1 = Position(2, 4)
    val target2 = Position(4, 3)

    val gridSize = 5

    fun move(dr: Int, dc: Int) {
        if (gameState != GameState.PLAYING) return

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

            if (isBox1) box1Pos = newBoxPos else box2Pos = newBoxPos
            playerPos = newPlayerPos
        } else {
            playerPos = newPlayerPos
        }

        moveCount++

        val box1OnTarget = box1Pos == target1 || box1Pos == target2
        val box2OnTarget = box2Pos == target1 || box2Pos == target2

        if (box1OnTarget && box2OnTarget) {
            gameState = GameState.WON
            if (foods.isNotEmpty()) {
                onResult(foods.random().name)
            }
        }
    }

    fun restart() {
        playerPos = Position(2, 0)
        box1Pos = Position(2, 3)
        box2Pos = Position(3, 3)
        gameState = GameState.PLAYING
        moveCount = 0
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
            text = "移动次数: $moveCount",
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
                            pos == box1Pos -> add(CellType.BOX)
                            pos == box2Pos -> add(CellType.BOX)
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
                            CellType.TARGET -> "🎯"
                            CellType.EMPTY -> ""
                        },
                        fontSize = 28.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("↑", -1, 0),
                Triple("↓", 1, 0),
                Triple("←", 0, -1),
                Triple("→", 0, 1)
            ).forEach { (label, dr, dc) ->
                Button(
                    onClick = { move(dc, dr) },
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
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
        }

        Button(
            onClick = { restart() },
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

private data class Position(val row: Int, val col: Int)

private enum class CellType {
    EMPTY, PLAYER, BOX, TARGET
}

private enum class GameState {
    PLAYING, WON
}
