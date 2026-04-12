package com.eatif.app.games.tetris

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

data class Tetromino(
    val shape: List<List<Int>>,
    val color: Color
)

val TETROMINO_I = Tetromino(listOf(listOf(1, 1, 1, 1)), Color.Cyan)
val TETROMINO_O = Tetromino(listOf(listOf(1, 1), listOf(1, 1)), Color.Yellow)
val TETROMINO_T = Tetromino(listOf(listOf(0, 1, 0), listOf(1, 1, 1)), Color.Magenta)
val TETROMINO_L = Tetromino(listOf(listOf(1, 0), listOf(1, 0), listOf(1, 1)), Color(0xFFFF8800))

val TETROMINOES = listOf(TETROMINO_I, TETROMINO_O, TETROMINO_T, TETROMINO_L)

class TetrisGameState {
    companion object {
        const val COLS = 10
        const val ROWS = 20
        const val CELL_SIZE = 28f
    }

    val board: Array<IntArray> = Array(ROWS) { IntArray(COLS) { 0 } }
    var currentPiece: Tetromino? = null
    var currentX: Int = 0
    var currentY: Int = 0
    var score: Int = 0
    var linesCleared: Int = 0
    var isGameOver: Boolean = false
    var isPaused: Boolean = false

    val colors = listOf(
        Color(0xFF00BCD4),
        Color(0xFFFFEB3B),
        Color(0xFF9C27B0),
        Color(0xFFFF9800),
        Color(0xFF4CAF50),
        Color(0xFFF44336),
        Color(0xFF2196F3)
    )

    fun spawnPiece() {
        currentPiece = TETROMINOES.random()
        currentX = COLS / 2 - 1
        currentY = 0

        if (!isValidPosition(currentX, currentY, currentPiece!!)) {
            isGameOver = true
            currentPiece = null
        }
    }

    fun isValidPosition(x: Int, y: Int, piece: Tetromino): Boolean {
        for (dy in piece.shape.indices) {
            for (dx in piece.shape[dy].indices) {
                if (piece.shape[dy][dx] == 1) {
                    val newX = x + dx
                    val newY = y + dy
                    if (newX < 0 || newX >= COLS || newY >= ROWS) return false
                    if (newY >= 0 && board[newY][newX] != 0) return false
                }
            }
        }
        return true
    }

    fun rotatePiece(): Boolean {
        val piece = currentPiece ?: return false
        val rotated = Tetromino(
            shape = List(piece.shape[0].size) { col ->
                List(piece.shape.size) { row ->
                    piece.shape[piece.shape.size - 1 - row][col]
                }
            },
            color = piece.color
        )
        if (isValidPosition(currentX, currentY, rotated)) {
            currentPiece = rotated
            return true
        }
        return false
    }

    fun moveLeft(): Boolean {
        val piece = currentPiece ?: return false
        if (isValidPosition(currentX - 1, currentY, piece)) {
            currentX--
            return true
        }
        return false
    }

    fun moveRight(): Boolean {
        val piece = currentPiece ?: return false
        if (isValidPosition(currentX + 1, currentY, piece)) {
            currentX++
            return true
        }
        return false
    }

    fun moveDown(): Boolean {
        val piece = currentPiece ?: return false
        if (isValidPosition(currentX, currentY + 1, piece)) {
            currentY++
            return true
        }
        return false
    }

    fun hardDrop(): Int {
        var dropped = 0
        val piece = currentPiece ?: return 0
        while (isValidPosition(currentX, currentY + 1, piece)) {
            currentY++
            dropped++
        }
        lockPiece()
        return dropped
    }

    fun lockPiece() {
        val piece = currentPiece ?: return
        for (dy in piece.shape.indices) {
            for (dx in piece.shape[dy].indices) {
                if (piece.shape[dy][dx] == 1) {
                    val y = currentY + dy
                    val x = currentX + dx
                    if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                        board[y][x] = colors.indexOf(piece.color) + 1
                    }
                }
            }
        }
        clearLines()
        spawnPiece()
    }

    fun clearLines() {
        val linesToClear = mutableListOf<Int>()
        for (y in 0 until ROWS) {
            if (board[y].all { it != 0 }) {
                linesToClear.add(y)
            }
        }

        for (y in linesToClear) {
            for (x in 0 until COLS) {
                board[y][x] = 0
            }
        }

        linesCleared += linesToClear.size
        score += linesToClear.size * 100
    }
}

