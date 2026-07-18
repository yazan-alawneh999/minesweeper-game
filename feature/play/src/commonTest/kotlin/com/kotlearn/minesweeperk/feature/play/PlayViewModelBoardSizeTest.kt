package com.kotlearn.minesweeperk.feature.play

import com.kotlearn.minesweeperk.domain.settings.BoardSize
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayViewModelBoardSizeTest {

    @Test
    fun mine_count_is_dimensions_times_density() {
        val size = BoardSize.of(columns = 10, rows = 16) // 160 tiles
        val mines = mineCountFor(size = size, difficulty = Difficulty.EASY) // 12%
        assertEquals(Difficulty.EASY.mineCountFor(size.tileCount), mines)
        assertEquals(19, mines) // round(160 * 0.12) = 19
    }
}
