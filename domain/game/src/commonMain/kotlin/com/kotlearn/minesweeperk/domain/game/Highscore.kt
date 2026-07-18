package com.kotlearn.minesweeperk.domain.game

import com.kotlearn.minesweeperk.domain.settings.Difficulty

data class Highscore(
    val username: String,
    val timeSeconds: Int,
    val difficulty: Difficulty,
)
