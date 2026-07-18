package com.kotlearn.minesweeperk.feature.play

/** Formats an elapsed duration in seconds as a clock-style `m:ss` string. */
internal fun formatElapsedTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
