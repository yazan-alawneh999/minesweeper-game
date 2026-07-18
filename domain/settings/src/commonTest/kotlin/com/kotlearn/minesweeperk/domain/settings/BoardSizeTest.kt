package com.kotlearn.minesweeperk.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class BoardSizeTest {

    @Test
    fun coerces_columns_and_rows_into_bounds() {
        assertEquals(BoardSize.MIN_SIDE, BoardSize.of(columns = 1, rows = 1).columns)
        assertEquals(BoardSize.MAX_SIDE, BoardSize.of(columns = 999, rows = 999).rows)
    }

    @Test
    fun tile_count_is_columns_times_rows() {
        assertEquals(150, BoardSize.of(columns = 10, rows = 15).tileCount)
    }

    @Test
    fun default_is_ten_by_sixteen() {
        assertEquals(10, BoardSize.DEFAULT.columns)
        assertEquals(16, BoardSize.DEFAULT.rows)
    }
}
