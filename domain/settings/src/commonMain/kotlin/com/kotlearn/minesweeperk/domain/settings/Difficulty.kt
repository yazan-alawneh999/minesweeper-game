package com.kotlearn.minesweeperk.domain.settings

/**
 * Difficulty levels for a Minesweeper game, each mapping to a concrete board
 * configuration (classic Minesweeper presets).
 */
enum class Difficulty(
    val label: String,
    val width: Int,
    val height: Int,
    val mineCount: Int,
) {
    EASY(label = "Easy", width = 9, height = 9, mineCount = 10),
    HARD(label = "Hard", width = 16, height = 16, mineCount = 40),
    EXPERT(label = "Expert", width = 30, height = 16, mineCount = 99);

    companion object {
        val DEFAULT = EASY

        /** Parses a persisted [Difficulty] name, falling back to [DEFAULT] when unknown. */
        fun fromName(name: String?): Difficulty =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}

