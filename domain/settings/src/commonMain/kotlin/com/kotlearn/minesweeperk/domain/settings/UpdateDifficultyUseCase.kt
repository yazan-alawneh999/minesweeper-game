package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.DifficultyRepository

class UpdateDifficultyUseCase(
    private val difficultyRepository: DifficultyRepository,
) {

    suspend operator fun invoke(difficulty: Difficulty) {
        difficultyRepository.updateDifficulty(difficulty.name)
    }

}

