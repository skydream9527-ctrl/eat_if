package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.DefaultFoods
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.usecase.AddHistoryUseCase
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase,
    private val addHistoryUseCase: AddHistoryUseCase
) : ViewModel() {

    private val defaultFoods = DefaultFoods.list

    private val _foods = MutableStateFlow<List<Food>>(defaultFoods)
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    init {
        loadFoods()
    }

    fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase().collect { repositoryFoods ->
                if (repositoryFoods.isNotEmpty()) {
                    _foods.value = repositoryFoods
                }
            }
        }
    }

    fun getGameName(gameId: String): String {
        return GameList.games.find { it.id == gameId }?.name ?: ""
    }

    fun recordHistory(foodName: String, gameId: String, scorePercent: Int) {
        val gameName = getGameName(gameId)
        viewModelScope.launch {
            addHistoryUseCase(foodName, gameName, scorePercent)
        }
    }
}
