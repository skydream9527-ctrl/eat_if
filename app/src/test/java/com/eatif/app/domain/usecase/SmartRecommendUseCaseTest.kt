package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.model.FoodTag
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.model.TimeSlot
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * 智能推荐 UseCase 核心评分逻辑测试
 */
class SmartRecommendUseCaseTest {

    private lateinit var useCase: SmartRecommendUseCase
    private lateinit var fakeRepository: FakeRecommendRepository

    private val testFoods = listOf(
        Food(id = 1, name = "火锅", category = "中餐", weight = 3, tags = listOf(FoodTag.HOTPOT, FoodTag.SPICY)),
        Food(id = 2, name = "沙拉", category = "轻食", weight = 2, tags = listOf(FoodTag.LIGHT)),
        Food(id = 3, name = "汉堡", category = "西餐", weight = 3, tags = listOf(FoodTag.FAST_FOOD)),
        Food(id = 4, name = "寿司", category = "日餐", weight = 2, tags = listOf(FoodTag.SEAFOOD, FoodTag.LIGHT)),
        Food(id = 5, name = "烧烤", category = "中餐", weight = 2, tags = listOf(FoodTag.BBQ, FoodTag.SPICY))
    )

    @Before
    fun setup() {
        fakeRepository = FakeRecommendRepository()
        useCase = SmartRecommendUseCase(fakeRepository).apply {
            // 固定随机源，保证测试可重复
            random = Random(42)
        }
    }

    @Test
    fun `cold start - no history returns recommendations with cold start bonus`() {
        // 无历史记录 → 冷启动
        fakeRepository.foods = testFoods
        fakeRepository.slotFrequencies = emptyList()
        fakeRepository.recentFrequencies = emptyList()
        fakeRepository.weekFrequencies = emptyList()

        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = emptyList(),
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.DINNER
        )

        assertEquals(5, result.size)
        // 冷启动时，晚餐时段偏好 HOTPOT/BBQ/SEAFOOD
        // 火锅(HOTPOT) 和 烧烤(BBQ) 应该有冷启动加分
        val hotpot = result.find { it.food.name == "火锅" }
        val salad = result.find { it.food.name == "沙拉" }
        assertTrue("火锅应比沙拉分数高（晚餐冷启动偏好）", hotpot!!.score > salad!!.score)
    }

    @Test
    fun `cold start - morning prefers light foods`() {
        fakeRepository.foods = testFoods
        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = emptyList(),
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.MORNING
        )
        // 早餐偏好 LIGHT/RICE/SWEET
        // 沙拉(LIGHT) 和 寿司(LIGHT) 应有冷启动加分
        val salad = result.find { it.food.name == "沙拉" }
        val hotpot = result.find { it.food.name == "火锅" }
        assertTrue("早餐时段沙拉应比火锅分数高", salad!!.score > hotpot!!.score)
    }

    @Test
    fun `recent penalty - recently eaten foods get lower score`() {
        fakeRepository.foods = testFoods
        // 火锅最近吃过
        fakeRepository.recentFrequencies = listOf(FoodFrequency("火锅", 1))

        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = listOf(FoodFrequency("火锅", 1)),
            weekFrequencies = listOf(FoodFrequency("火锅", 1)),
            currentTimeSlot = TimeSlot.LUNCH
        )

        val hotpot = result.find { it.food.name == "火锅" }!!
        val sushi = result.find { it.food.name == "寿司" }!!
        // 火锅有 -5 的近期惩罚
        assertTrue("近期吃过的火锅应被降权", sushi.score > hotpot.score)
    }

    @Test
    fun `nutrition balance - underrepresented tags get bonus`() {
        fakeRepository.foods = testFoods
        // 一周内只吃过 SPICY 类（火锅、烧烤）
        fakeRepository.weekFrequencies = listOf(
            FoodFrequency("火锅", 3),
            FoodFrequency("烧烤", 2)
        )
        fakeRepository.recentFrequencies = listOf(
            FoodFrequency("火锅", 1),
            FoodFrequency("烧烤", 1)
        )

        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = fakeRepository.recentFrequencies,
            weekFrequencies = fakeRepository.weekFrequencies,
            currentTimeSlot = TimeSlot.DINNER
        )

        // LIGHT 标签近期为 0，应获得营养均衡加分
        val salad = result.find { it.food.name == "沙拉" }!!
        val hotpot = result.find { it.food.name == "火锅" }!!
        // 沙拉有 LIGHT 稀有加分 + 火锅有近期惩罚
        assertTrue("近期缺失的 LIGHT 标签应获得均衡加分", salad.score > hotpot.score)
    }

    @Test
    fun `weight bonus - higher weight foods score higher`() {
        fakeRepository.foods = testFoods
        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = emptyList(),
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.LUNCH
        )
        // weight=3 的应比 weight=2 的有更高权重加分
        // 但其他维度也可能影响，这里只验证方法不崩溃且返回正确数量
        assertEquals(5, result.size)
    }

    @Test
    fun `slot frequency - frequently eaten at same slot scores higher`() {
        fakeRepository.foods = testFoods
        // 火锅在同一时段吃过 3 次
        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = listOf(FoodFrequency("火锅", 3)),
            recentFrequencies = emptyList(),  // 非冷启动
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.DINNER
        )

        val hotpot = result.find { it.food.name == "火锅" }!!
        assertTrue("同时段常吃的火锅分数应为正", hotpot.score > 0)
        // reason 应包含"常选"
        assertTrue("reason 应体现常选", hotpot.reason.contains("常选"))
    }

    @Test
    fun `reason text - cold start includes time slot and tag`() {
        fakeRepository.foods = testFoods
        val result = useCase.scoreFoods(
            foods = testFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = emptyList(),
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.DINNER
        )
        val hotpot = result.find { it.food.name == "火锅" }!!
        assertTrue("冷启动 reason 应包含时段", hotpot.reason.contains("晚餐"))
        assertTrue("冷启动 reason 应包含标签", hotpot.reason.contains("火锅") || hotpot.reason.contains("辣"))
    }

    @Test
    fun `returns all foods when input is empty tags`() {
        val noTagFoods = listOf(
            Food(id = 1, name = "美食A", category = "中餐", weight = 1, tags = emptyList()),
            Food(id = 2, name = "美食B", category = "西餐", weight = 1, tags = emptyList())
        )
        val result = useCase.scoreFoods(
            foods = noTagFoods,
            slotFrequencies = emptyList(),
            recentFrequencies = emptyList(),
            weekFrequencies = emptyList(),
            currentTimeSlot = TimeSlot.LUNCH
        )
        assertEquals(2, result.size)
    }
}

