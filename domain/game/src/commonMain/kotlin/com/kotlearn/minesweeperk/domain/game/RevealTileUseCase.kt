package com.kotlearn.minesweeperk.domain.game

import kotlin.random.Random

class RevealTileUseCase {

    operator fun invoke(gameState: GameState, x: Int, y: Int, random: Random = Random.Default): GameState {
        if (gameState.status != GameStatus.PLAYING) return gameState
        if (gameState.tiles[x][y].isRevealed || gameState.tiles[x][y].isFlagged) return gameState

        // Mines are only placed on the first reveal, so the first revealed tile is never a mine
        val state = if (gameState.minesArePlaced) {
            gameState
        } else {
            gameState.withMinesPlaced(safeX = x, safeY = y, random = random)
        }

        return if (state.tiles[x][y].isMine) {
            state.copy(
                tiles = state.tiles.map { column ->
                    column.map { tile ->
                        if (tile.isMine) tile.copy(isRevealed = true) else tile
                    }
                },
                status = GameStatus.LOST,
            )
        } else {
            state.withTileFloodRevealed(x = x, y = y)
        }
    }

    private val GameState.minesArePlaced: Boolean
        get() = tiles.any { column -> column.any { it.isMine } }

    private fun GameState.withMinesPlaced(safeX: Int, safeY: Int, random: Random): GameState {
        // Keep the tapped tile and its neighbours mine-free so the first reveal
        // always has zero adjacent mines and flood-reveals an area. On boards too
        // dense to spare the whole neighbourhood, fall back to only the tapped
        // tile so it is at least never a mine.
        val safeZone = (neighboursOf(safeX, safeY) + (safeX to safeY)).toSet()
        val clearedNeighbourhood = allPositions().filterNot { it in safeZone }
        val candidates = if (clearedNeighbourhood.size >= mineCount) {
            clearedNeighbourhood
        } else {
            allPositions().filterNot { it == safeX to safeY }
        }
        val minePositions = candidates
            .shuffled(random)
            .take(mineCount)
            .toSet()
        return copy(
            tiles = List(width) { x ->
                List(height) { y ->
                    tiles[x][y].copy(
                        isMine = x to y in minePositions,
                        adjacentMines = neighboursOf(x, y).count { it in minePositions },
                    )
                }
            }
        )
    }

    private fun GameState.withTileFloodRevealed(x: Int, y: Int): GameState {
        val positionsToReveal = mutableSetOf<Pair<Int, Int>>()
        val positionsToVisit = ArrayDeque(listOf(x to y))
        while (positionsToVisit.isNotEmpty()) {
            val position = positionsToVisit.removeFirst()
            if (!positionsToReveal.add(position)) continue
            val (tileX, tileY) = position
            if (tiles[tileX][tileY].adjacentMines == 0) {
                positionsToVisit += neighboursOf(tileX, tileY).filter { (neighbourX, neighbourY) ->
                    val neighbour = tiles[neighbourX][neighbourY]
                    !neighbour.isRevealed && !neighbour.isFlagged && (neighbourX to neighbourY) !in positionsToReveal
                }
            }
        }
        val revealedTiles = List(width) { tileX ->
            List(height) { tileY ->
                val tile = tiles[tileX][tileY]
                if (tileX to tileY in positionsToReveal) tile.copy(isRevealed = true) else tile
            }
        }
        val allSafeTilesRevealed = revealedTiles.all { column ->
            column.all { it.isMine || it.isRevealed }
        }
        return copy(
            tiles = revealedTiles,
            status = if (allSafeTilesRevealed) GameStatus.WON else GameStatus.PLAYING,
        )
    }

    private fun GameState.allPositions(): List<Pair<Int, Int>> =
        (0 until width).flatMap { x -> (0 until height).map { y -> x to y } }

    private fun GameState.neighboursOf(x: Int, y: Int): List<Pair<Int, Int>> =
        (x - 1..x + 1).flatMap { neighbourX ->
            (y - 1..y + 1).map { neighbourY -> neighbourX to neighbourY }
        }.filter { (neighbourX, neighbourY) ->
            neighbourX in 0 until width && neighbourY in 0 until height && (neighbourX to neighbourY) != (x to y)
        }

}
