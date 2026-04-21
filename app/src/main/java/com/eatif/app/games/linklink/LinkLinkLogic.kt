package com.eatif.app.games.linklink

import kotlin.random.Random

data class LinkLinkCell(
    val pattern: Int,
    val removed: Boolean = false
)

class LinkLinkLogic(
    val gridSize: Int = 8
) {
    var grid: Array<Array<LinkLinkCell?>> = Array(gridSize) { Array(gridSize) { null } }
    var patterns: IntArray = IntArray(gridSize * gridSize)
    
    private val patternEmojis = listOf(
        "🍎", "🍊", "🍋", "🍇", "🍓", "🍑", "🥝", "🍒",
        "🍕", "🍔", "🍜", "🍣", "🥗", "🍩", "🍪", "🍰"
    )
    
    fun initializeGrid() {
        val totalCells = gridSize * gridSize
        val numPatterns = 16
        val repeatsPerPattern = totalCells / numPatterns
        
        val patternsList = mutableListOf<Int>()
        for (pattern in 0 until numPatterns) {
            for (i in 0 until repeatsPerPattern) {
                patternsList.add(pattern)
            }
        }
        
        while (patternsList.size < totalCells) {
            patternsList.add(Random.nextInt(numPatterns))
        }
        
        patternsList.shuffle()
        
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val pattern = patternsList[index]
                grid[row][col] = LinkLinkCell(pattern)
                patterns[index] = pattern
            }
        }
    }
    
    fun getPattern(row: Int, col: Int): Int? {
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return null
        val cell = grid[row][col]
        return if (cell?.removed == false) cell.pattern else null
    }
    
    fun isRemoved(row: Int, col: Int): Boolean {
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return true
        return grid[row][col]?.removed ?: true
    }
    
    fun removePair(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (!canConnect(row1, col1, row2, col2)) return false
        if (getPattern(row1, col1) != getPattern(row2, col2)) return false
        
        grid[row1][col1] = grid[row1][col1]?.copy(removed = true)
        grid[row2][col2] = grid[row2][col2]?.copy(removed = true)
        return true
    }
    
    fun canConnect(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (row1 == row2 && col1 == col2) return false
        if (isRemoved(row1, col1) || isRemoved(row2, col2)) return false
        
        return canConnectStraight(row1, col1, row2, col2) ||
               canConnectOneTurn(row1, col1, row2, col2) ||
               canConnectTwoTurns(row1, col1, row2, col2)
    }
    
    private fun canConnectStraight(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (row1 == row2) {
            val minCol = minOf(col1, col2)
            val maxCol = maxOf(col1, col2)
            for (col in minCol + 1 until maxCol) {
                if (!isRemoved(row1, col)) return false
            }
            return true
        }
        
        if (col1 == col2) {
            val minRow = minOf(row1, row2)
            val maxRow = maxOf(row1, row2)
            for (row in minRow + 1 until maxRow) {
                if (!isRemoved(row, col1)) return false
            }
            return true
        }
        
        return false
    }
    
    private fun canConnectOneTurn(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val turnPoint1Row = row1
        val turnPoint1Col = col2
        val turnPoint2Row = row2
        val turnPoint2Col = col1
        
        if (isValidTurnPoint(turnPoint1Row, turnPoint1Col)) {
            if (canConnectStraight(row1, col1, turnPoint1Row, turnPoint1Col) &&
                canConnectStraight(turnPoint1Row, turnPoint1Col, row2, col2)) {
                return true
            }
        }
        
        if (isValidTurnPoint(turnPoint2Row, turnPoint2Col)) {
            if (canConnectStraight(row1, col1, turnPoint2Row, turnPoint2Col) &&
                canConnectStraight(turnPoint2Row, turnPoint2Col, row2, col2)) {
                return true
            }
        }
        
        return false
    }
    
    private fun canConnectTwoTurns(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        for (row in -1..gridSize) {
            if (canConnectStraight(row1, col1, row, col1) &&
                isValidPath(row, col1, row, col2) &&
                canConnectStraight(row, col2, row2, col2)) {
                return true
            }
        }
        
        for (col in -1..gridSize) {
            if (canConnectStraight(row1, col1, row1, col) &&
                isValidPath(row1, col, row2, col) &&
                canConnectStraight(row2, col, row2, col2)) {
                return true
            }
        }
        
        return false
    }
    
    private fun isValidTurnPoint(row: Int, col: Int): Boolean {
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return true
        return isRemoved(row, col)
    }
    
    private fun isValidPath(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (row1 == row2) {
            val minCol = minOf(col1, col2)
            val maxCol = maxOf(col1, col2)
            for (col in minCol..maxCol) {
                if (col >= 0 && col < gridSize && row1 >= 0 && row1 < gridSize) {
                    if (!isRemoved(row1, col)) return false
                }
            }
            return true
        }
        
        if (col1 == col2) {
            val minRow = minOf(row1, row2)
            val maxRow = maxOf(row1, row2)
            for (row in minRow..maxRow) {
                if (row >= 0 && row < gridSize && col1 >= 0 && col1 < gridSize) {
                    if (!isRemoved(row, col1)) return false
                }
            }
            return true
        }
        
        return false
    }
    
    fun hasPossibleMatch(): Boolean {
        val remainingCells = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!isRemoved(row, col)) {
                    remainingCells.add(Pair(row, col))
                }
            }
        }
        
        for (i in remainingCells.indices) {
            for (j in i + 1 until remainingCells.size) {
                val (r1, c1) = remainingCells[i]
                val (r2, c2) = remainingCells[j]
                if (getPattern(r1, c1) == getPattern(r2, c2) && canConnect(r1, c1, r2, c2)) {
                    return true
                }
            }
        }
        
        return false
    }
    
    fun shuffle(): Int {
        val remainingCells = mutableListOf<Pair<Int, Int>>()
        val remainingPatterns = mutableListOf<Int>()
        
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!isRemoved(row, col)) {
                    remainingCells.add(Pair(row, col))
                    remainingPatterns.add(getPattern(row, col)!!)
                }
            }
        }
        
        remainingPatterns.shuffle()
        
        for ((index, cell) in remainingCells.withIndex()) {
            val (row, col) = cell
            grid[row][col] = LinkLinkCell(remainingPatterns[index])
        }
        
        return remainingCells.size
    }
    
    fun getRemainingCount(): Int {
        var count = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!isRemoved(row, col)) count++
            }
        }
        return count
    }
    
    fun isComplete(): Boolean {
        return getRemainingCount() == 0
    }
    
    fun getPatternEmoji(pattern: Int): String {
        return if (pattern < patternEmojis.size) patternEmojis[pattern] else "?"
    }
    
    fun clearGrid() {
        grid = Array(gridSize) { Array(gridSize) { null } }
    }
}