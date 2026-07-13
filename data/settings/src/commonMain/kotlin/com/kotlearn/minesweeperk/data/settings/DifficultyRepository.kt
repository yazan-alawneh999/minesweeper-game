package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface DifficultyRepository {

    val difficulty: Flow<String>

    suspend fun updateDifficulty(difficulty: String)

    data object DifficultyKey : Storage.Key.StringKey("difficulty", "EASY")

}

