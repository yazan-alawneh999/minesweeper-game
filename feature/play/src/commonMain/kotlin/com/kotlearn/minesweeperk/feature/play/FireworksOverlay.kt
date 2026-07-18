package com.kotlearn.minesweeperk.feature.play

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Burst(val cx: Float, val cy: Float, val delay: Float, val color: Color)

/** A looping fireworks animation drawn on a Canvas. Intended to be layered above
 *  the board while the game is won. Purely decorative; no state escapes. */
@Composable
internal fun FireworksOverlay(modifier: Modifier = Modifier) {
    val palette = remember {
        listOf(
            Color(0xFFEB392A), Color(0xFF377E22), Color(0xFF0000F5),
            Color(0xFFFFD23F), Color(0xFFFF7B00), Color(0xFF9B51E0),
        )
    }
    val bursts = remember {
        List(6) {
            Burst(
                cx = Random.nextFloat() * 0.8f + 0.1f,
                cy = Random.nextFloat() * 0.5f + 0.15f,
                delay = Random.nextFloat(),
                color = palette[it % palette.size],
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "fireworks")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "t",
    )
    val sparksPerBurst = 24

    Canvas(modifier = modifier.fillMaxSize()) {
        bursts.forEach { burst ->
            val local = ((t + burst.delay) % 1f)
            val radius = local * size.minDimension * 0.35f
            val alpha = (1f - local)
            val center = Offset(burst.cx * size.width, burst.cy * size.height)
            for (i in 0 until sparksPerBurst) {
                val angle = (i.toFloat() / sparksPerBurst) * (2f * kotlin.math.PI.toFloat())
                val p = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
                drawCircle(
                    color = burst.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = size.minDimension * 0.008f,
                    center = p,
                )
            }
        }
    }
}
