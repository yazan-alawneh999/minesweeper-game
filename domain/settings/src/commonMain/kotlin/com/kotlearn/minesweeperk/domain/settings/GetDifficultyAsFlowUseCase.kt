package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.DifficultyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDifficultyAsFlowUseCase(
    private val difficultyRepository: DifficultyRepository,
) {

    operator fun invoke(): Flow<Difficulty> = difficultyRepository.difficulty
        .map { Difficulty.fromName(it) }

}

