package com.kotlearn.minesweeperk.data.game

import kotlinx.serialization.Serializable

@Serializable
data class Highscore(
    val username: String,
    val timeSeconds: Int,
)
