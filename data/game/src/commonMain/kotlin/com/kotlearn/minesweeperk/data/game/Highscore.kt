package com.kotlearn.minesweeperk.data.game

import kotlinx.serialization.Serializable

@Serializable
data class Highscore(
    val username: String,
    val timeSeconds: Int,
    // The difficulty name (e.g. "EASY") the score was achieved on. Defaults to
    // empty so highscores persisted before per-difficulty support still decode.
    val difficulty: String = "",
)
