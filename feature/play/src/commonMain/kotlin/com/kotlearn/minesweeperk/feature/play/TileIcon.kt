package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import minesweeperk.feature.play.generated.resources.Res
import minesweeperk.feature.play.generated.resources.mine
import org.jetbrains.compose.resources.painterResource

/** Renders the selected flag icon, filling its parent. Emoji variants draw as
 *  text (sized via [emojiSize]); vector variants draw as a tinted [Icon]. */
@Composable
internal fun FlagTileIcon(
    icon: FlagIcon,
    tint: Color,
    emojiSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        Text(text = emoji, fontSize = emojiSize, textAlign = TextAlign.Center, modifier = modifier)
    } else {
        val vector = when (icon) {
            FlagIcon.MATERIAL_OUTLINED -> Icons.Outlined.Flag
            else -> Icons.Filled.Flag
        }
        Icon(imageVector = vector, contentDescription = null, tint = tint, modifier = modifier.fillMaxSize())
    }
}

/** Renders the selected mine icon. Emoji variants draw as text; vector variants
 *  draw the bundled `mine` drawable (SPIKED) or a filled dot (CLASSIC_DOT). */
@Composable
internal fun MineTileIcon(
    icon: MineIcon,
    tint: Color,
    emojiSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        Text(text = emoji, fontSize = emojiSize, textAlign = TextAlign.Center, modifier = modifier)
    } else {
        when (icon) {
            MineIcon.CLASSIC_DOT -> Icon(
                painter = painterResource(Res.drawable.mine),
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
            else -> Icon( // SPIKED — the existing drawable
                painter = painterResource(Res.drawable.mine),
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}
