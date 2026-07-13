package com.kotlearn.minesweeperk.domain.game

data class Tile(
    val isMine: Boolean = false,
    val isRevealed: Boolean = false,
    val isFlagged: Boolean = false,
    val adjacentMines: Int = 0,
)
