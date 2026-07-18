package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository

class UpdateFlagIconUseCase(
    private val repository: IconPreferencesRepository,
) {
    suspend operator fun invoke(icon: FlagIcon) = repository.updateFlagIcon(icon.name)
}
