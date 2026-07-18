package com.kotlearn.minesweeperk.core.audio

/** A short game sound effect, backed by a bundled audio file. */
enum class GameSound {
    EXPLOSION,
    WIN,
    FLAG;

    val fileName: String
        get() = when (this) {
            EXPLOSION -> "explosion.wav"
            WIN -> "win.wav"
            FLAG -> "flag.wav"
        }
}
