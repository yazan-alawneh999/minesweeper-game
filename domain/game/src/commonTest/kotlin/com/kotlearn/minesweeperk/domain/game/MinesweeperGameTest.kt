package com.kotlearn.minesweeperk.domain.game

import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinesweeperGameTest {

    private val createGameUseCase = CreateGameUseCase()
    private val revealTileUseCase = RevealTileUseCase()
    private val toggleFlagUseCase = ToggleFlagUseCase()

    @Test
    fun newGameHasCorrectSizeAndAllTilesHidden() {
        val gameState = createGameUseCase(width = 10, height = 16, mineCount = 25)

        assertEquals(10, gameState.tiles.size)
        assertTrue(gameState.tiles.all { it.size == 16 })
        assertTrue(gameState.tiles.all { column -> column.none { it.isRevealed || it.isFlagged || it.isMine } })
        assertEquals(GameStatus.PLAYING, gameState.status)
    }

    @Test
    fun firstRevealedTileIsNeverAMine() {
        for (seed in 0..49) {
            val gameState = createGameUseCase(width = 9, height = 9, mineCount = 40)
            val revealedState = revealTileUseCase(gameState, x = 4, y = 4, random = Random(seed))

            assertTrue(revealedState.tiles[4][4].isRevealed)
            assertFalse(revealedState.tiles[4][4].isMine)
            assertEquals(40, revealedState.tiles.sumOf { column -> column.count { it.isMine } })
        }
    }

    @Test
    fun revealingNumberTileRevealsOnlyThatTile() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0))

        val revealedState = revealTileUseCase(gameState, x = 1, y = 1)

        assertTrue(revealedState.tiles[1][1].isRevealed)
        assertEquals(1, revealedState.tiles[1][1].adjacentMines)
        assertEquals(1, revealedState.tiles.sumOf { column -> column.count { it.isRevealed } })
        assertEquals(GameStatus.PLAYING, revealedState.status)
    }

    @Test
    fun revealingEmptyTileFloodRevealsNeighbours() {
        val gameState = buildGameState(width = 5, height = 5, minePositions = setOf(0 to 0))

        val revealedState = revealTileUseCase(gameState, x = 4, y = 4)

        assertFalse(revealedState.tiles[0][0].isRevealed)
        assertEquals(24, revealedState.tiles.sumOf { column -> column.count { it.isRevealed } })
    }

    @Test
    fun floodRevealSkipsFlaggedTiles() {
        val gameState = buildGameState(width = 5, height = 5, minePositions = setOf(0 to 0))
        val flaggedState = toggleFlagUseCase(gameState, x = 0, y = 4)

        val revealedState = revealTileUseCase(flaggedState, x = 4, y = 4)

        assertFalse(revealedState.tiles[0][4].isRevealed)
        assertTrue(revealedState.tiles[0][4].isFlagged)
        assertEquals(GameStatus.PLAYING, revealedState.status)
    }

    @Test
    fun revealingMineLosesAndRevealsAllMines() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0, 2 to 2))

        val revealedState = revealTileUseCase(revealTileUseCase(gameState, x = 1, y = 1), x = 0, y = 0)

        assertEquals(GameStatus.LOST, revealedState.status)
        assertTrue(revealedState.tiles[0][0].isRevealed)
        assertTrue(revealedState.tiles[2][2].isRevealed)
    }

    @Test
    fun revealingAllSafeTilesWinsTheGame() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0))

        val revealedState = revealTileUseCase(gameState, x = 2, y = 2)

        assertEquals(GameStatus.WON, revealedState.status)
        assertFalse(revealedState.tiles[0][0].isRevealed)
        assertEquals(8, revealedState.tiles.sumOf { column -> column.count { it.isRevealed } })
    }

    @Test
    fun togglingFlagFlagsAndUnflagsTile() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0))

        val flaggedState = toggleFlagUseCase(gameState, x = 1, y = 1)
        assertTrue(flaggedState.tiles[1][1].isFlagged)
        assertEquals(1, flaggedState.flagCount)

        val unflaggedState = toggleFlagUseCase(flaggedState, x = 1, y = 1)
        assertFalse(unflaggedState.tiles[1][1].isFlagged)
        assertEquals(0, unflaggedState.flagCount)
    }

    @Test
    fun flaggedTileCannotBeRevealed() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0))
        val flaggedState = toggleFlagUseCase(gameState, x = 0, y = 0)

        val revealedState = revealTileUseCase(flaggedState, x = 0, y = 0)

        assertEquals(flaggedState, revealedState)
    }

    @Test
    fun revealedTileCannotBeFlagged() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0))
        val revealedState = revealTileUseCase(gameState, x = 1, y = 1)

        val flaggedState = toggleFlagUseCase(revealedState, x = 1, y = 1)

        assertEquals(revealedState, flaggedState)
    }

    @Test
    fun nothingChangesAfterGameIsLost() {
        val gameState = buildGameState(width = 3, height = 3, minePositions = setOf(0 to 0, 2 to 2))
        val lostState = revealTileUseCase(revealTileUseCase(gameState, x = 1, y = 1), x = 0, y = 0)

        assertEquals(lostState, revealTileUseCase(lostState, x = 2, y = 0))
        assertEquals(lostState, toggleFlagUseCase(lostState, x = 2, y = 0))
    }

    private fun buildGameState(width: Int, height: Int, minePositions: Set<Pair<Int, Int>>) = GameState(
        width = width,
        height = height,
        mineCount = minePositions.size,
        tiles = List(width) { x ->
            List(height) { y ->
                Tile(
                    isMine = x to y in minePositions,
                    adjacentMines = minePositions.count { (mineX, mineY) ->
                        (mineX to mineY) != (x to y) && abs(mineX - x) <= 1 && abs(mineY - y) <= 1
                    },
                )
            }
        },
        status = GameStatus.PLAYING,
    )

}
