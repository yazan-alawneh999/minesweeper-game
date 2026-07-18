package com.kotlearn.minesweeperk.core.audio

import org.koin.core.module.Module
import org.koin.dsl.module

val audioModule = module {
    includes(platformAudioModule)
}

internal expect val platformAudioModule: Module
