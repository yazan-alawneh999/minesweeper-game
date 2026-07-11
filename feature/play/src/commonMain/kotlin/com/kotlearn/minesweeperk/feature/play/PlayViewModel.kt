package com.kotlearn.minesweeperk.feature.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.game.AddHighscoreUseCase
import com.kotlearn.minesweeperk.domain.game.CreateGameUseCase
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.domain.game.RevealTileUseCase
import com.kotlearn.minesweeperk.domain.game.ToggleFlagUseCase
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import com.kotlearn.minesweeperk.domain.settings.GetDifficultyAsFlowUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class PlayViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val revealTileUseCase: RevealTileUseCase,
    private val toggleFlagUseCase: ToggleFlagUseCase,
    private val addHighscoreUseCase: AddHighscoreUseCase,
    private val getDifficultyAsFlowUseCase: GetDifficultyAsFlowUseCase,
) : ViewModel() {

    private var difficulty: Difficulty = Difficulty.DEFAULT

    private val _gameState = MutableStateFlow(createNewGame())
    val gameState = _gameState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            getDifficultyAsFlowUseCase().collect { newDifficulty ->
                if (newDifficulty != difficulty) {
                    difficulty = newDifficulty
                    restart()
                }
            }
        }
    }

    fun revealTile(x: Int, y: Int) {
        val previousStatus = _gameState.value.status
        _gameState.value = revealTileUseCase(_gameState.value, x, y)
        if (_gameState.value.status == GameStatus.PLAYING) {
            startTimerIfNeeded()
        } else {
            stopTimer()
        }
        if (previousStatus == GameStatus.PLAYING && _gameState.value.status == GameStatus.WON) {
            viewModelScope.launch {
                addHighscoreUseCase(timeSeconds = _elapsedSeconds.value)
            }
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
        width = difficulty.width,
        height = difficulty.height,
        mineCount = difficulty.mineCount,
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


}
