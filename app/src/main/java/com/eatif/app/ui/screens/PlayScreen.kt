package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.games.climb100.Climb100Game
import com.eatif.app.games.flappy.FlappyEatGame
import com.eatif.app.games.game2048.Game2048
import com.eatif.app.games.jump.JumpGame
import com.eatif.app.games.minesweeper.MinesweeperGame
import com.eatif.app.games.needle.NeedleGame
import com.eatif.app.games.onetstroke.OneStrokeGame
import com.eatif.app.games.rps.RockPaperScissorsGame
import com.eatif.app.games.slot.SlotMachineGame
import com.eatif.app.games.spinwheel.SpinWheelGame
import com.eatif.app.games.tetris.TetrisGame
import com.eatif.app.games.snake.SnakeGame
import com.eatif.app.games.boxpusher.BoxPusherGame
import com.eatif.app.games.runner.InfiniteRunnerGame
import com.eatif.app.games.shooting.ShootingGame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    gameId: String,
    onGameEnd: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlayViewModel = hiltViewModel()
) {
    val foods by viewModel.foods.collectAsState()
    val gameName = viewModel.getGameName(gameId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = gameName,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (gameId) {
                "spinwheel" -> SpinWheelGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "rps" -> RockPaperScissorsGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "slot" -> SlotMachineGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "needle" -> NeedleGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "jump" -> JumpGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "climb100" -> Climb100Game(
                    foods = foods,
                    onResult = onGameEnd
                )
                "2048" -> Game2048(
                    foods = foods,
                    onResult = onGameEnd
                )
                "snake" -> SnakeGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "tetris" -> TetrisGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "minesweeper" -> MinesweeperGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "onetstroke" -> OneStrokeGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "flappy" -> FlappyEatGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "boxpusher" -> BoxPusherGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "runner" -> InfiniteRunnerGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                "shooting" -> ShootingGame(
                    foods = foods,
                    onResult = onGameEnd
                )
                else -> Text(
                    text = "游戏 $gameName 开发中...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
