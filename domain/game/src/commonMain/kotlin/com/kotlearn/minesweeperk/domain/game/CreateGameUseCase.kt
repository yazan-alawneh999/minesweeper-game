package com.kotlearn.minesweeperk.domain.game

class CreateGameUseCase {

    operator fun invoke(width: Int, height: Int, mineCount: Int): GameState {
        require(width > 0 && height > 0) { "Board must be at least 1x1" }
        require(mineCount in 0 until width * height) { "Mine count must leave at least one safe tile" }
        return GameState(
            width = width,
            height = height,
            mineCount = mineCount,
            tiles = List(width) { List(height) { Tile() } },
            status = GameStatus.PLAYING,
        )
    }

}
