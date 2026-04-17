package com.eatif.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.games.GameRegistry
import com.eatif.app.ui.components.TutorialDialog
import com.eatif.app.ui.settings.GameSettingsManager
import com.eatif.app.ui.theme.White
import com.eatif.app.ui.tutorial.GameTutorials

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    gameId: String,
    mode: String = "single",
    onGameEnd: (String, Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlayViewModel = hiltViewModel()
) {
    val foods by viewModel.foods.collectAsState()
    val gameName = viewModel.getGameName(gameId)
    var isPaused by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    val handleGameEnd: (String, Int) -> Unit = { foodName, scorePercent ->
        viewModel.recordHistory(foodName, gameId, scorePercent)
        onGameEnd(foodName, scorePercent)
    }

    val tutorial = remember(gameId) { GameTutorials.getTutorial(gameId) }
    val hasSeenTutorial = remember(gameId) { GameSettingsManager.hasSeenTutorial(gameId) }

    if (tutorial != null && !hasSeenTutorial) {
        showTutorial = true
    }

    if (tutorial != null && showTutorial) {
        TutorialDialog(
            tutorial = tutorial,
            onDismiss = {
                GameSettingsManager.setTutorialShown(gameId)
                showTutorial = false
            }
        )
    }

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
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                },
                actions = {
                    if (tutorial != null) {
                        IconButton(onClick = {
                            showTutorial = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "游戏教程"
                            )
                        }
                    }
                    IconButton(onClick = { isPaused = !isPaused }) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "继续" else "暂停"
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
            val gameConfig = GameRegistry.get(gameId)
            if (gameConfig != null) {
                val onPauseToggle: ((Boolean) -> Unit)? = if (gameConfig.supportsSelfPause) {
                    { isPaused = it }
                } else null
                gameConfig.content(foods, isPaused, onPauseToggle, handleGameEnd, mode)
            } else {
                Text(
                    text = "游戏 $gameName 开发中...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (isPaused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⏸️ 游戏已暂停",
                            style = MaterialTheme.typography.headlineMedium,
                            color = White
                        )
                        Text(
                            text = "点击播放按钮继续",
                            style = MaterialTheme.typography.bodyLarge,
                            color = White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("退出游戏") },
            text = { Text("确定要退出当前游戏吗？进度将不会保存。") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBackClick()
                }) {
                    Text("退出", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("继续游戏")
                }
            }
        )
    }
}
