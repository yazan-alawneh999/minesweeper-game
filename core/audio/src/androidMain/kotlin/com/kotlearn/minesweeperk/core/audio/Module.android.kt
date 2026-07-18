package com.kotlearn.minesweeperk.core.audio

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAudioModule: Module = module {
    single { AndroidSoundPlayer(context = get()) } bind SoundPlayer::class
}