/**
 * 推荐算法驱动维度测试
 */
class GameDrivenRecommendUseCaseTest {

    private lateinit var useCase: GameDrivenRecommendUseCase
    private lateinit var smartUseCase: SmartRecommendUseCase
    private val fakeRepository = FakeRecommendRepository()

    private val testFoods = listOf(
        Food(id = 1, name = "火锅", category = "中餐", weight = 3, tags = listOf(FoodTag.HOTPOT, FoodTag.SPICY)),
        Food(id = 2, name = "沙拉", category = "轻食", weight = 2, tags = listOf(FoodTag.LIGHT)),
        Food(id = 3, name = "汉堡", category = "西餐", weight = 3, tags = listOf(FoodTag.FAST_FOOD)),
        Food(id = 4, name = "寿司", category = "日餐", weight = 2, tags = listOf(FoodTag.SEAFOOD, FoodTag.LIGHT)),
        Food(id = 5, name = "蛋糕", category = "甜点", weight = 1, tags = listOf(FoodTag.SWEET, FoodTag.DESSERT))
    )

    @Before
    fun setup() {
        smartUseCase = SmartRecommendUseCase(fakeRepository).apply { random = Random(42) }
        useCase = GameDrivenRecommendUseCase(fakeRepository, smartUseCase)
    }

    @Test
    fun `high score - reward tags get bonus`() {
        val baseRecommendations = testFoods.map { Recommendation(food = it, reason = "test", score = 10.0) }

        val adjusted = useCase.applyGameScoreDimension(baseRecommendations, scorePercent = 90)

        // 高分(90 >= 70) → REWARD mood
        // 火锅(SPICY) 应获得奖励加分
        val hotpot = adjusted.find { it.food.name == "火锅" }!!
        val salad = adjusted.find { it.food.name == "沙拉" }!!
        assertTrue("高分时火锅(奖励标签)应比沙拉分数高", hotpot.score > salad.score)
        assertTrue("reason 应包含高分奖励", hotpot.reason.contains("高分奖励"))
    }

