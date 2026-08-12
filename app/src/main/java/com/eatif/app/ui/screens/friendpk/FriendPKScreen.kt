package com.eatif.app.ui.screens.friendpk

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.GameList
import com.eatif.app.ui.share.ShareUtils
import com.eatif.app.ui.theme.OrangePrimary

/**
 * 好友 PK 排行榜 - 展示用户各游戏最佳成绩与好友挑战成绩的对比。
 *
 * 纯本地实现：好友成绩由用户手动录入，离线可用。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendPKScreen(
    onBackClick: () -> Unit,
    viewModel: FriendPKViewModel = hiltViewModel()
) {
    val boards by viewModel.boards.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(boards.size) {
        // 进入页面时刷新一次好友数据，保证最新
        viewModel.reloadFriends()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("👥 好友 PK") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = OrangePrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加好友成绩")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (boards.isEmpty()) {
            EmptyPKState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(boards, key = { it.gameId }) { board ->
                    PKBoardCard(
                        board = board,
                        onShareChallenge = {
                            shareChallenge(context, board)
                        },
                        onRemoveFriend = { friendId ->
                            viewModel.removeFriendScore(friendId)
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddFriendScoreDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, gameId, score ->
                viewModel.addFriendScore(name, gameId, score)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun EmptyPKState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🏆", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有 PK 记录",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "玩一局游戏后，点击右下角添加好友成绩，\n看看谁更厉害！",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun PKBoardCard(
    board: GamePKBoard,
    onShareChallenge: () -> Unit,
    onRemoveFriend: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = board.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = board.gameName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onShareChallenge) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "发起挑战",
                        tint = OrangePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            board.ranks.forEachIndexed { index, item ->
                RankRow(
                    rank = index + 1,
                    item = item,
                    onRemove = if (item.friendId != null) {
                        { onRemoveFriend(item.friendId) }
                    } else null
                )
                if (index < board.ranks.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun RankRow(
    rank: Int,
    item: PKRankItem,
    onRemove: (() -> Unit)?
) {
    val medal = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> "$rank"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (item.isMe) OrangePrimary.copy(alpha = 0.12f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = medal,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (item.isMe) FontWeight.Bold else FontWeight.Normal,
            color = if (item.isMe) OrangePrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.scorePercent}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddFriendScoreDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, gameId: String, score: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGameId by remember { mutableStateOf(GameList.games.first().id) }
    var scoreText by remember { mutableStateOf("") }

    val scoreInt = scoreText.toIntOrNull()?.coerceIn(0, 100) ?: -1
    val canConfirm = name.isNotBlank() && scoreInt >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加好友成绩") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("好友昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "选择游戏",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 游戏选择：横向 chips
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GameList.games) { game ->
                        val isSelected = game.id == selectedGameId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) OrangePrimary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedGameId = game.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${game.emoji} ${game.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = scoreText,
                    onValueChange = { raw ->
                        // 只允许数字
                        scoreText = raw.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("得分 (0-100)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedGameId, scoreInt) },
                enabled = canConfirm
            ) {
                Text("添加", color = if (canConfirm) OrangePrimary else MaterialTheme.colorScheme.outline)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/** 构造并分享挑战文案 */
private fun shareChallenge(context: Context, board: GamePKBoard) {
    val myScore = board.ranks.find { it.isMe }?.scorePercent
    val text = buildString {
        appendLine("🏆 Eat If 好友 PK 挑战！")
        appendLine()
        appendLine("🎮 游戏：${board.emoji} ${board.gameName}")
        if (myScore != null) {
            appendLine("💯 我的成绩：$myScore%")
            appendLine()
            appendLine("你能超越我吗？来 Eat If 一决高下！")
        } else {
            appendLine("快来挑战这个游戏，看看你能得多少分！")
        }
        appendLine()
        appendLine("—— Eat If，让游戏决定你的下一顿饭")
    }
    ShareUtils.shareText(context, text, "发起好友 PK 挑战")
}
