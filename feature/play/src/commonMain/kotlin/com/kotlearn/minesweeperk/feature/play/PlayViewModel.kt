package com.kotlearn.minesweeperk.feature.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.domain.game.AddHighscoreUseCase
import com.kotlearn.minesweeperk.domain.game.CreateGameUseCase
import com.kotlearn.minesweeperk.domain.game.GameState
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

    // Null until the persisted difficulty has loaded, so we never flash a
    // default board that is immediately replaced by the real one.
    private var difficulty: Difficulty? = null

    // Board dimensions are derived from the available screen space and reported
    // by the UI via [onBoardMeasured]; null until the board has been measured.
    private var columns: Int? = null
    private var rows: Int? = null

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState = _gameState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            getDifficultyAsFlowUseCase().collect { newDifficulty ->
                if (newDifficulty != difficulty) {
                    difficulty = newDifficulty
                    startNewGame()
                }
            }
        }
    }

    fun revealTile(x: Int, y: Int) {
        val current = _gameState.value ?: return
        val previousStatus = current.status
        val newState = revealTileUseCase(current, x, y)
        _gameState.value = newState
        if (newState.status == GameStatus.PLAYING) {
            startTimerIfNeeded()
        } else {
            stopTimer()
        }
        if (previousStatus == GameStatus.PLAYING && newState.status == GameStatus.WON) {
            viewModelScope.launch {
                addHighscoreUseCase(timeSeconds = _elapsedSeconds.value)
            }
        }
    }

    fun toggleFlag(x: Int, y: Int) {
        val current = _gameState.value ?: return
        _gameState.value = toggleFlagUseCase(current, x, y)
    }

    /**
     * Reports how many tiles fit on screen. Starts (or restarts) the game when
     * the dimensions change so the board always fills the available space.
     */
    fun onBoardMeasured(columns: Int, rows: Int) {
        if (columns == this.columns && rows == this.rows) return
        this.columns = columns
        this.rows = rows
        startNewGame()
    }

    fun restart() = startNewGame()

    private fun startNewGame() {
        val difficulty = difficulty ?: return
        val columns = columns ?: return
        val rows = rows ?: return
        stopTimer()
        _elapsedSeconds.value = 0
        _gameState.value = createGameUseCase(
            width = columns,
            height = rows,
            mineCount = difficulty.mineCountFor(tileCount = columns * rows),
        )
    }

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
