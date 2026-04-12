package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodLibraryViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    val foods: StateFlow<List<Food>> = repository.getAllFoods()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addFood(name: String, category: String) {
        viewModelScope.launch {
            val food = Food(
                name = name,
                category = category,
                weight = 1
            )
            repository.addFood(food)
        }
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }
}
