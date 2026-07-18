package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.BoardSizeRepository
import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository
import com.kotlearn.minesweeperk.data.settings.SoundPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeBoardSizeRepository : BoardSizeRepository {
    val cols = MutableStateFlow(10)
    val rowsFlow = MutableStateFlow(16)
    override val columns: Flow<Int> get() = cols
    override val rows: Flow<Int> get() = rowsFlow
    override suspend fun updateSize(columns: Int, rows: Int) { cols.value = columns; rowsFlow.value = rows }
}

private class FakeIconPreferencesRepository : IconPreferencesRepository {
    val flag = MutableStateFlow("RED_FLAG")
    val mine = MutableStateFlow("BOMB")
    override val flagIcon: Flow<String> get() = flag
    override val mineIcon: Flow<String> get() = mine
    override suspend fun updateFlagIcon(name: String) { flag.value = name }
    override suspend fun updateMineIcon(name: String) { mine.value = name }
}

private class FakeSoundPreferencesRepository : SoundPreferencesRepository {
    val enabled = MutableStateFlow(true)
    override val soundEnabled: Flow<Boolean> get() = enabled
    override suspend fun updateSoundEnabled(enabled: Boolean) { this.enabled.value = enabled }
}

class SettingsUseCasesTest {

    @Test
    fun get_board_size_maps_to_clamped_BoardSize() = runTest {
        val repo = FakeBoardSizeRepository().apply { cols.value = 99; rowsFlow.value = 2 }
        val size = GetBoardSizeAsFlowUseCase(repo).invoke().first()
        assertEquals(BoardSize.MAX_SIDE, size.columns)
        assertEquals(BoardSize.MIN_SIDE, size.rows)
    }

    @Test
    fun update_board_size_writes_repo() = runTest {
        val repo = FakeBoardSizeRepository()
        UpdateBoardSizeUseCase(repo).invoke(BoardSize.of(12, 20))
        assertEquals(12, repo.cols.value)
        assertEquals(20, repo.rowsFlow.value)
    }

    @Test
    fun get_icon_preferences_maps_names_to_enums() = runTest {
        val repo = FakeIconPreferencesRepository().apply { flag.value = "PIN"; mine.value = "SKULL" }
        val prefs = GetIconPreferencesAsFlowUseCase(repo).invoke().first()
        assertEquals(FlagIcon.PIN, prefs.flag)
        assertEquals(MineIcon.SKULL, prefs.mine)
    }

    @Test
    fun update_icon_use_cases_write_enum_name() = runTest {
        val repo = FakeIconPreferencesRepository()
        UpdateFlagIconUseCase(repo).invoke(FlagIcon.BLACK_FLAG)
        UpdateMineIconUseCase(repo).invoke(MineIcon.EXPLOSION)
        assertEquals("BLACK_FLAG", repo.flag.value)
        assertEquals("EXPLOSION", repo.mine.value)
    }

    @Test
    fun sound_use_cases_round_trip() = runTest {
        val repo = FakeSoundPreferencesRepository()
        UpdateSoundEnabledUseCase(repo).invoke(false)
        assertEquals(false, GetSoundEnabledAsFlowUseCase(repo).invoke().first())
    }
}
