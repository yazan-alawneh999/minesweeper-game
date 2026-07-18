package com.kotlearn.minesweeperk.core.audio

/**
 * Plays short game sound effects. Implementations are per-platform. Callers are
 * responsible for respecting the user's sound-enabled preference.
 */
interface SoundPlayer {
    fun play(sound: GameSound)
}
