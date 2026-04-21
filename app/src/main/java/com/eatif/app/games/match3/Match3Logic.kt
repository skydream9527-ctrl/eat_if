package com.eatif.app.games.match3

import kotlin.random.Random

enum class GemColor {
    RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE
}

data class Position(val row: Int, val col: Int)

class Match3Logic(
    val gridSize: Int = 6
) {
    private val colors = GemColor.entries.toTypedArray()
    var grid: Array<Array<GemColor?>> = Array(gridSize) { Array(gridSize) { null } }

    fun initializeGrid() {
        fillGrid()
        while (hasMatches()) {
            removeMatches()
            dropGems()
            fillEmptyCells()
        }
    }

    private fun fillGrid() {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                grid[row][col] = colors[Random.nextInt(colors.size)]
            }
        }
    }

    fun swapGems(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (!isValidSwap(row1, col1, row2, col2)) return false

        val temp = grid[row1][col1]
        grid[row1][col1] = grid[row2][col2]
        grid[row2][col2] = temp

        val createsMatch = hasMatches()
        if (!createsMatch) {
            grid[row2][col2] = grid[row1][col1]
            grid[row1][col1] = temp
        }
        return createsMatch
    }

    private fun isValidSwap(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (row1 < 0 || row1 >= gridSize || col1 < 0 || col1 >= gridSize) return false
        if (row2 < 0 || row2 >= gridSize || col2 < 0 || col2 >= gridSize) return false
        val rowDiff = kotlin.math.abs(row1 - row2)
        val colDiff = kotlin.math.abs(col1 - col2)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    fun hasMatches(): Boolean {
        return findMatches().isNotEmpty()
    }

    fun findMatches(): Set<Position> {
        val matches = mutableSetOf<Position>()

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize - 2) {
                val color = grid[row][col]
                if (color != null &&
                    grid[row][col + 1] == color &&
                    grid[row][col + 2] == color
                ) {
                    matches.add(Position(row, col))
                    matches.add(Position(row, col + 1))
                    matches.add(Position(row, col + 2))
                    for (c in col + 3 until gridSize) {
                        if (grid[row][c] == color) matches.add(Position(row, c))
                        else break
                    }
                }
            }
        }

        for (col in 0 until gridSize) {
            for (row in 0 until gridSize - 2) {
                val color = grid[row][col]
                if (color != null &&
                    grid[row + 1][col] == color &&
                    grid[row + 2][col] == color
                ) {
                    matches.add(Position(row, col))
                    matches.add(Position(row + 1, col))
                    matches.add(Position(row + 2, col))
                    for (r in row + 3 until gridSize) {
                        if (grid[r][col] == color) matches.add(Position(r, col))
                        else break
                    }
                }
            }
        }

        return matches
    }

    fun removeMatches(): Int {
        val matches = findMatches()
        for (pos in matches) {
            grid[pos.row][pos.col] = null
        }
        return matches.size
    }

    fun dropGems(): List<Position> {
        val movedPositions = mutableListOf<Position>()

        for (col in 0 until gridSize) {
            var writeRow = gridSize - 1
            for (row in gridSize - 1 downTo 0) {
                if (grid[row][col] != null) {
                    if (row != writeRow) {
                        grid[writeRow][col] = grid[row][col]
                        grid[row][col] = null
                        movedPositions.add(Position(writeRow, col))
                    }
                    writeRow--
                }
            }
        }

        return movedPositions
    }

    fun fillEmptyCells(): List<Position> {
        val filledPositions = mutableListOf<Position>()

        for (col in 0 until gridSize) {
            for (row in gridSize - 1 downTo 0) {
                if (grid[row][col] == null) {
                    grid[row][col] = colors[Random.nextInt(colors.size)]
                    filledPositions.add(Position(row, col))
                }
            }
        }

        return filledPositions
    }

    fun processTurn(): Int {
        var totalRemoved = 0
        while (hasMatches()) {
            totalRemoved += removeMatches()
            dropGems()
            fillEmptyCells()
        }
        return totalRemoved
    }

    fun isGameOver(movesLeft: Int): Boolean {
        return movesLeft <= 0 || !hasAnyPossibleMatch()
    }

    fun hasAnyPossibleMatch(): Boolean {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (col < gridSize - 1 && wouldCreateMatch(row, col, row, col + 1)) return true
                if (row < gridSize - 1 && wouldCreateMatch(row, col, row + 1, col)) return true
            }
        }
        return false
    }

    private fun wouldCreateMatch(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        val temp = grid[row1][col1]
        grid[row1][col1] = grid[row2][col2]
        grid[row2][col2] = temp

        val createsMatch = wouldMatchAt(row1, col1) || wouldMatchAt(row2, col2)

        grid[row2][col2] = grid[row1][col1]
        grid[row1][col1] = temp

        return createsMatch
    }

    private fun wouldMatchAt(row: Int, col: Int): Boolean {
        val color = grid[row][col]
        if (color == null) return false

        var horizontalCount = 1
        for (c in col - 1 downTo 0) {
            if (grid[row][c] == color) horizontalCount++
            else break
        }
        for (c in col + 1 until gridSize) {
            if (grid[row][c] == color) horizontalCount++
            else break
        }
        if (horizontalCount >= 3) return true

        var verticalCount = 1
        for (r in row - 1 downTo 0) {
            if (grid[r][col] == color) verticalCount++
            else break
        }
        for (r in row + 1 until gridSize) {
            if (grid[r][col] == color) verticalCount++
            else break
        }
        return verticalCount >= 3
    }

    fun getGem(row: Int, col: Int): GemColor? {
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) return null
        return grid[row][col]
    }

    fun setGem(row: Int, col: Int, color: GemColor?) {
        if (row >= 0 && row < gridSize && col >= 0 && col < gridSize) {
            grid[row][col] = color
        }
    }

    fun clearGrid() {
        grid = Array(gridSize) { Array(gridSize) { null } }
    }
}