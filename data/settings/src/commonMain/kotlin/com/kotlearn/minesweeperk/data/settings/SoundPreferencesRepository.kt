package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface SoundPreferencesRepository {

    val soundEnabled: Flow<Boolean>

    suspend fun updateSoundEnabled(enabled: Boolean)

    data object SoundEnabledKey : Storage.Key.BooleanKey("sound_enabled", true)
}
