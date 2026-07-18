package com.kotlearn.minesweeperk.feature.play

import com.kotlearn.minesweeperk.core.audio.GameSound
import com.kotlearn.minesweeperk.core.audio.SoundPlayer
import com.kotlearn.minesweeperk.domain.game.GameStatus
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSoundPlayer : SoundPlayer {
    val played = mutableListOf<GameSound>()
    override fun play(sound: GameSound) { played += sound }
}

class PlayViewModelSoundTest {

    @Test
    fun plays_explosion_on_transition_to_lost_when_enabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.LOST)
        assertEquals(listOf(GameSound.EXPLOSION), player.played)
    }

    @Test
    fun plays_win_on_transition_to_won_when_enabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.WON)
        assertEquals(listOf(GameSound.WIN), player.played)
    }

    @Test
    fun plays_nothing_when_disabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = false, from = GameStatus.PLAYING, to = GameStatus.WON)
        assertEquals(emptyList(), player.played)
    }

    @Test
    fun plays_nothing_when_status_unchanged() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.PLAYING)
        assertEquals(emptyList(), player.played)
    }
}
