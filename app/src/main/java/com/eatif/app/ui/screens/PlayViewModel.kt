package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    private val defaultFoods = listOf(
        Food(name = "火锅", category = "中餐"),
        Food(name = "寿司", category = "日餐"),
        Food(name = "汉堡", category = "西餐"),
        Food(name = "披萨", category = "西餐"),
        Food(name = "拉面", category = "日餐"),
        Food(name = "饺子", category = "中餐"),
        Food(name = "沙拉", category = "西餐"),
        Food(name = "炸鸡", category = "西餐")
    )

    private val _foods = MutableStateFlow<List<Food>>(defaultFoods)
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    init {
        loadFoods()
    }

    fun loadFoods() {
        viewModelScope.launch {
            val repositoryFoods = repository.getAllFoods().first()
            if (repositoryFoods.isNotEmpty()) {
                _foods.value = repositoryFoods
            }
        }
    }

    fun getGameName(gameId: String): String {
        return GameList.games.find { it.id == gameId }?.name ?: ""
    }
}
