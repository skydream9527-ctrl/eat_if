package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodSelectViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase
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

    private fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase().collect { repositoryFoods ->
                _foods.value = if (repositoryFoods.isNotEmpty()) {
                    repositoryFoods
                } else {
                    defaultFoods
                }
            }
        }
    }
}
