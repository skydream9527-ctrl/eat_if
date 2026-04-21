package com.eatif.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.ui.components.XPProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    onBackClick: () -> Unit,
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAchievements() }

    var selectedAchievement by remember { mutableStateOf<AchievementWithProgress?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成就") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == AchievementFilter.ALL,
                    onClick = { viewModel.setFilter(AchievementFilter.ALL) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = filter == AchievementFilter.UNLOCKED,
                    onClick = { viewModel.setFilter(AchievementFilter.UNLOCKED) },
                    label = { Text("已解锁") }
                )
                FilterChip(
                    selected = filter == AchievementFilter.LOCKED,
                    onClick = { viewModel.setFilter(AchievementFilter.LOCKED) },
                    label = { Text("未解锁") }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip("全部", categoryFilter == null) { viewModel.setCategoryFilter(null) }
                CategoryChip("里程碑", categoryFilter == AchievementCategory.MILESTONE) { viewModel.setCategoryFilter(AchievementCategory.MILESTONE) }
                CategoryChip("技能", categoryFilter == AchievementCategory.SKILL) { viewModel.setCategoryFilter(AchievementCategory.SKILL) }
                CategoryChip("连击", categoryFilter == AchievementCategory.STREAK) { viewModel.setCategoryFilter(AchievementCategory.STREAK) }
                CategoryChip("探索", categoryFilter == AchievementCategory.EXPLORATION) { viewModel.setCategoryFilter(AchievementCategory.EXPLORATION) }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { item ->
                    AchievementCard(item = item, onClick = { selectedAchievement = item })
                }
            }
        }
        selectedAchievement?.let { item ->
            if (!item.progress.isUnlocked) {
                AchievementDetailDialog(item = item, onDismiss = { selectedAchievement = null })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
private fun AchievementCard(item: AchievementWithProgress, onClick: () -> Unit) {
    val alpha = if (item.progress.isUnlocked) 1f else 0.5f
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.progress.isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp).alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = item.achievement.icon, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = item.achievement.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!item.progress.isUnlocked) {
                XPProgressBar(
                    progressPercent = if (item.progress.requiredProgress > 0) (item.progress.currentProgress.toFloat() / item.progress.requiredProgress) * 100f else 0f,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${item.progress.currentProgress}/${item.progress.requiredProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(text = "✅", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AchievementDetailDialog(item: AchievementWithProgress, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.achievement.name) },
        text = {
            Column {
                Text(text = item.achievement.icon, style = MaterialTheme.typography.displayMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.achievement.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "进度: ${item.progress.currentProgress}/${item.progress.requiredProgress}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "奖励: +${item.achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFC107)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
