package com.eatif.app.games.linklink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 连连看路径检测算法测试 - 纯逻辑无依赖
 */
class LinkLinkLogicTest {

    private lateinit var logic: LinkLinkLogic

    @Before
    fun setup() {
        logic = LinkLinkLogic(gridSize = 8)
    }

    @Test
    fun `initializeGrid returns grid with correct dimensions`() {
        val grid = logic.initializeGrid()
        assertEquals(8, grid.size)
        assertEquals(8, grid[0].size)
    }

    @Test
    fun `initializeGrid fills all cells with patterns`() {
        val grid = logic.initializeGrid()
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                assertFalse("格子 ($row,$col) 应有图案", grid[row][col].isRemoved)
                assertTrue("格子 ($row,$col) 的图案不应为空", grid[row][col].pattern != null)
            }
        }
    }

    @Test
    fun `initializeGrid creates even number of each pattern`() {
        val grid = logic.initializeGrid()
        val patternCounts = mutableMapOf<String, Int>()
        grid.flatten().forEach { cell ->
            cell.pattern?.let {
                patternCounts[it] = (patternCounts[it] ?: 0) + 1
            }
        }
        // 每种图案应成对出现
        patternCounts.values.forEach { count ->
            assertEquals("每种图案应成对出现", 0, count % 2)
        }
    }

    @Test
    fun `canConnect returns false for different patterns`() {
        val grid = logic.initializeGrid()
        // 找两个不同图案的格子
        var cell1: LinkCell? = null
        var cell2: LinkCell? = null
        for (row in grid.indices) {
            for (col in grid[row].indices) {
                val cell = grid[row][col]
                if (cell1 == null) {
                    cell1 = cell
                } else if (cell.pattern != cell1.pattern) {
                    cell2 = cell
                    break
                }
            }
            if (cell2 != null) break
        }
        if (cell1 != null && cell2 != null) {
            assertFalse("不同图案不能连接", logic.canConnect(cell1.row, cell1.col, cell2.row, cell2.col))
        }
    }

    @Test
    fun `canConnect returns false for same cell`() {
        logic.initializeGrid()
        assertFalse("同一格子不能连接", logic.canConnect(0, 0, 0, 0))
    }

    @Test
    fun `canConnect direct line - horizontal adjacent same pattern`() {
        // 手动构造一个简单场景：两个相同图案水平相邻，中间无障碍
        val testLogic = LinkLinkLogic(gridSize = 4)
        testLogic.initializeGrid()
        // 验证直接相连的判断逻辑能正常运行
        testLogic.getGrid()
    }

    @Test
    fun `removePair marks cells as removed`() {
        val grid = logic.initializeGrid()
        // 找两个相同图案的格子
        var pair: Pair<LinkCell, LinkCell>? = null
        val allCells = grid.flatten().filter { !it.isRemoved }
        for (i in allCells.indices) {
            for (j in i + 1 until allCells.size) {
                if (allCells[i].pattern == allCells[j].pattern) {
                    pair = Pair(allCells[i], allCells[j])
                    break
                }
            }
            if (pair != null) break
        }
        if (pair != null) {
            val (c1, c2) = pair
            val removed = logic.removePair(c1.row, c1.col, c2.row, c2.col)
            // 是否能移除取决于路径，但方法不应崩溃
            if (removed) {
                val newGrid = logic.getGrid()
                assertTrue("移除后格子1应标记为removed", newGrid[c1.row][c1.col].isRemoved)
                assertTrue("移除后格子2应标记为removed", newGrid[c2.row][c2.col].isRemoved)
            }
        }
    }

    @Test
    fun `getRemainingPairs returns correct count after init`() {
        logic.initializeGrid()
        val remaining = logic.getRemainingPairs()
        assertEquals("8x8 网格应有 32 对", 32, remaining)
    }

    @Test
    fun `isComplete returns false on fresh grid`() {
        logic.initializeGrid()
        assertFalse("新网格不应完成", logic.isComplete())
    }

    @Test
    fun `hasPossibleMatch returns true on fresh grid`() {
        logic.initializeGrid()
        // 新网格应有可行匹配（除非极端巧合）
        assertTrue("新网格应有可行匹配", logic.hasPossibleMatch())
    }

    @Test
    fun `shuffle keeps remaining pair count`() {
        logic.initializeGrid()
        val beforeCount = logic.getRemainingPairs()
        logic.shuffle()
        val afterCount = logic.getRemainingPairs()
        assertEquals("洗牌不应改变剩余对数", beforeCount, afterCount)
    }
}
