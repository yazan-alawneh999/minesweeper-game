package com.kotlearn.minesweeperk.domain.game

import com.kotlearn.minesweeperk.data.game.dataGameModule
import com.kotlearn.minesweeperk.data.settings.dataSettingsModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainGameModule = module {

    includes(dataSettingsModule, dataGameModule)

    factoryOf(::CreateGameUseCase)
    factoryOf(::RevealTileUseCase)
    factoryOf(::ToggleFlagUseCase)
    factoryOf(::AddHighscoreUseCase)
    factoryOf(::GetHighscoresAsFlowUseCase)

}