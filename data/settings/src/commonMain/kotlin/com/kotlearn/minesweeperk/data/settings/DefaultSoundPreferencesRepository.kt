package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultSoundPreferencesRepository(
    private val storage: Storage,
) : SoundPreferencesRepository {

    override val soundEnabled: Flow<Boolean> = storage.getAsFlow(SoundPreferencesRepository.SoundEnabledKey)
        .map { it ?: SoundPreferencesRepository.SoundEnabledKey.defaultValue ?: true }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        storage.writeValue(SoundPreferencesRepository.SoundEnabledKey, enabled)
    }
}
