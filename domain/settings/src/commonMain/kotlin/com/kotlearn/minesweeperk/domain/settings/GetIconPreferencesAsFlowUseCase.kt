package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetIconPreferencesAsFlowUseCase(
    private val repository: IconPreferencesRepository,
) {
    operator fun invoke(): Flow<IconPreferences> =
        combine(repository.flagIcon, repository.mineIcon) { flag, mine ->
            IconPreferences(flag = FlagIcon.fromName(flag), mine = MineIcon.fromName(mine))
        }
}
