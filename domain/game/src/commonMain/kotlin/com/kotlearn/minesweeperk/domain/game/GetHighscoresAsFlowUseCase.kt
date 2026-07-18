package com.kotlearn.minesweeperk.domain.game

import com.kotlearn.minesweeperk.data.game.HighscoreRepository
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHighscoresAsFlowUseCase(
    private val highscoreRepository: HighscoreRepository,
) {

    operator fun invoke(): Flow<List<Highscore>> = highscoreRepository.highscores
        .map { highscores ->
            highscores
                .sortedBy { it.timeSeconds }
                .map {
                    Highscore(
                        username = it.username,
                        timeSeconds = it.timeSeconds,
                        difficulty = Difficulty.fromName(it.difficulty),
                    )
                }
        }

}
