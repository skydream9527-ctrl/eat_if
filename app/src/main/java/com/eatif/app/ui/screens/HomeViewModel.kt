package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.usecase.GetFoodCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFoodCountUseCase: GetFoodCountUseCase
) : ViewModel() {

    private val _foodCount = MutableStateFlow(0)
    val foodCount: StateFlow<Int> = _foodCount.asStateFlow()

    init {
        viewModelScope.launch {
            getFoodCountUseCase().collect { count ->
                _foodCount.value = count
            }
        }
    }
}
