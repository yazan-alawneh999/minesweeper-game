package com.kotlearn.minesweeperk.feature.play

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.kotlearn.minesweeperk.domain.game.GameStatus
import kotlin.test.Test
import kotlin.test.assertTrue

class GameEndOverlayTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun showsWinMessageAndTimeWhenWon() = runComposeUiTest {
        setContent {
            GameEndOverlay(status = GameStatus.WON, elapsedSeconds = 42, onRestart = {})
        }

        onAllNodes(hasText("You won!")).assertCountEquals(1)
        onAllNodes(hasText("42", substring = true)).assertCountEquals(1)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun showsLoseMessageWhenLost() = runComposeUiTest {
        setContent {
            GameEndOverlay(status = GameStatus.LOST, elapsedSeconds = 10, onRestart = {})
        }

        onAllNodes(hasText("You lost")).assertCountEquals(1)
        onAllNodes(hasText("You won!")).assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun isHiddenWhilePlaying() = runComposeUiTest {
        setContent {
            GameEndOverlay(status = GameStatus.PLAYING, elapsedSeconds = 5, onRestart = {})
        }

        onAllNodes(hasText("You won!")).assertCountEquals(0)
        onAllNodes(hasText("You lost")).assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingPlayAgainInvokesRestart() = runComposeUiTest {
        var restarted = false
        setContent {
            GameEndOverlay(status = GameStatus.WON, elapsedSeconds = 1, onRestart = { restarted = true })
        }

        onNodeWithTag("playAgain").performClick()

        assertTrue(restarted)
    }
}
