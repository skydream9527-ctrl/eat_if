package com.eatif.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Game
import com.eatif.app.domain.model.GameCategory
import com.eatif.app.domain.model.GameList
import com.eatif.app.ui.components.DifficultySelector
import com.eatif.app.ui.components.EmptyStates
import com.eatif.app.ui.components.EmptyStateWithAction
import com.eatif.app.ui.components.GameCard
import com.eatif.app.ui.settings.GameSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectScreen(
    mode: String,
    onGameSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    onGameRuleClick: (String) -> Unit = {},
    onLevelSelectClick: (String) -> Unit = {}
) {
    val title = if (mode == "double") "双人竞技 - 选择游戏" else "单人模式 - 选择游戏"
    var selectedCategory by remember { mutableStateOf<GameCategory?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf(GameSettingsManager.difficulty) }

    val filteredGames = remember(selectedCategory, showFavoritesOnly) {
        var games = if (selectedCategory == null) {
            GameList.games
        } else {
            GameList.games.filter { it.category == selectedCategory }
        }
        if (showFavoritesOnly) {
            games = games.filter { GameSettingsManager.isFavorite(it.id) }
        }
        games
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedDifficulty = when (selectedDifficulty) {
                            GameDifficulty.EASY -> GameDifficulty.NORMAL
                            GameDifficulty.NORMAL -> GameDifficulty.HARD
                            GameDifficulty.HARD -> GameDifficulty.EASY
                        }
                        GameSettingsManager.difficulty = selectedDifficulty
                    }) {
                        Text(
                            text = "${selectedDifficulty.getEmoji()} ${selectedDifficulty.getDisplayName()}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "收藏游戏",
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DifficultySelector(
                selectedDifficulty = selectedDifficulty,
                onDifficultySelected = {
                    selectedDifficulty = it
                    GameSettingsManager.difficulty = it
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            if (showFavoritesOnly && filteredGames.isEmpty()) {
                EmptyStateWithAction(
                    emoji = EmptyStates.FAVORITES.emoji,
                    title = EmptyStates.FAVORITES.title,
                    subtitle = EmptyStates.FAVORITES.subtitle,
                    actionText = "查看全部游戏",
                    onActionClick = { showFavoritesOnly = false }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames) { game ->
                        GameCard(
                            title = game.name,
                            emoji = game.emoji,
                            description = game.description,
                            isFavorite = GameSettingsManager.isFavorite(game.id),
                            onFavoriteClick = {
                                GameSettingsManager.toggleFavorite(game.id)
                            },
                            onClick = { onGameSelected(game.id) },
                            onSettingsClick = { onGameRuleClick(game.id) },
                            onLevelClick = { onLevelSelectClick(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    selectedCategory: GameCategory?,
    onCategorySelected: (GameCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("全部") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        GameCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(getCategoryName(category)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

private fun getCategoryName(category: GameCategory): String {
    return when (category) {
        GameCategory.PRECISION -> "精准"
        GameCategory.JUMP -> "跳跃"
        GameCategory.CLIMB -> "攀爬"
        GameCategory.LUCK -> "运气"
        GameCategory.BATTLE -> "对战"
        GameCategory.PUZZLE -> "益智"
        GameCategory.CLASSIC -> "经典"
        GameCategory.ARCADE -> "街机"
    }
}
