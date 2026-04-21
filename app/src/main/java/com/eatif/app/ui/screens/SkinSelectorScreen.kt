package com.eatif.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinRarity
import com.eatif.app.domain.usecase.SkinRegistry
import com.eatif.app.domain.usecase.SkinResolver
import com.eatif.app.ui.settings.SkinSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinSelectorScreen(
    gameId: String,
    gameName: String,
    onBackClick: () -> Unit,
    skinResolver: SkinResolver = remember { SkinResolver() }
) {
    val skins = remember(gameId) { SkinRegistry.getSkinsForGame(gameId) }
    val activeSkinId = SkinSettingsManager.getActiveSkinId(gameId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$gameName - 皮肤") },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(skins) { skin ->
                val isUnlocked = SkinSettingsManager.isSkinUnlocked(skin.id)
                val isActive = activeSkinId == skin.id && isUnlocked
                SkinCard(
                    skin = skin,
                    isUnlocked = isUnlocked,
                    isActive = isActive,
                    onClick = {
                        if (isUnlocked && !isActive) {
                            skinResolver.setActiveSkin(gameId, skin.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SkinCard(
    skin: Skin,
    isUnlocked: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = when (skin.rarity) {
        SkinRarity.COMMON -> MaterialTheme.colorScheme.primary
        SkinRarity.RARE -> Color(0xFF2196F3)
        SkinRarity.EPIC -> Color(0xFF9C27B0)
        SkinRarity.LEGENDARY -> Color(0xFFFFC107)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else if (isUnlocked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = if (isUnlocked) 0.3f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = skin.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = rarityColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "锁定",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = skin.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = skin.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = rarityColor
            )

            if (!isUnlocked) {
                Text(
                    text = "解锁条件: ${skin.unlockRequirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已激活",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "使用中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
