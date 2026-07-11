package com.kotlearn.minesweeperk.feature.highscores

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlearn.minesweeperk.domain.game.Highscore
import com.kotlearn.minesweeperk.ui.core.LocalDimensions
import com.kotlearn.minesweeperk.ui.core.LocalPadding
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun HighscoresScreen(
    viewModel: HighscoresViewModel,
    modifier: Modifier = Modifier
) {
    val highscores by viewModel.highscores.collectAsStateWithLifecycle()

    HighscoresContent(
        highscores = highscores,
        modifier = modifier,
    )
}

@Composable
private fun HighscoresContent(
    highscores: List<Highscore>,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.background(GlassTheme.backgroundGradient)) {

        LiquidBlobs()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(LocalPadding.current.normal)
        ) {

            Text(
                text = "🏆 Highscores",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.h4,
                modifier = Modifier.padding(vertical = LocalPadding.current.large)
            )

            if (highscores.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(LocalPadding.current.small),
                    modifier = Modifier
                        .widthIn(max = LocalDimensions.current.maxWidthSmall * 1.5f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(highscores) { index, highscore ->
                        HighscoreRow(
                            rank = index + 1,
                            highscore = highscore,
                        )
                    }
                }
            }

        }
    }
}

@Composable
private fun HighscoreRow(
    rank: Int,
    highscore: Highscore,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(20.dp))
            .padding(
                horizontal = LocalPadding.current.normal,
                vertical = LocalPadding.current.small,
            )
    ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(LocalDimensions.current.viewTiny)
                .glass(shape = CircleShape)
        ) {
            Text(
                text = when (rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> rank.toString()
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
            )
        }

        Spacer(modifier = Modifier.width(LocalPadding.current.normal))

        Text(
            text = highscore.username,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = formatTime(highscore.timeSeconds),
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.subtitle1,
        )

    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .widthIn(max = LocalDimensions.current.maxWidthSmall * 1.5f)
            .fillMaxWidth()
            .glass(shape = RoundedCornerShape(28.dp))
            .padding(LocalPadding.current.large)
    ) {
        Text(
            text = "💣",
            fontSize = 48.sp,
        )
        Text(
            text = "No highscores yet",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(top = LocalPadding.current.small)
        )
        Text(
            text = "Win a game to claim the top spot!",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = LocalPadding.current.tiny)
        )
    }
}

@Composable
private fun LiquidBlobs() {
    Box(
        modifier = Modifier
            .size(320.dp)
            .offset(x = (-100).dp, y = (-60).dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(GlassTheme.blobPurple, Color.Transparent),
                ),
                shape = CircleShape,
            )
    )
    Box(
        modifier = Modifier
            .size(280.dp)
            .offset(x = 220.dp, y = 380.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(GlassTheme.blobTeal, Color.Transparent),
                ),
                shape = CircleShape,
            )
    )
    Box(
        modifier = Modifier
            .size(260.dp)
            .offset(x = (-40).dp, y = 620.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(GlassTheme.blobPink, Color.Transparent),
                ),
                shape = CircleShape,
            )
    )
}

private fun Modifier.glass(shape: Shape): Modifier = this
    .clip(shape)
    .background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.06f),
            ),
        ),
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.55f),
                Color.White.copy(alpha = 0.10f),
            ),
        ),
        shape = shape,
    )

private object GlassTheme {

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1B1035),
            Color(0xFF2C1B4D),
            Color(0xFF13294B),
        ),
    )

    val blobPurple = Color(0xFF7C4DFF).copy(alpha = 0.55f)
    val blobTeal = Color(0xFF00BFA5).copy(alpha = 0.40f)
    val blobPink = Color(0xFFFF4081).copy(alpha = 0.35f)

}

private fun formatTime(timeSeconds: Int): String {
    val minutes = timeSeconds / 60
    val seconds = timeSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Preview
@Composable
fun HighscoresContentPreview() {
    HighscoresContent(
        highscores = listOf(
            Highscore(username = "Yazan", timeSeconds = 42),
            Highscore(username = "Anonymous", timeSeconds = 61),
            Highscore(username = "Player 3", timeSeconds = 95),
            Highscore(username = "Player 4", timeSeconds = 128),
        ),
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview
@Composable
fun HighscoresContentEmptyPreview() {
    HighscoresContent(
        highscores = emptyList(),
        modifier = Modifier.fillMaxSize(),
    )
}
