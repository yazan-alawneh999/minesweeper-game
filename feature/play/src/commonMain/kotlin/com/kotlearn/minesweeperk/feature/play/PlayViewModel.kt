package com.kotlearn.minesweeperk.feature.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlearn.minesweeperk.core.audio.GameSound
import com.kotlearn.minesweeperk.core.audio.SoundPlayer
import com.kotlearn.minesweeperk.domain.game.AddHighscoreUseCase
import com.kotlearn.minesweeperk.domain.game.CreateGameUseCase
import com.kotlearn.minesweeperk.domain.game.GameState
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.domain.game.RevealTileUseCase
import com.kotlearn.minesweeperk.domain.game.ToggleFlagUseCase
import com.kotlearn.minesweeperk.domain.settings.BoardSize
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.GetBoardSizeAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.GetDifficultyAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.GetIconPreferencesAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.GetSoundEnabledAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.IconPreferences
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PlayViewModel(
    private val createGameUseCase: CreateGameUseCase,
    private val revealTileUseCase: RevealTileUseCase,
    private val toggleFlagUseCase: ToggleFlagUseCase,
    private val addHighscoreUseCase: AddHighscoreUseCase,
    private val getDifficultyAsFlowUseCase: GetDifficultyAsFlowUseCase,
    private val getBoardSizeAsFlowUseCase: GetBoardSizeAsFlowUseCase,
    private val getIconPreferencesAsFlowUseCase: GetIconPreferencesAsFlowUseCase,
    private val getSoundEnabledAsFlowUseCase: GetSoundEnabledAsFlowUseCase,
    private val soundPlayer: SoundPlayer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Null until the persisted difficulty has loaded, so we never flash a
    // default board that is immediately replaced by the real one. Restored
    // from saved state so a difficulty change survives process death without
    // being mistaken for a genuine change (which would reset the board).
    private var difficulty: Difficulty? =
        savedStateHandle.get<String>(KEY_DIFFICULTY)?.let { Difficulty.fromName(it) }

    // Board dimensions now come from the player's saved preference, not screen
    // measurement. Restored from saved state so an in-progress game keeps its
    // own dimensions and is never resized under the player.
    private var boardSize: BoardSize? = savedStateHandle.get<String>(KEY_BOARD_SIZE)?.let {
        val parts = it.split("x")
        parts.getOrNull(0)?.toIntOrNull()?.let { c ->
            parts.getOrNull(1)?.toIntOrNull()?.let { r -> BoardSize.of(c, r) }
        }
    }

    private val _gameState = MutableStateFlow(restoreGameState())
    val gameState = _gameState.asStateFlow()

    val iconPreferences = getIconPreferencesAsFlowUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            IconPreferences(flag = FlagIcon.DEFAULT, mine = MineIcon.DEFAULT),
        )

    private val soundEnabled = getSoundEnabledAsFlowUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _elapsedSeconds = MutableStateFlow(savedStateHandle[KEY_ELAPSED] ?: 0)
    val elapsedSeconds = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null

    init {
        // If a game was restored after process death, keep its dimensions so the
        // board is treated as already measured and is never restarted, and resume
        // the timer if the restored game was mid-play.
        _gameState.value?.let { restored ->
            boardSize = BoardSize.of(columns = restored.width, rows = restored.height)
            if (restored.status == GameStatus.PLAYING && restored.hasStarted()) {
                startTimerIfNeeded()
            }
        }
        viewModelScope.launch {
            // Start a new game on the first load of difficulty + board size, and
            // restart only when the user genuinely changes either — never on a
            // process-death restore that re-emits the same values.
            combine(getDifficultyAsFlowUseCase(), getBoardSizeAsFlowUseCase()) { d, s -> d to s }
                .collect { (newDifficulty, newSize) ->
                    val difficultyChanged = difficulty != null && newDifficulty != difficulty
                    val sizeChanged = boardSize != null && newSize != boardSize
                    difficulty = newDifficulty
                    boardSize = newSize
                    savedStateHandle[KEY_DIFFICULTY] = newDifficulty.name
                    savedStateHandle[KEY_BOARD_SIZE] = "${newSize.columns}x${newSize.rows}"
                    if (_gameState.value == null || difficultyChanged || sizeChanged) {
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
        maybePlayTransitionSound(
            player = soundPlayer,
            enabled = soundEnabled.value,
            from = previousStatus,
            to = newState.status,
        )
        if (newState.status == GameStatus.PLAYING) {
            startTimerIfNeeded()
        } else {
            stopTimer()
        }
        if (previousStatus == GameStatus.PLAYING && newState.status == GameStatus.WON) {
            val difficulty = difficulty ?: return
            viewModelScope.launch {
                addHighscoreUseCase(timeSeconds = _elapsedSeconds.value, difficulty = difficulty)
            }
        }
    }

    fun toggleFlag(x: Int, y: Int) {
        val current = _gameState.value ?: return
        val newState = toggleFlagUseCase(current, x, y)
        if (soundEnabled.value && newState.flagCount != current.flagCount) {
            soundPlayer.play(GameSound.FLAG)
        }
        setGameState(newState)
    }

    fun restart() = startNewGame()

    private fun startNewGame() {
        val difficulty = difficulty ?: return
        val size = boardSize ?: return
        stopTimer()
        setElapsedSeconds(0)
        setGameState(
            createGameUseCase(
                width = size.columns,
                height = size.rows,
                mineCount = mineCountFor(size = size, difficulty = difficulty),
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
        const val KEY_BOARD_SIZE = "boardSize"
    }

}

/** Number of mines for [size] at [difficulty]'s density. */
internal fun mineCountFor(size: BoardSize, difficulty: Difficulty): Int =
    difficulty.mineCountFor(tileCount = size.tileCount)

/** Plays the sound for a status transition, if any, respecting [enabled]. */
internal fun maybePlayTransitionSound(
    player: SoundPlayer,
    enabled: Boolean,
    from: GameStatus,
    to: GameStatus,
) {
    if (!enabled || from == to) return
    when (to) {
        GameStatus.LOST -> player.play(GameSound.EXPLOSION)
        GameStatus.WON -> player.play(GameSound.WIN)
        GameStatus.PLAYING -> Unit
    }
}