@Composable
fun TetrisGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    val gameState = remember { TetrisGameState() }
    var gameStateVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        gameState.spawnPiece()
    }

    LaunchedEffect(gameStateVersion) {
        if (gameState.isGameOver || gameState.isPaused) return@LaunchedEffect
        kotlinx.coroutines.delay(500)
        val piece = gameState.currentPiece
        if (piece != null && !gameState.isValidPosition(gameState.currentX, gameState.currentY + 1, piece)) {
            gameState.lockPiece()
            gameStateVersion++
        } else if (piece != null) {
            gameState.currentY++
        }
        if (gameState.linesCleared >= 3) {
            val randomFood = foods.randomOrNull()
            if (randomFood != null) {
                onResult(randomFood.name)
            }
            gameState.linesCleared = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🧱 俄罗斯方块",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "分数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${gameState.score}",
                    style = MaterialTheme.typography.titleLarge,
                    color = OrangePrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "行数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${gameState.linesCleared}/3",
                    style = MaterialTheme.typography.titleLarge,
                    color = Green
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val gridWidth = TETRIS_COLS * CELL_SIZE
        val gridHeight = TETRIS_ROWS * CELL_SIZE

        Box(
            modifier = Modifier
                .background(Color.DarkGray, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Canvas(
                modifier = Modifier.size(gridWidth.dp, gridHeight.dp)
            ) {
                for (y in 0 until TETRIS_ROWS) {
                    for (x in 0 until TETRIS_COLS) {
                        val cell = gameState.board[y][x]
                        val color = if (cell > 0) gameState.colors[cell - 1] else Color.White.copy(alpha = 0.3f)
                        drawRect(
                            color = color,
                            topLeft = Offset(x * CELL_SIZE + 1, y * CELL_SIZE + 1),
                            size = Size(CELL_SIZE - 2, CELL_SIZE - 2)
                        )
                    }
                }

                gameState.currentPiece?.let { piece ->
                    for (dy in piece.shape.indices) {
                        for (dx in piece.shape[dy].indices) {
                            if (piece.shape[dy][dx] == 1) {
                                val x = (gameState.currentX + dx) * CELL_SIZE
                                val y = (gameState.currentY + dy) * CELL_SIZE
                                drawRect(
                                    color = piece.color,
                                    topLeft = Offset(x + 1, y + 1),
                                    size = Size(CELL_SIZE - 2, CELL_SIZE - 2)
                                )
                            }
                        }
                    }
                }
            }

            if (gameState.isGameOver) {
                Box(
                    modifier = Modifier
                        .size(gridWidth.dp, gridHeight.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "游戏结束",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Red
                        )
                        Text(
                            text = "得分: ${gameState.score}",
                            style = MaterialTheme.typography.titleMedium,
                            color = White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (gameState.moveLeft()) gameStateVersion++
                },
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("◀", style = MaterialTheme.typography.titleLarge, color = White)
            }

            Button(
                onClick = {
                    if (gameState.rotatePiece()) gameStateVersion++
                },
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("↻", style = MaterialTheme.typography.titleLarge, color = White)
            }

            Button(
                onClick = {
                    if (gameState.moveRight()) gameStateVersion++
                },
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("▶", style = MaterialTheme.typography.titleLarge, color = White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (gameState.moveDown()) gameStateVersion++
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green)
            ) {
                Text("加速下落", color = White)
            }

            Button(
                onClick = {
                    gameState.hardDrop()
                    gameStateVersion++
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red)
            ) {
                Text("直接落地", color = White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                for (y in 0 until TETRIS_ROWS) {
                    for (x in 0 until TETRIS_COLS) {
                        gameState.board[y][x] = 0
                    }
                }
                gameState.score = 0
                gameState.linesCleared = 0
                gameState.isGameOver = false
                gameState.currentPiece = null
                gameState.spawnPiece()
                gameStateVersion++
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = White
            ),
            enabled = gameState.isGameOver
        ) {
            Text(text = "重新开始", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private const val TETRIS_COLS = TetrisGameState.COLS
private const val TETRIS_ROWS = TetrisGameState.ROWS
