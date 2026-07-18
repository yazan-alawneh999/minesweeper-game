package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository

class UpdateMineIconUseCase(
    private val repository: IconPreferencesRepository,
) {
    suspend operator fun invoke(icon: MineIcon) = repository.updateMineIcon(icon.name)
}
