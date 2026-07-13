package com.kotlearn.minesweeperk.domain.game

import com.kotlearn.minesweeperk.data.game.HighscoreRepository
import com.kotlearn.minesweeperk.data.settings.UsernameRepository
import kotlinx.coroutines.flow.firstOrNull
import com.kotlearn.minesweeperk.data.game.Highscore as DataHighscore

class AddHighscoreUseCase(
    private val usernameRepository: UsernameRepository,
    private val highscoreRepository: HighscoreRepository,
) {

    suspend operator fun invoke(timeSeconds: Int) {
        val username = usernameRepository.username.firstOrNull().orEmpty().ifBlank { "Anonymous" }
        highscoreRepository.addHighscore(
            DataHighscore(
                username = username,
                timeSeconds = timeSeconds,
            )
        )
    }

}
