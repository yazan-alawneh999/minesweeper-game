package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.dataSettingsModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainSettingsModule = module {

    includes(dataSettingsModule)

    factoryOf(::GetUsernameAsFlowUseCase)
    factoryOf(::UpdateUsernameUseCase)

    factoryOf(::GetDifficultyAsFlowUseCase)
    factoryOf(::UpdateDifficultyUseCase)

    factoryOf(::GetBoardSizeAsFlowUseCase)
    factoryOf(::UpdateBoardSizeUseCase)
    factoryOf(::GetIconPreferencesAsFlowUseCase)
    factoryOf(::UpdateFlagIconUseCase)
    factoryOf(::UpdateMineIconUseCase)
    factoryOf(::GetSoundEnabledAsFlowUseCase)
    factoryOf(::UpdateSoundEnabledUseCase)

}