package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.SoundPreferencesRepository

class UpdateSoundEnabledUseCase(
    private val repository: SoundPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateSoundEnabled(enabled)
}
