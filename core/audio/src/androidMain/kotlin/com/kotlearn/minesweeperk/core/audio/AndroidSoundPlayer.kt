package com.kotlearn.minesweeperk.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.io.File

@OptIn(ExperimentalResourceApi::class)
internal class AndroidSoundPlayer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<GameSound, Int>()

    init {
        scope.launch {
            GameSound.entries.forEach { sound ->
                val bytes = Res.readBytes("files/${sound.fileName}")
                val file = File.createTempFile(sound.name, ".wav", context.cacheDir).apply {
                    writeBytes(bytes)
                    deleteOnExit()
                }
                soundIds[sound] = soundPool.load(file.absolutePath, 1)
            }
        }
    }

    override fun play(sound: GameSound) {
        val id = soundIds[sound] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }
}
