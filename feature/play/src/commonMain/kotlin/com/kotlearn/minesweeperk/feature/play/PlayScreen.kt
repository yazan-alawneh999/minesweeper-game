package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlearn.minesweeperk.domain.game.GameState
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.ui.core.LiquidGlass
import com.kotlearn.minesweeperk.ui.core.LocalPadding
import com.kotlearn.minesweeperk.ui.core.LocalSystemPaddingValue
import com.kotlearn.minesweeperk.ui.core.glassContentColor
import com.kotlearn.minesweeperk.ui.core.liquidGlass

@Composable
internal fun PlayScreen(
    viewModel: PlayViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val iconPreferences by viewModel.iconPreferences.collectAsStateWithLifecycle()
    val boardSize by viewModel.boardSizeState.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabledState.collectAsStateWithLifecycle()
    var showPanel by rememberSaveable { mutableStateOf(false) }

    val padding = LocalPadding.current

    Box(modifier = modifier.background(LiquidGlass.backgroundBrush)) {
        // The board is only drawn once the game exists (after the difficulty and
        // board-size preferences have loaded), but the layout is always present
        // so nothing jumps when it appears.
        val state = gameState
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .safeDrawingPadding()
                .padding(padding.normal),
        ) {
            PlayToolbar(
                onNavigateBack = onNavigateBack,
                onOpenSettings = { showPanel = true },
            )

            Spacer(modifier = Modifier.height(padding.normal))

            ScoreBar(
                minesRemaining = state?.let { it.mineCount - it.flagCount } ?: 0,
                elapsedSeconds = elapsedSeconds,
                status = state?.status ?: GameStatus.PLAYING,
                onRestart = viewModel::restart,
            )

            Spacer(modifier = Modifier.height(padding.large))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .liquidGlass(shape = RoundedCornerShape(24.dp))
                    .padding(padding.normal),
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    // Board dimensions come from the saved BoardSize preference;
                    // the board scales to fit this area.
                    if (state != null) {
                        MinesweeperBoard(
                            tileStates = state.toTileStates(),
                            onTileClick = viewModel::revealTile,
                            onTileLongClick = viewModel::toggleFlag,
                            flagIcon = iconPreferences.flag,
                            mineIcon = iconPreferences.mine,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // Only celebrate a win. A loss just reveals the board (with the
                // explosion) and the player restarts via the score-bar button —
                // no blocking "You lost" dialog.
                if (state != null && state.status == GameStatus.WON) {
                    FireworksOverlay(modifier = Modifier.fillMaxSize())
                    GameEndOverlay(
                        status = state.status,
                        elapsedSeconds = elapsedSeconds,
                        onRestart = viewModel::restart,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        if (showPanel) {
            QuickSettingsPanel(
                boardSize = boardSize,
                flagIcon = iconPreferences.flag,
                mineIcon = iconPreferences.mine,
                soundEnabled = soundEnabled,
                onBoardSizeChange = viewModel::setBoardSize,
                onFlagIconChange = viewModel::setFlagIcon,
                onMineIconChange = viewModel::setMineIcon,
                onSoundEnabledChange = viewModel::setSoundEnabled,
                onReset = viewModel::resetToDefaults,
                onDismiss = { showPanel = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun PlayToolbar(
    onNavigateBack: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val padding = LocalPadding.current
    val shape = RoundedCornerShape(percent = 50)
    Column {
        Spacer(modifier = Modifier.height(LocalSystemPaddingValue.current.calculateTopPadding()))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .liquidGlass(shape = shape)
                    .clickable(onClick = onNavigateBack),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = glassContentColor,
                )
            }
            Spacer(modifier = Modifier.width(padding.normal))
            Text(
                text = "Minesweeper",
                color = glassContentColor,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .liquidGlass(shape = shape)
                    .clickable(onClick = onOpenSettings),
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Customize",
                    tint = glassContentColor,
                )
            }
        }
    }
}

@Composable
private fun ScoreBar(
    minesRemaining: Int,
    elapsedSeconds: Int,
    status: GameStatus,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(24.dp))
            .padding(horizontal = padding.large, vertical = padding.normal),
    ) {
        CounterText(value = minesRemaining)
        RestartButton(status = status, onClick = onRestart)
        TimerText(elapsedSeconds = elapsedSeconds)
    }
}

@Composable
private fun TimerText(elapsedSeconds: Int) {
    Text(
        text = formatElapsedTime(elapsedSeconds),
        color = LocalMinesweeperBoardColorScheme.current.timerText,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.h5,
    )
}

@Composable
private fun RestartButton(
    status: GameStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .liquidGlass(shape = shape, selected = true)
            .clickable(onClick = onClick),
    ) {
        Text(
            text = when (status) {
                GameStatus.PLAYING -> "🙂"
                GameStatus.WON -> "😎"
                GameStatus.LOST -> "😵"
            },
            fontSize = 24.sp,
        )
    }
}

@Composable
private fun CounterText(value: Int) {
    Text(
        text = value.coerceIn(-99, 999).toString().padStart(3, '0'),
        color = LocalMinesweeperBoardColorScheme.current.timerText,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.h5,
    )
}

internal fun GameState.toTileStates(): List<List<TileState>> = tiles.map { column ->
    column.map { tile ->
        if (tile.isRevealed) {
            if (tile.isMine) {
                TileState.Revealed.Mine
            } else {
                TileState.Revealed.Number(value = tile.adjacentMines.takeIf { it > 0 })
            }
        } else {
            TileState.Hidden(flagged = tile.isFlagged)
        }
    }
}
