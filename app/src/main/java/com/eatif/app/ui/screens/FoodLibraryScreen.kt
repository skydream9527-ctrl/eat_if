package com.eatif.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodTag
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLibraryScreen(
    onBackClick: () -> Unit,
    viewModel: FoodLibraryViewModel = hiltViewModel()
) {
    val foods by viewModel.filteredFoods.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFood by remember { mutableStateOf<Food?>(null) }
    var editingTagsFood by remember { mutableStateOf<Food?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is FoodLibraryUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            is FoodLibraryUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的美食库") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Food"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (foods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "美食库为空",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "点击 + 添加你的第一道菜",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.selectTag(null) },
                            label = { Text("全部") }
                        )
                    }
                    items(FoodTag.entries) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { viewModel.selectTag(tag) },
                            label = { Text("${tag.emoji} ${tag.label}") }
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(foods, key = { it.id }) { food ->
                        FoodItem(
                            food = food,
                            onDeleteClick = { viewModel.deleteFood(food.id) },
                            onEditWeightClick = { editingFood = food },
                            onEditTagsClick = { editingTagsFood = food }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, category, weight, tags ->
                viewModel.addFood(name, category, weight, tags)
                showAddDialog = false
            }
        )
    }

    editingFood?.let { food ->
        EditWeightDialog(
            food = food,
            onDismiss = { editingFood = null },
            onConfirm = { newWeight ->
                viewModel.updateWeight(food, newWeight)
                editingFood = null
            }
        )
    }

    editingTagsFood?.let { food ->
        EditTagsDialog(
            food = food,
            onDismiss = { editingTagsFood = null },
            onConfirm = { tags ->
                viewModel.updateTags(food, tags)
                editingTagsFood = null
            }
        )
    }
}

@Composable
private fun FoodItem(
    food: Food,
    onDeleteClick: () -> Unit,
    onEditWeightClick: () -> Unit,
    onEditTagsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = food.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "权重: ${food.weight}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (food.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        food.tags.forEach { tag ->
                            Text(
                                text = "${tag.emoji} ${tag.label}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            Row {
                IconButton(onClick = onEditTagsClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Tags",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
                IconButton(onClick = onEditWeightClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Weight",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddFoodDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, weight: Int, tags: List<FoodTag>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var weight by remember { mutableFloatStateOf(1f) }
    val selectedTags = remember { mutableStateListOf<FoodTag>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加美食") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("美食名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "权重: ${weight.roundToInt()} (出现概率)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 1f..5f,
                    steps = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "标签",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FoodTag.entries.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (tag in selectedTags) selectedTags.remove(tag)
                                else selectedTags.add(tag)
                            }
                    ) {
                        Checkbox(
                            checked = tag in selectedTags,
                            onCheckedChange = { checked ->
                                if (checked) selectedTags.add(tag)
                                else selectedTags.remove(tag)
                            }
                        )
                        Text("${tag.emoji} ${tag.label}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, category, weight.roundToInt(), selectedTags.toList()) },
                enabled = name.isNotBlank() && category.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditWeightDialog(
    food: Food,
    onDismiss: () -> Unit,
    onConfirm: (newWeight: Int) -> Unit
) {
    var weight by remember { mutableFloatStateOf(food.weight.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整权重 - ${food.name}") },
        text = {
            Column {
                Text(
                    text = "权重越高，出现概率越大",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "当前权重: ${weight.roundToInt()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Slider(
                    value = weight,
                    onValueChange = { weight = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1", style = MaterialTheme.typography.labelSmall)
                    Text("5", style = MaterialTheme.typography.labelSmall)
                    Text("10", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(weight.roundToInt()) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditTagsDialog(
    food: Food,
    onDismiss: () -> Unit,
    onConfirm: (tags: List<FoodTag>) -> Unit
) {
    val selectedTags = remember { mutableStateListOf(*food.tags.toTypedArray()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑标签 - ${food.name}") },
        text = {
            Column {
                FoodTag.entries.forEach { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (tag in selectedTags) selectedTags.remove(tag)
                                else selectedTags.add(tag)
                            }
                    ) {
                        Checkbox(
                            checked = tag in selectedTags,
                            onCheckedChange = { checked ->
                                if (checked) selectedTags.add(tag)
                                else selectedTags.remove(tag)
                            }
                        )
                        Text("${tag.emoji} ${tag.label}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedTags.toList()) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
