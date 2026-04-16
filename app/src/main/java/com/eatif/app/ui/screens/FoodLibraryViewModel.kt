package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.usecase.AddFoodUseCase
import com.eatif.app.domain.usecase.DeleteFoodUseCase
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import com.eatif.app.domain.usecase.UpdateFoodWeightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodLibraryViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase,
    private val addFoodUseCase: AddFoodUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase,
    private val updateFoodWeightUseCase: UpdateFoodWeightUseCase
) : ViewModel() {

    val foods: StateFlow<List<Food>> = getAllFoodsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<FoodLibraryUiState>(FoodLibraryUiState.Idle)
    val uiState: StateFlow<FoodLibraryUiState> = _uiState.asStateFlow()

    fun addFood(name: String, category: String, weight: Int = 1) {
        viewModelScope.launch {
            val food = Food(
                name = name,
                category = category,
                weight = weight
            )
            addFoodUseCase(food)
                .onSuccess { _uiState.value = FoodLibraryUiState.Success("添加成功") }
                .onFailure { _uiState.value = FoodLibraryUiState.Error(it.message ?: "添加失败") }
        }
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            deleteFoodUseCase(id)
                .onSuccess { _uiState.value = FoodLibraryUiState.Success("删除成功") }
                .onFailure { _uiState.value = FoodLibraryUiState.Error(it.message ?: "删除失败") }
        }
    }

    fun updateWeight(food: Food, newWeight: Int) {
        viewModelScope.launch {
            updateFoodWeightUseCase(food, newWeight)
                .onSuccess { _uiState.value = FoodLibraryUiState.Success("权重已更新") }
                .onFailure { _uiState.value = FoodLibraryUiState.Error(it.message ?: "更新失败") }
        }
    }

    fun resetUiState() {
        _uiState.value = FoodLibraryUiState.Idle
    }
}

sealed class FoodLibraryUiState {
    data object Idle : FoodLibraryUiState()
    data class Success(val message: String) : FoodLibraryUiState()
    data class Error(val message: String) : FoodLibraryUiState()
}
