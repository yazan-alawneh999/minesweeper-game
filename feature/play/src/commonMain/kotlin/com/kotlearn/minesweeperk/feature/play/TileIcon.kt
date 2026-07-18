package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import minesweeperk.feature.play.generated.resources.Res
import minesweeperk.feature.play.generated.resources.mine
import org.jetbrains.compose.resources.painterResource

/** Renders the selected flag icon, sized to fill its parent responsively.
 *  Emoji variants scale to the box; vector variants draw as a tinted [Icon]. */
@Composable
internal fun FlagTileIcon(
    icon: FlagIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        EmojiGlyph(emoji = emoji, modifier = modifier)
    } else {
        val vector = when (icon) {
            FlagIcon.MATERIAL_OUTLINED -> Icons.Outlined.Flag
            else -> Icons.Filled.Flag
        }
        Icon(imageVector = vector, contentDescription = null, tint = tint, modifier = modifier.fillMaxSize())
    }
}

/** Renders the selected mine icon, sized to fill its parent responsively. */
@Composable
internal fun MineTileIcon(
    icon: MineIcon,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        EmojiGlyph(emoji = emoji, modifier = modifier)
    } else {
        when (icon) {
            MineIcon.CLASSIC_DOT -> Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
            else -> Icon( // SPIKED — the bundled drawable used before this feature
                painter = painterResource(Res.drawable.mine),
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

/** Draws an emoji glyph scaled to its container so it never crops, at any
 *  screen size. Emoji ignore [Color] tint, so none is applied. */
@Composable
private fun EmojiGlyph(emoji: String, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val side = if (maxWidth < maxHeight) maxWidth else maxHeight
        // Emoji glyphs render slightly larger than their font size; 0.82 keeps a
        // small margin so the glyph stays fully inside the box.
        val fontSize = with(LocalDensity.current) { (side * 0.82f).toSp() }
        Text(text = emoji, fontSize = fontSize, textAlign = TextAlign.Center)
    }
}
