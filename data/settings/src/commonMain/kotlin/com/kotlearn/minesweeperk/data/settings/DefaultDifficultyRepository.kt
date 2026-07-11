package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultDifficultyRepository(
    private val storage: Storage,
) : DifficultyRepository {

    override val difficulty: Flow<String> = storage.getAsFlow(DifficultyRepository.DifficultyKey)
        .map { it ?: DifficultyRepository.DifficultyKey.defaultValue.orEmpty() }

    override suspend fun updateDifficulty(difficulty: String) {
        storage.writeValue(DifficultyRepository.DifficultyKey, difficulty)
    }
}

