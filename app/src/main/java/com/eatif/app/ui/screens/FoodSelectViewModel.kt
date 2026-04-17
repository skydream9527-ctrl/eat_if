package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.DefaultFoods
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

    private val defaultFoods = DefaultFoods.list

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
