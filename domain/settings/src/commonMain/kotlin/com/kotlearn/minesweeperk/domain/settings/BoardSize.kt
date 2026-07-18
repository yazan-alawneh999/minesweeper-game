package com.kotlearn.minesweeperk.domain.settings

/**
 * Explicit board dimensions chosen by the player. Construct via [of], which
 * clamps to [MIN_SIDE]..[MAX_SIDE] so a corrupt/persisted value can never
 * produce an unplayable board.
 */
data class BoardSize private constructor(
    val columns: Int,
    val rows: Int,
) {
    val tileCount: Int get() = columns * rows

    companion object {
        const val MIN_SIDE = 5
        const val MAX_SIDE = 30
        val DEFAULT = of(columns = 10, rows = 16)

        fun of(columns: Int, rows: Int) = BoardSize(
            columns = columns.coerceIn(MIN_SIDE, MAX_SIDE),
            rows = rows.coerceIn(MIN_SIDE, MAX_SIDE),
        )
    }
}
