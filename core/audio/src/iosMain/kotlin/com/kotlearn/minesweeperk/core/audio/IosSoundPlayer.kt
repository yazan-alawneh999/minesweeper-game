package com.kotlearn.minesweeperk.core.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, ExperimentalResourceApi::class)
internal class IosSoundPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val players = mutableMapOf<GameSound, AVAudioPlayer>()

    init {
        scope.launch {
            GameSound.entries.forEach { sound ->
                val bytes = Res.readBytes("files/${sound.fileName}")
                if (bytes.isEmpty()) return@forEach
                val data = bytes.usePinned { pinned ->
                    NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                }
                AVAudioPlayer(data = data, error = null).also {
                    it.prepareToPlay()
                    players[sound] = it
                }
            }
        }
    }

    override fun play(sound: GameSound) {
        players[sound]?.let {
            it.currentTime = 0.0
            it.play()
        }
    }
}
