package com.kotlearn.minesweeperk.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired "Liquid Glass" styling helpers.
 *
 * The effect is achieved without a platform backdrop-blur (keeping it fully
 * multiplatform) by layering translucent gradients with a bright, angled
 * highlight border so the surface reads as a piece of frosted glass floating
 * over the background.
 */
object LiquidGlass {

    /** A colorful gradient backdrop that lets the glass surfaces stand out. */
    val backgroundBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFF1B2A4A),
                Color(0xFF2E1D4D),
                Color(0xFF123047),
            ),
        )

    /** Accent color used to highlight the selected element. */
    val accent = Color(0xFF6EA8FF)
}

/**
 * Applies a translucent frosted-glass surface with a rounded shape and an
 * angled light-to-dark highlight border.
 *
 * @param selected when true, the surface is tinted with the accent color and
 * gets a brighter border to signal selection.
 */
fun Modifier.liquidGlass(
    shape: Shape,
    selected: Boolean = false,
    borderWidth: Dp = 1.dp,
): Modifier {
    val baseTint = if (selected) LiquidGlass.accent else Color.White
    val fillBrush = Brush.linearGradient(
        colors = listOf(
            baseTint.copy(alpha = if (selected) 0.45f else 0.28f),
            baseTint.copy(alpha = if (selected) 0.22f else 0.10f),
            baseTint.copy(alpha = if (selected) 0.35f else 0.18f),
        ),
    )
    val borderBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (selected) 0.9f else 0.65f),
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = if (selected) 0.55f else 0.35f),
        ),
    )
    return this
        .clip(shape)
        .background(brush = fillBrush, shape = shape)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

/** Convenience readable text color that sits well on top of glass surfaces. */
internal val glassContentColor = Color.White.compositeOver(Color(0xFFEAF2FF))

