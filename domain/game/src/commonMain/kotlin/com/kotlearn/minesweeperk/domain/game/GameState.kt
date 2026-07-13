package com.kotlearn.minesweeperk.domain.game

data class GameState(
    val width: Int,
    val height: Int,
    val mineCount: Int,
    val tiles: List<List<Tile>>,
    val status: GameStatus,
) {

    val flagCount: Int
        get() = tiles.sumOf { column -> column.count { it.isFlagged } }

}
