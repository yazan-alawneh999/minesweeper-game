package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface IconPreferencesRepository {

    val flagIcon: Flow<String>
    val mineIcon: Flow<String>

    suspend fun updateFlagIcon(name: String)
    suspend fun updateMineIcon(name: String)

    data object FlagIconKey : Storage.Key.StringKey("flag_icon", "RED_FLAG")
    data object MineIconKey : Storage.Key.StringKey("mine_icon", "BOMB")
}
