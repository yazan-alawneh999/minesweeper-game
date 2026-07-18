package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.SoundPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetSoundEnabledAsFlowUseCase(
    private val repository: SoundPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.soundEnabled
}
