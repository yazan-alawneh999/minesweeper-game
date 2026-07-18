package com.kotlearn.minesweeperk.feature.play

import com.kotlearn.minesweeperk.core.audio.audioModule
import com.kotlearn.minesweeperk.domain.game.domainGameModule
import com.kotlearn.minesweeperk.domain.settings.domainSettingsModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playModule = module {

    includes(audioModule)
    includes(domainGameModule)
    includes(domainSettingsModule)

    viewModelOf(::PlayViewModel)

}