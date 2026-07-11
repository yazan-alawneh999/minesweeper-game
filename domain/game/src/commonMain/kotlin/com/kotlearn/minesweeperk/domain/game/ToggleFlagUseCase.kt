package com.kotlearn.minesweeperk.domain.game

class ToggleFlagUseCase {

    operator fun invoke(gameState: GameState, x: Int, y: Int): GameState {
        if (gameState.status != GameStatus.PLAYING) return gameState
        val tile = gameState.tiles[x][y]
        if (tile.isRevealed) return gameState
        return gameState.copy(
            tiles = gameState.tiles.mapIndexed { tileX, column ->
                column.mapIndexed { tileY, currentTile ->
                    if (tileX == x && tileY == y) currentTile.copy(isFlagged = !currentTile.isFlagged) else currentTile
                }
            }
        )
    }

}
