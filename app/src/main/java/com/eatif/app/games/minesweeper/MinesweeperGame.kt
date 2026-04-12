package com.eatif.app.games.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gray
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White

@Composable
fun MinesweeperGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    val gridSize = 8
    val mineCount = 8
    val totalCells = gridSize * gridSize
    val safeCells = totalCells - mineCount

    var board by remember { mutableStateOf(createBoard(gridSize, mineCount)) }
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var uncoveredCount by remember { mutableStateOf(0) }
    var cellsRemaining by remember { mutableStateOf(safeCells) }

    val mineDisplayCount = board.flatten().count { it.isMine && !it.isRevealed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍 扫雷",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "剩余雷数: $mineDisplayCount | 安全格子: $cellsRemaining",
            style = MaterialTheme.typography.titleMedium,
            color = GrayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            items(board.flatten()) { cell ->
                CellView(
                    cell = cell,
                    onClick = {
                        if (gameState == GameState.IDLE || gameState == GameState.PLAYING) {
                            if (!cell.isRevealed && !cell.isFlagged) {
                                if (cell.isMine) {
                                    gameState = GameState.LOST
                                    board = board.map { row ->
                                        row.map { c ->
                                            if (c.isMine) c.copy(isRevealed = true) else c
                                        }
                                    }
                                } else {
                                    val (newBoard, newUncovered) = revealCell(board, cell.row, cell.col, gridSize)
                                    board = newBoard
                                    uncoveredCount += newUncovered
                                    cellsRemaining = safeCells - uncoveredCount

                                    if (cellsRemaining <= safeCells / 2 && foods.isNotEmpty()) {
                                        val randomFood = foods.random().name
                                        onResult(randomFood)
                                        gameState = GameState.WON
                                    } else if (cellsRemaining == 0) {
                                        val randomFood = foods.random().name
                                        onResult(randomFood)
                                        gameState = GameState.WON
                                    } else {
                                        gameState = GameState.PLAYING
                                    }
                                }
                            }
                        }
                    },
                    onFlag = {
                        if (!cell.isRevealed && (gameState == GameState.IDLE || gameState == GameState.PLAYING)) {
                            board = board.map { row ->
                                row.map { c ->
                                    if (c.row == cell.row && c.col == cell.col) {
                                        c.copy(isFlagged = !c.isFlagged)
                                    } else c
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameState == GameState.LOST) {
            Text(
                text = "💥踩到雷了!",
                style = MaterialTheme.typography.titleLarge,
                color = Red
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else if (gameState == GameState.WON) {
            Text(
                text = "🎉 恭喜通关!",
                style = MaterialTheme.typography.titleLarge,
                color = Green
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                board = createBoard(gridSize, mineCount)
                gameState = GameState.IDLE
                uncoveredCount = 0
                cellsRemaining = safeCells
            },
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

@Composable
private fun CellView(
    cell: Cell,
    onClick: () -> Unit,
    onFlag: () -> Unit
) {
    val backgroundColor = when {
        cell.isRevealed && cell.isMine -> Red
        cell.isRevealed -> GrayLight
        else -> GrayMedium
    }

    val textColor = when {
        cell.adjacentMines == 1 -> Color(0xFF0000FF)
        cell.adjacentMines == 2 -> Color(0xFF008000)
        cell.adjacentMines == 3 -> Color(0xFFFF0000)
        cell.adjacentMines == 4 -> Color(0xFF000080)
        cell.adjacentMines == 5 -> Color(0xFF800000)
        cell.adjacentMines == 6 -> Color(0xFF008080)
        cell.adjacentMines == 7 -> Color.Black
        cell.adjacentMines == 8 -> Color.Gray
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                cell.isRevealed && cell.isMine -> "💣"
                cell.isFlagged -> "🚩"
                cell.isRevealed && cell.adjacentMines > 0 -> cell.adjacentMines.toString()
                cell.isRevealed -> ""
                else -> "?"
            },
            color = if (cell.isFlagged || (cell.isRevealed && cell.isMine)) Color.Unspecified else textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(2.dp)
        )
    }
}

private data class Cell(
    val row: Int,
    val col: Int,
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMines: Int = 0
)

private enum class GameState {
    IDLE, PLAYING, WON, LOST
}

private fun createBoard(gridSize: Int, mineCount: Int): MutableList<MutableList<Cell>> {
    val positions = mutableListOf<Pair<Int, Int>>()
    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            positions.add(Pair(row, col))
        }
    }
    positions.shuffle()
    val minePositions = positions.take(mineCount).toSet()

    val board = mutableListOf<MutableList<Cell>>()
    for (row in 0 until gridSize) {
        val rowCells = mutableListOf<Cell>()
        for (col in 0 until gridSize) {
            val isMine = Pair(row, col) in minePositions
            rowCells.add(Cell(row = row, col = col, isMine = isMine))
        }
        board.add(rowCells)
    }

    for (row in 0 until gridSize) {
        for (col in 0 until gridSize) {
            if (!board[row][col].isMine) {
                val adjacentMines = countAdjacentMines(board, row, col, gridSize)
                board[row][col] = board[row][col].copy(adjacentMines = adjacentMines)
            }
        }
    }

    return board
}

private fun countAdjacentMines(board: MutableList<MutableList<Cell>>, row: Int, col: Int, gridSize: Int): Int {
    var count = 0
    for (dr in -1..1) {
        for (dc in -1..1) {
            if (dr == 0 && dc == 0) continue
            val newRow = row + dr
            val newCol = col + dc
            if (newRow in 0 until gridSize && newCol in 0 until gridSize) {
                if (board[newRow][newCol].isMine) count++
            }
        }
    }
    return count
}

private fun revealCell(
    board: MutableList<MutableList<Cell>>,
    row: Int,
    col: Int,
    gridSize: Int
): Pair<MutableList<MutableList<Cell>>, Int> {
    if (row !in 0 until gridSize || col !in 0 until gridSize) return Pair(board, 0)
    val cell = board[row][col]
    if (cell.isRevealed || cell.isMine) return Pair(board, 0)

    board[row][col] = board[row][col].copy(isRevealed = true)
    var uncoveredCount = 1

    if (cell.adjacentMines == 0) {
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val (resultBoard, resultCount) = revealCell(board, row + dr, col + dc, gridSize)
                uncoveredCount += resultCount
            }
        }
    }

    return Pair(board, uncoveredCount)
}