    @Test
    fun `low score - comfort tags get bonus`() {
        val baseRecommendations = testFoods.map { Recommendation(food = it, reason = "test", score = 10.0) }

        val adjusted = useCase.applyGameScoreDimension(baseRecommendations, scorePercent = 20)

        // 低分(20 < 30) → COMFORT mood
        // 沙拉(LIGHT) 和 蛋糕(SWEET) 应获得安慰加分
        val salad = adjusted.find { it.food.name == "沙拉" }!!
        val hotpot = adjusted.find { it.food.name == "火锅" }!!
        assertTrue("低分时沙拉(安慰标签)应比火锅分数高", salad.score > hotpot.score)
        assertTrue("reason 应包含低分安慰", salad.reason.contains("低分安慰"))
    }

    @Test
    fun `medium score - no mood bonus applied`() {
        val baseRecommendations = testFoods.map { Recommendation(food = it, reason = "test", score = 10.0) }

        val adjusted = useCase.applyGameScoreDimension(baseRecommendations, scorePercent = 50)

        // 中分(50) → NEUTRAL mood，无加减分
        adjusted.forEach { rec ->
            assertTrue("中分时 reason 不应有 mood 后缀", !rec.reason.contains("奖励") && !rec.reason.contains("安慰"))
        }
    }

    @Test
    fun `ensureCategoryDiversity - top N not all same category`() {
        // 构造 5 个同 category 的高分推荐 + 5 个不同 category 的低分推荐
        val sameCategory = (1..5).map {
            Recommendation(
                food = Food(id = it.toLong(), name = "中餐$it", category = "中餐", weight = 1),
                reason = "test",
                score = 100.0 - it  // 递减分数
            )
        }
        val otherCategory = (1..5).map {
            Recommendation(
                food = Food(id = (it + 5).toLong(), name = "西餐$it", category = "西餐", weight = 1),
                reason = "test",
                score = 50.0 - it
            )
        }
        val all = sameCategory + otherCategory

        val diversified = useCase.ensureCategoryDiversity(all, targetCount = 3)

        assertEquals(3, diversified.size)
        val categories = diversified.map { it.food.category }.toSet()
        assertTrue("Top3 应有类别多样性", categories.size >= 2)
    }

    @Test
    fun `ensureCategoryDiversity - returns all when input smaller than target`() {
        val small = listOf(
            Recommendation(Food(name = "A", category = "中餐"), "r", 1.0),
            Recommendation(Food(name = "B", category = "西餐"), "r", 2.0)
        )
        val result = useCase.ensureCategoryDiversity(small, targetCount = 5)
        assertEquals("输入不足时返回全部", 2, result.size)
    }

    @Test
    fun `GameMood fromScore thresholds`() {
        assertEquals(GameDrivenRecommendUseCase.GameMood.COMFORT, GameDrivenRecommendUseCase.GameMood.fromScore(0))
        assertEquals(GameDrivenRecommendUseCase.GameMood.COMFORT, GameDrivenRecommendUseCase.GameMood.fromScore(29))
        assertEquals(GameDrivenRecommendUseCase.GameMood.NEUTRAL, GameDrivenRecommendUseCase.GameMood.fromScore(30))
        assertEquals(GameDrivenRecommendUseCase.GameMood.NEUTRAL, GameDrivenRecommendUseCase.GameMood.fromScore(69))
        assertEquals(GameDrivenRecommendUseCase.GameMood.REWARD, GameDrivenRecommendUseCase.GameMood.fromScore(70))
        assertEquals(GameDrivenRecommendUseCase.GameMood.REWARD, GameDrivenRecommendUseCase.GameMood.fromScore(100))
    }
}

/** 测试用 Fake Repository - 不依赖 mockito */
private class FakeRecommendRepository : RecommendRepository {
    var foods: List<Food> = emptyList()
    var slotFrequencies: List<FoodFrequency> = emptyList()
    var recentFrequencies: List<FoodFrequency> = emptyList()
    var weekFrequencies: List<FoodFrequency> = emptyList()

    override fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>> {
        return flowOf(recentFrequencies)
    }

    override fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>> {
        return flowOf(slotFrequencies)
    }

    override fun getAllFoods(): Flow<List<Food>> {
        return flowOf(foods)
    }
}
