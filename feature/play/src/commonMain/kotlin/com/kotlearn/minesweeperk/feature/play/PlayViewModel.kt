package com.kotlearn.minesweeperk.feature.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.game.CreateGameUseCase
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.domain.game.RevealTileUseCase
import com.kotlearn.minesweeperk.domain.game.ToggleFlagUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val revealTileUseCase: RevealTileUseCase,
    private val toggleFlagUseCase: ToggleFlagUseCase,
) : ViewModel() {

    private val _gameState = MutableStateFlow(createNewGame())
    val gameState = _gameState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun revealTile(x: Int, y: Int) {
        _gameState.value = revealTileUseCase(_gameState.value, x, y)
        if (_gameState.value.status == GameStatus.PLAYING) {
            startTimerIfNeeded()
        } else {
            stopTimer()
        }
    }

    fun toggleFlag(x: Int, y: Int) {
        _gameState.value = toggleFlagUseCase(_gameState.value, x, y)
    }

    fun restart() {
        stopTimer()
        _elapsedSeconds.value = 0
        _gameState.value = createNewGame()
    }

    private fun createNewGame() = createGameUseCase(
        width = BOARD_WIDTH,
        height = BOARD_HEIGHT,
        mineCount = MINE_COUNT,
    )

    private fun startTimerIfNeeded() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _elapsedSeconds.value += 1
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    companion object {
        private const val BOARD_WIDTH = 10
        private const val BOARD_HEIGHT = 16
        private const val MINE_COUNT = 25
    }

}
