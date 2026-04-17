package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.model.GameRuleConfig
import com.eatif.app.ui.settings.GameSettingsManager
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameRuleScreen(
    gameId: String,
    onBackClick: () -> Unit
) {
    val game = GameList.games.find { it.id == gameId }
    val config = remember { GameSettingsManager.getGameRuleConfig(gameId) }
    var rounds by remember { mutableFloatStateOf(config.rounds.toFloat()) }
    var timeLimit by remember { mutableFloatStateOf(config.timeLimit.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${game?.emoji ?: ""} 规则设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "游戏轮数: ${rounds.roundToInt()}",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = rounds,
                onValueChange = { rounds = it },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (timeLimit.roundToInt() == 0) "时间限制: 无限"
                       else "时间限制: ${timeLimit.roundToInt()}秒",
                style = MaterialTheme.typography.titleMedium
            )
            Slider(
                value = timeLimit,
                onValueChange = { timeLimit = it },
                valueRange = 0f..120f,
                steps = 11,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(
                onClick = {
                    GameSettingsManager.setGameRuleConfig(
                        GameRuleConfig(
                            gameId = gameId,
                            rounds = rounds.roundToInt(),
                            timeLimit = timeLimit.roundToInt()
                        )
                    )
                    onBackClick()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
