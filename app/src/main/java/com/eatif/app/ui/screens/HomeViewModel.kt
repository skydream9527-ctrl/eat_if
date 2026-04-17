package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.usecase.GetFoodCountUseCase
import com.eatif.app.domain.usecase.SmartRecommendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFoodCountUseCase: GetFoodCountUseCase,
    private val smartRecommendUseCase: SmartRecommendUseCase
) : ViewModel() {

    private val _foodCount = MutableStateFlow(0)
    val foodCount: StateFlow<Int> = _foodCount.asStateFlow()

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    init {
        viewModelScope.launch {
            getFoodCountUseCase().collect { count ->
                _foodCount.value = count
            }
        }
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            smartRecommendUseCase(5).collect { result ->
                result.onSuccess { _recommendations.value = it }
            }
        }
    }
}
