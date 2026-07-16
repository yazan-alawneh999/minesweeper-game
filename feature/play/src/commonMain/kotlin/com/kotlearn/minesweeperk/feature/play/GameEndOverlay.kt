package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlearn.minesweeperk.domain.game.GameStatus
import com.kotlearn.minesweeperk.ui.core.LocalPadding
import com.kotlearn.minesweeperk.ui.core.glassContentColor
import com.kotlearn.minesweeperk.ui.core.liquidGlass

/**
 * A dimmed overlay shown when the game is [GameStatus.WON] or [GameStatus.LOST],
 * announcing the result and offering a fresh game. Renders nothing while the
 * game is still [GameStatus.PLAYING].
 */
@Composable
internal fun GameEndOverlay(
    status: GameStatus,
    elapsedSeconds: Int,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status == GameStatus.PLAYING) return

    val won = status == GameStatus.WON
    val padding = LocalPadding.current
    val buttonShape = RoundedCornerShape(percent = 50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            // Swallow taps so the board underneath can't be played while the
            // result is showing (no ripple on the scrim).
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .liquidGlass(shape = RoundedCornerShape(24.dp))
                .padding(padding.large),
        ) {
            Text(text = if (won) "😎" else "😵", fontSize = 48.sp)

            Spacer(modifier = Modifier.height(padding.normal))

            Text(
                text = if (won) "You won!" else "You lost",
                color = glassContentColor,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
            )

            if (won) {
                Spacer(modifier = Modifier.height(padding.small))
                Text(
                    text = "Time: ${elapsedSeconds}s",
                    color = glassContentColor,
                    style = MaterialTheme.typography.subtitle1,
                )
            }

            Spacer(modifier = Modifier.height(padding.large))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .testTag("playAgain")
                    .clip(buttonShape)
                    .liquidGlass(shape = buttonShape, selected = true)
                    .clickable(onClick = onRestart)
                    .padding(horizontal = padding.large, vertical = padding.normal),
            ) {
                Text(
                    text = "Play again",
                    color = glassContentColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}