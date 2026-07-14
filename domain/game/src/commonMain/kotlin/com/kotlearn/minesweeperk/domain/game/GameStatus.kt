package com.kotlearn.minesweeperk.domain.game

import kotlinx.serialization.Serializable

@Serializable
enum class GameStatus {
    PLAYING,
    WON,
    LOST,
}
