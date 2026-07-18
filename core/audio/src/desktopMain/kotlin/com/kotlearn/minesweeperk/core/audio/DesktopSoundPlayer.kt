package com.kotlearn.minesweeperk.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem

@OptIn(ExperimentalResourceApi::class)
internal class DesktopSoundPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val cache = mutableMapOf<GameSound, ByteArray>()

    init {
        scope.launch {
            GameSound.entries.forEach { cache[it] = Res.readBytes("files/${it.fileName}") }
        }
    }

    override fun play(sound: GameSound) {
        val bytes = cache[sound] ?: return
        scope.launch {
            runCatching {
                val stream = AudioSystem.getAudioInputStream(BufferedInputStream(ByteArrayInputStream(bytes)))
                val clip = AudioSystem.getClip()
                clip.open(stream)
                clip.start()
            }
        }
    }
}
