package com.kotlearn.minesweeperk.data.game

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface HighscoreRepository {

    val highscores: Flow<List<Highscore>>

    suspend fun addHighscore(highscore: Highscore)

    data object HighscoresKey : Storage.Key.StringKey("highscores", null)

}
