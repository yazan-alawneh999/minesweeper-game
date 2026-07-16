package com.kotlearn.minesweeperk.feature.play

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PlayViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val revealTileUseCase: RevealTileUseCase,
    private val toggleFlagUseCase: ToggleFlagUseCase,
    private val addHighscoreUseCase: AddHighscoreUseCase,
    private val getDifficultyAsFlowUseCase: GetDifficultyAsFlowUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Null until the persisted difficulty has loaded, so we never flash a
    // default board that is immediately replaced by the real one. Restored
    // from saved state so a difficulty change survives process death without
    // being mistaken for a genuine change (which would reset the board).
    private var difficulty: Difficulty? =
        savedStateHandle.get<String>(KEY_DIFFICULTY)?.let { Difficulty.fromName(it) }

    // Board dimensions are decided by the *first* measurement and then locked in
    // for the life of the game, so rotating or resizing the window keeps the
    // same board instead of resetting an in-progress game. Null until measured
    // (or restored from a saved game).
    private var columns: Int? = null
    private var rows: Int? = null

    private val _gameState = MutableStateFlow(restoreGameState())
    val gameState = _gameState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(savedStateHandle[KEY_ELAPSED] ?: 0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        // If a game was restored after process death, keep its dimensions so the
        // board is treated as already measured and is never restarted, and resume
        // the timer if the restored game was mid-play.
        _gameState.value?.let { restored ->
            columns = restored.width
            rows = restored.height
            if (restored.status == GameStatus.PLAYING && restored.hasStarted()) {
                startTimerIfNeeded()
            }
        }
        viewModelScope.launch {
            getDifficultyAsFlowUseCase().collect { newDifficulty ->
                // Start a new game on the very first difficulty load, and restart
                // only when the user genuinely changes difficulty — never on a
                // process-death restore that re-emits the same value.
                val difficultyChanged = difficulty != null && newDifficulty != difficulty
                difficulty = newDifficulty
                savedStateHandle[KEY_DIFFICULTY] = newDifficulty.name
                if (_gameState.value == null || difficultyChanged) {
                    startNewGame()
                }
            }
        }
    }

    fun revealTile(x: Int, y: Int) {
        val current = _gameState.value ?: return
        val previousStatus = current.status
        val newState = revealTileUseCase(current, x, y)
        setGameState(newState)
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
        setGameState(toggleFlagUseCase(current, x, y))
    }

    /**
     * Reports how many tiles fit on screen. Only the first report sizes the
     * board; later reports (e.g. from a rotation) are ignored so an in-progress
     * game is never reset just because the available space changed.
     */
    fun onBoardMeasured(columns: Int, rows: Int) {
        if (this.columns != null && this.rows != null) return
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
        setElapsedSeconds(0)
        setGameState(
            createGameUseCase(
                width = columns,
                height = rows,
                mineCount = difficulty.mineCountFor(tileCount = columns * rows),
            )
        )
    }

    private fun startTimerIfNeeded() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                setElapsedSeconds(_elapsedSeconds.value + 1)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun setGameState(state: GameState?) {
        _gameState.value = state
        savedStateHandle[KEY_GAME] = state?.let { Json.encodeToString(it) }
    }

    private fun setElapsedSeconds(value: Int) {
        _elapsedSeconds.value = value
        savedStateHandle[KEY_ELAPSED] = value
    }

    private fun restoreGameState(): GameState? =
        savedStateHandle.get<String>(KEY_GAME)?.let { Json.decodeFromString(it) }

    private fun GameState.hasStarted(): Boolean =
        tiles.any { column -> column.any { it.isRevealed } }

    private companion object {
        const val KEY_GAME = "game"
        const val KEY_ELAPSED = "elapsedSeconds"
        const val KEY_DIFFICULTY = "difficulty"
    }

}