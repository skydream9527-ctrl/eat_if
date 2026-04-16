package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AddFoodUseCaseTest {

    private val repository = mock<FoodRepository>()

    @Test
    fun `addFood returns success when food name and category are valid`() = runTest {
        val useCase = AddFoodUseCase(repository)
        val food = Food(name = "火锅", category = "中餐", weight = 1)

        whenever(repository.addFood(food)).thenReturn(1L)
        whenever(repository.addFood(any())).thenReturn(1L)

        val result = useCase(food)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `addFood returns failure when food name is blank`() = runTest {
        val useCase = AddFoodUseCase(repository)
        val food = Food(name = "  ", category = "中餐", weight = 1)

        val result = useCase(food)

        assertTrue(result.isFailure)
        assertEquals("美食名称不能为空", result.exceptionOrNull()?.message)
    }

    @Test
    fun `addFood returns failure when category is blank`() = runTest {
        val useCase = AddFoodUseCase(repository)
        val food = Food(name = "火锅", category = "", weight = 1)

        val result = useCase(food)

        assertTrue(result.isFailure)
        assertEquals("分类不能为空", result.exceptionOrNull()?.message)
    }
}

class GetRandomFoodUseCaseTest {

    private val repository = mock<FoodRepository>()

    @Test
    fun `getRandomFood returns failure when foods is empty`() = runTest {
        val useCase = GetRandomFoodUseCase(repository)
        whenever(repository.getAllFoods()).thenReturn(flowOf(emptyList()))

        var result: Result<List<Food>>? = null
        useCase().collect { result = it }

        assertTrue(result!!.isFailure)
        assertEquals("美食库为空", result!!.exceptionOrNull()?.message)
    }

    @Test
    fun `getRandomFood returns food based on weight`() = runTest {
        val useCase = GetRandomFoodUseCase(repository)
        val foods = listOf(
            Food(id = 1, name = "火锅", category = "中餐", weight = 3),
            Food(id = 2, name = "寿司", category = "日餐", weight = 1)
        )
        whenever(repository.getAllFoods()).thenReturn(flowOf(foods))

        var result: Result<List<Food>>? = null
        useCase(count = 1).collect { result = it }

        assertTrue(result!!.isSuccess)
        val selectedFoods = result!!.getOrNull()!!
        assertEquals(1, selectedFoods.size)
        assertTrue(selectedFoods.first().name in listOf("火锅", "寿司"))
    }
}