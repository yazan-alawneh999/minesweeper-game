package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlearn.minesweeperk.domain.game.GameState
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.ui.core.LocalPadding

@Composable
internal fun PlayScreen(
    viewModel: PlayViewModel,
    modifier: Modifier = Modifier,
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(LocalPadding.current.normal)
        ) {
            CounterText(value = gameState.mineCount - gameState.flagCount)
            Button(
                onClick = {
                    viewModel.restart()
                }
            ) {
                Text(
                    text = when (gameState.status) {
                        GameStatus.PLAYING -> "🙂"
                        GameStatus.WON -> "😎"
                        GameStatus.LOST -> "😵"
                    }
                )
            }
            CounterText(value = elapsedSeconds)
        }

        MinesweeperBoard(
            tileStates = gameState.toTileStates(),
            onTileClick = viewModel::revealTile,
            onTileLongClick = viewModel::toggleFlag,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    start = LocalPadding.current.normal,
                    end = LocalPadding.current.normal,
                    bottom = LocalPadding.current.normal,
                )
        )
    }
}

@Composable
private fun CounterText(value: Int) {
    Text(
        text = value.coerceIn(-99, 999).toString().padStart(3, '0'),
        color = LocalMinesweeperBoardColorScheme.current.timerText,
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
