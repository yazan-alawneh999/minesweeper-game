package com.kotlearn.minesweeperk.data.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HighscoresCappingTest {

    @Test
    fun keepsBestScoresPerDifficultyIndependently() {
        val scores =
            (1..12).map { Highscore(username = "easy$it", timeSeconds = it, difficulty = "EASY") } +
                (100..102).map { Highscore(username = "exp$it", timeSeconds = it, difficulty = "EXPERT") }

        val capped = highscoresCappedPerDifficulty(scores, maxPerDifficulty = 10)

        val easyTimes = capped.filter { it.difficulty == "EASY" }.map { it.timeSeconds }.sorted()
        val expertTimes = capped.filter { it.difficulty == "EXPERT" }.map { it.timeSeconds }.sorted()
        // Easy is capped to its 10 fastest...
        assertEquals((1..10).toList(), easyTimes)
        // ...and the slower Expert scores survive rather than being crowded out.
        assertEquals(listOf(100, 101, 102), expertTimes)
    }

    @Test
    fun newBetterScoreReplacesWorstOnSaturatedDifficulty() {
        val existing = (10..19).map { Highscore(username = "p$it", timeSeconds = it, difficulty = "EASY") }
        val newBest = Highscore(username = "new", timeSeconds = 5, difficulty = "EASY")

        val capped = highscoresCappedPerDifficulty(existing + newBest, maxPerDifficulty = 10)
        val easy = capped.filter { it.difficulty == "EASY" }

        assertEquals(10, easy.size)
        assertTrue(easy.any { it.timeSeconds == 5 && it.username == "new" })
        assertFalse(easy.any { it.timeSeconds == 19 }) // the previous worst is dropped
    }
}