package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultIconPreferencesRepository(
    private val storage: Storage,
) : IconPreferencesRepository {

    override val flagIcon: Flow<String> = storage.getAsFlow(IconPreferencesRepository.FlagIconKey)
        .map { it ?: IconPreferencesRepository.FlagIconKey.defaultValue.orEmpty() }

    override val mineIcon: Flow<String> = storage.getAsFlow(IconPreferencesRepository.MineIconKey)
        .map { it ?: IconPreferencesRepository.MineIconKey.defaultValue.orEmpty() }

    override suspend fun updateFlagIcon(name: String) {
        storage.writeValue(IconPreferencesRepository.FlagIconKey, name)
    }

    override suspend fun updateMineIcon(name: String) {
        storage.writeValue(IconPreferencesRepository.MineIconKey, name)
    }
}
