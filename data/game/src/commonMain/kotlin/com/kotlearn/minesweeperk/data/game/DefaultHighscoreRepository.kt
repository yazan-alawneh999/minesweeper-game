package com.kotlearn.minesweeperk.data.game

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class DefaultHighscoreRepository(
    private val storage: Storage,
) : HighscoreRepository {

    override val highscores: Flow<List<Highscore>> = storage.getAsFlow(HighscoreRepository.HighscoresKey)
        .map { json -> json?.let { decode(it) }.orEmpty() }

    override suspend fun addHighscore(highscore: Highscore) {
        val currentHighscores = storage.get(HighscoreRepository.HighscoresKey)?.let { decode(it) }.orEmpty()
        val updatedHighscores = highscoresCappedPerDifficulty(
            scores = currentHighscores + highscore,
            maxPerDifficulty = MAX_HIGHSCORES_PER_DIFFICULTY,
        )
        storage.writeValue(HighscoreRepository.HighscoresKey, Json.encodeToString(updatedHighscores))
    }

    private fun decode(json: String): List<Highscore> = try {
        Json.decodeFromString(json)
    } catch (e: Exception) {
        emptyList()
    }

    companion object {
        private const val MAX_HIGHSCORES_PER_DIFFICULTY = 10
    }

}

/**
 * Keeps only the [maxPerDifficulty] fastest scores for each difficulty, so a
 * great time on a hard level is never crowded out by faster times from easier
 * levels. Scores are compared within their own difficulty group.
 */
internal fun highscoresCappedPerDifficulty(
    scores: List<Highscore>,
    maxPerDifficulty: Int,
): List<Highscore> = scores
    .groupBy { it.difficulty }
    .flatMap { (_, difficultyScores) ->
        difficultyScores.sortedBy { it.timeSeconds }.take(maxPerDifficulty)
    }
