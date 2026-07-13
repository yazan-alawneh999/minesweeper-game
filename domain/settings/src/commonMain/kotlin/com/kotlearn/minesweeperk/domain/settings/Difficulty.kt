package com.kotlearn.minesweeperk.domain.settings

import kotlin.math.roundToInt

/**
 * Difficulty levels for a Minesweeper game.
 *
 * The board is sized to fill the available screen, so difficulty is expressed
 * as a mine *density* (the fraction of tiles that are mines) rather than a
 * fixed board size and mine count.
 */
enum class Difficulty(
    val label: String,
    val mineDensity: Float,
) {
    EASY(label = "Easy", mineDensity = 0.12f),
    HARD(label = "Hard", mineDensity = 0.18f),
    EXPERT(label = "Expert", mineDensity = 0.22f);

    /** Mine density as a whole-number percentage, for display. */
    val minePercent: Int
        get() = (mineDensity * 100).roundToInt()

    /** Number of mines for a board with the given tile count. */
    fun mineCountFor(tileCount: Int): Int =
        (tileCount * mineDensity).roundToInt().coerceIn(1, (tileCount - 1).coerceAtLeast(1))

    companion object {
        val DEFAULT = EASY

        /** Parses a persisted [Difficulty] name, falling back to [DEFAULT] when unknown. */
        fun fromName(name: String?): Difficulty =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
