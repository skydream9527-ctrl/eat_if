package com.eatif.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    /** 批量导入用户勾选的美食，返回插入的条数 */
    fun importFoods(foods: List<Food>, onDone: () -> Unit) {
        if (foods.isEmpty()) {
            onDone()
            return
        }
        viewModelScope.launch {
            runCatching {
                foodRepository.addFoods(foods)
            }
            onDone()
        }
    }
}
