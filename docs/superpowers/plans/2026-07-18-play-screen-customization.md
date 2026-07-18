# Play Screen Customization & Engagement — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add customizable board size, selectable flag/mine icons, cross-platform sound effects, a fireworks win animation, and an inline quick-settings panel to the Minesweeper play screen.

**Architecture:** Extend the existing DataStore-backed settings layer with new preferences (board size, icons, sound on/off). Render icons from domain enums. Add a new `core/audio` KMP module exposing an `expect/actual` `SoundPlayer`. Trigger sounds and a Compose-`Canvas` fireworks animation from `PlayViewModel`/`PlayScreen` on game-state transitions. Surface all controls in an inline liquid-glass panel on the play screen.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin DI, AndroidX DataStore, `compose.components.resources` (for bundled audio bytes), platform audio APIs (Android `SoundPool`, iOS `AVAudioPlayer`, Desktop `javax.sound.sampled`).

**Reference spec:** `docs/superpowers/specs/2026-07-18-play-screen-customization-design.md`

---

## Conventions used throughout

- Follow existing module patterns exactly. Repos live in `data/settings`, use cases in `domain/settings`, UI in `feature/play`.
- Keys extend `Storage.Key` sealed classes (`IntKey`, `StringKey`, `BooleanKey`).
- Koin: repos `singleOf(...) { bind<Interface>() }` in `dataSettingsModule`; use cases `factoryOf(...)` in `domainSettingsModule`.
- Tests: `commonTest`, `kotlin("test")` with `kotlin.test.Test/assertEquals`. Run a module's tests with `./gradlew :module:path:allTests` (or `:desktopTest` for JVM-only).
- Commit after every green step. Branch is already `add-customize-play-screen`.
- **Verification of UI, audio playback, and animation is done live on the Android emulator per the `verify` skill** — those tasks have a manual verification step instead of a unit test.

## File Structure Overview

**Phase 1 — Settings backbone** (`data/settings`, `domain/settings`)
- Create `data/settings/.../BoardSizeRepository.kt`, `DefaultBoardSizeRepository.kt`
- Create `data/settings/.../IconPreferencesRepository.kt`, `DefaultIconPreferencesRepository.kt`
- Create `data/settings/.../SoundPreferencesRepository.kt`, `DefaultSoundPreferencesRepository.kt`
- Modify `data/settings/.../Module.kt`
- Create `domain/settings/.../BoardSize.kt`, `FlagIcon.kt`, `MineIcon.kt`, `IconPreferences.kt`
- Create use cases: `GetBoardSizeAsFlowUseCase`, `UpdateBoardSizeUseCase`, `GetIconPreferencesAsFlowUseCase`, `UpdateFlagIconUseCase`, `UpdateMineIconUseCase`, `GetSoundEnabledAsFlowUseCase`, `UpdateSoundEnabledUseCase`
- Modify `domain/settings/.../Module.kt`

**Phase 2 — Icon rendering** (`feature/play`)
- Create `feature/play/.../TileIcon.kt`
- Modify `MinesweeperBoard.kt` (accept icon params)

**Phase 3 — Board size in ViewModel** (`feature/play`)
- Modify `PlayViewModel.kt`, `PlayScreen.kt`

**Phase 4 — Audio module** (`core/audio` — new)
- Create module `core/audio` with `SoundPlayer.kt`, `GameSound.kt`, `Module.kt` + platform actuals + audio assets
- Modify `settings.gradle.kts`
- Wire into `PlayViewModel.kt` + `feature/play/build.gradle.kts` + `playModule`

**Phase 5 — Fireworks** (`feature/play`)
- Create `feature/play/.../FireworksOverlay.kt`
- Modify `PlayScreen.kt`

**Phase 6 — Inline quick-settings panel** (`feature/play`)
- Create `feature/play/.../QuickSettingsPanel.kt`
- Modify `PlayScreen.kt`, `PlayViewModel.kt`

---

# Phase 1 — Settings backbone

Goal: persist and expose the five new preferences. Fully unit-tested; nothing visible yet.

### Task 1.1: Board size domain model

**Files:**
- Create: `domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/BoardSize.kt`
- Test: `domain/settings/src/commonTest/kotlin/com/kotlearn/minesweeperk/domain/settings/BoardSizeTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.kotlearn.minesweeperk.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class BoardSizeTest {

    @Test
    fun coerces_columns_and_rows_into_bounds() {
        assertEquals(BoardSize.MIN_SIDE, BoardSize.of(columns = 1, rows = 1).columns)
        assertEquals(BoardSize.MAX_SIDE, BoardSize.of(columns = 999, rows = 999).rows)
    }

    @Test
    fun tile_count_is_columns_times_rows() {
        assertEquals(150, BoardSize.of(columns = 10, rows = 15).tileCount)
    }

    @Test
    fun default_is_ten_by_sixteen() {
        assertEquals(10, BoardSize.DEFAULT.columns)
        assertEquals(16, BoardSize.DEFAULT.rows)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :domain:settings:desktopTest --tests "*BoardSizeTest*"`
Expected: FAIL — `BoardSize` unresolved.

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.kotlearn.minesweeperk.domain.settings

/**
 * Explicit board dimensions chosen by the player. Construct via [of], which
 * clamps to [MIN_SIDE]..[MAX_SIDE] so a corrupt/persisted value can never
 * produce an unplayable board.
 */
data class BoardSize private constructor(
    val columns: Int,
    val rows: Int,
) {
    val tileCount: Int get() = columns * rows

    companion object {
        const val MIN_SIDE = 5
        const val MAX_SIDE = 30
        val DEFAULT = of(columns = 10, rows = 16)

        fun of(columns: Int, rows: Int) = BoardSize(
            columns = columns.coerceIn(MIN_SIDE, MAX_SIDE),
            rows = rows.coerceIn(MIN_SIDE, MAX_SIDE),
        )
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :domain:settings:desktopTest --tests "*BoardSizeTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/BoardSize.kt domain/settings/src/commonTest/kotlin/com/kotlearn/minesweeperk/domain/settings/BoardSizeTest.kt
git commit -m "feat(settings): add BoardSize domain model with bounds"
```

### Task 1.2: FlagIcon and MineIcon enums

**Files:**
- Create: `domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/FlagIcon.kt`
- Create: `domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/MineIcon.kt`
- Test: `domain/settings/src/commonTest/kotlin/com/kotlearn/minesweeperk/domain/settings/IconEnumTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.kotlearn.minesweeperk.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull

class IconEnumTest {

    @Test
    fun flag_fromName_falls_back_to_default_when_unknown() {
        assertEquals(FlagIcon.DEFAULT, FlagIcon.fromName("nope"))
        assertEquals(FlagIcon.DEFAULT, FlagIcon.fromName(null))
    }

    @Test
    fun mine_fromName_parses_known_value() {
        assertEquals(MineIcon.SKULL, MineIcon.fromName("SKULL"))
    }

    @Test
    fun emoji_icons_carry_an_emoji_vector_icons_do_not() {
        assertNotNull(FlagIcon.RED_FLAG.emoji)
        assertNull(FlagIcon.MATERIAL_FILLED.emoji)
        assertNotNull(MineIcon.BOMB.emoji)
        assertNull(MineIcon.CLASSIC_DOT.emoji)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :domain:settings:desktopTest --tests "*IconEnumTest*"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Write minimal implementation**

`FlagIcon.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

/**
 * A player-selectable flag icon. Emoji variants carry an [emoji] string;
 * vector variants leave it null and are rendered from a Compose vector in the
 * UI layer (keeps this module free of Compose types).
 */
enum class FlagIcon(val emoji: String?) {
    RED_FLAG(emoji = "🚩"),   // 🚩
    BLACK_FLAG(emoji = "🏴"), // 🏴
    PIN(emoji = "📍"),        // 📍
    MATERIAL_FILLED(emoji = null),
    MATERIAL_OUTLINED(emoji = null);

    companion object {
        val DEFAULT = RED_FLAG
        fun fromName(name: String?): FlagIcon =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
```

`MineIcon.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

/**
 * A player-selectable mine icon. Emoji variants carry an [emoji] string;
 * vector variants leave it null and render from a Compose vector/drawable.
 */
enum class MineIcon(val emoji: String?) {
    BOMB(emoji = "💣"),      // 💣
    EXPLOSION(emoji = "💥"), // 💥
    SKULL(emoji = "☠️"),     // ☠️
    CLASSIC_DOT(emoji = null),
    SPIKED(emoji = null);

    companion object {
        val DEFAULT = BOMB
        fun fromName(name: String?): MineIcon =
            entries.firstOrNull { it.name == name } ?: DEFAULT
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :domain:settings:desktopTest --tests "*IconEnumTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/FlagIcon.kt domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/MineIcon.kt domain/settings/src/commonTest/kotlin/com/kotlearn/minesweeperk/domain/settings/IconEnumTest.kt
git commit -m "feat(settings): add FlagIcon and MineIcon enums"
```

### Task 1.3: BoardSizeRepository (data layer)

**Files:**
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/BoardSizeRepository.kt`
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/DefaultBoardSizeRepository.kt`

> This layer stores raw Ints; no unit test (mirrors the untested `DefaultDifficultyRepository`). Correctness is covered by the domain use-case tests in Task 1.6.

- [ ] **Step 1: Write the interface + key**

`BoardSizeRepository.kt`:
```kotlin
package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface BoardSizeRepository {

    val columns: Flow<Int>
    val rows: Flow<Int>

    suspend fun updateSize(columns: Int, rows: Int)

    data object ColumnsKey : Storage.Key.IntKey("board_columns", 10)
    data object RowsKey : Storage.Key.IntKey("board_rows", 16)
}
```

- [ ] **Step 2: Write the implementation**

`DefaultBoardSizeRepository.kt`:
```kotlin
package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DefaultBoardSizeRepository(
    private val storage: Storage,
) : BoardSizeRepository {

    override val columns: Flow<Int> = storage.getAsFlow(BoardSizeRepository.ColumnsKey)
        .map { it ?: BoardSizeRepository.ColumnsKey.defaultValue ?: 10 }

    override val rows: Flow<Int> = storage.getAsFlow(BoardSizeRepository.RowsKey)
        .map { it ?: BoardSizeRepository.RowsKey.defaultValue ?: 16 }

    override suspend fun updateSize(columns: Int, rows: Int) {
        storage.writeValue(BoardSizeRepository.ColumnsKey, columns)
        storage.writeValue(BoardSizeRepository.RowsKey, rows)
    }
}
```

- [ ] **Step 3: Compile**

Run: `./gradlew :data:settings:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/BoardSizeRepository.kt data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/DefaultBoardSizeRepository.kt
git commit -m "feat(settings): add BoardSizeRepository"
```

### Task 1.4: IconPreferencesRepository (data layer)

**Files:**
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/IconPreferencesRepository.kt`
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/DefaultIconPreferencesRepository.kt`

- [ ] **Step 1: Write the interface + keys**

```kotlin
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
```

- [ ] **Step 2: Write the implementation**

```kotlin
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
```

- [ ] **Step 3: Compile**

Run: `./gradlew :data:settings:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/IconPreferencesRepository.kt data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/DefaultIconPreferencesRepository.kt
git commit -m "feat(settings): add IconPreferencesRepository"
```

### Task 1.5: SoundPreferencesRepository (data layer)

**Files:**
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/SoundPreferencesRepository.kt`
- Create: `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/DefaultSoundPreferencesRepository.kt`

- [ ] **Step 1: Write the interface + key**

```kotlin
package com.kotlearn.minesweeperk.data.settings

import com.kotlearn.minesweeperk.data.core.storage.Storage
import kotlinx.coroutines.flow.Flow

interface SoundPreferencesRepository {

    val soundEnabled: Flow<Boolean>

    suspend fun updateSoundEnabled(enabled: Boolean)

    data object SoundEnabledKey : Storage.Key.BooleanKey("sound_enabled", true)
}
```

- [ ] **Step 2: Write the implementation**

```kotlin
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
```

- [ ] **Step 3: Register all three repos in Koin**

Modify `data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/Module.kt` — add inside the `module { ... }` block, after the existing `DefaultDifficultyRepository` binding:

```kotlin
    singleOf(::DefaultBoardSizeRepository) {
        bind<BoardSizeRepository>()
    }

    singleOf(::DefaultIconPreferencesRepository) {
        bind<IconPreferencesRepository>()
    }

    singleOf(::DefaultSoundPreferencesRepository) {
        bind<SoundPreferencesRepository>()
    }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :data:settings:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add data/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/data/settings/
git commit -m "feat(settings): add SoundPreferencesRepository and register repos in Koin"
```

### Task 1.6: Domain use cases + models for the new prefs

**Files:**
- Create: `domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/IconPreferences.kt`
- Create: `domain/settings/.../GetBoardSizeAsFlowUseCase.kt`, `UpdateBoardSizeUseCase.kt`
- Create: `domain/settings/.../GetIconPreferencesAsFlowUseCase.kt`, `UpdateFlagIconUseCase.kt`, `UpdateMineIconUseCase.kt`
- Create: `domain/settings/.../GetSoundEnabledAsFlowUseCase.kt`, `UpdateSoundEnabledUseCase.kt`
- Modify: `domain/settings/.../Module.kt`
- Test: `domain/settings/src/commonTest/kotlin/com/kotlearn/minesweeperk/domain/settings/SettingsUseCasesTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :domain:settings:desktopTest --tests "*SettingsUseCasesTest*"`
Expected: FAIL — use cases + `IconPreferences` unresolved.

- [ ] **Step 3: Write the models + use cases**

`IconPreferences.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

data class IconPreferences(
    val flag: FlagIcon,
    val mine: MineIcon,
)
```

`GetBoardSizeAsFlowUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.BoardSizeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBoardSizeAsFlowUseCase(
    private val repository: BoardSizeRepository,
) {
    operator fun invoke(): Flow<BoardSize> =
        combine(repository.columns, repository.rows) { c, r -> BoardSize.of(columns = c, rows = r) }
}
```

`UpdateBoardSizeUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.BoardSizeRepository

class UpdateBoardSizeUseCase(
    private val repository: BoardSizeRepository,
) {
    suspend operator fun invoke(size: BoardSize) {
        repository.updateSize(columns = size.columns, rows = size.rows)
    }
}
```

`GetIconPreferencesAsFlowUseCase.kt`:
```kotlin
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
```

`UpdateFlagIconUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository

class UpdateFlagIconUseCase(
    private val repository: IconPreferencesRepository,
) {
    suspend operator fun invoke(icon: FlagIcon) = repository.updateFlagIcon(icon.name)
}
```

`UpdateMineIconUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.IconPreferencesRepository

class UpdateMineIconUseCase(
    private val repository: IconPreferencesRepository,
) {
    suspend operator fun invoke(icon: MineIcon) = repository.updateMineIcon(icon.name)
}
```

`GetSoundEnabledAsFlowUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.SoundPreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetSoundEnabledAsFlowUseCase(
    private val repository: SoundPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.soundEnabled
}
```

`UpdateSoundEnabledUseCase.kt`:
```kotlin
package com.kotlearn.minesweeperk.domain.settings

import com.kotlearn.minesweeperk.data.settings.SoundPreferencesRepository

class UpdateSoundEnabledUseCase(
    private val repository: SoundPreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) = repository.updateSoundEnabled(enabled)
}
```

- [ ] **Step 4: Register use cases in Koin**

Modify `domain/settings/src/commonMain/kotlin/com/kotlearn/minesweeperk/domain/settings/Module.kt` — add inside `module { ... }`:

```kotlin
    factoryOf(::GetBoardSizeAsFlowUseCase)
    factoryOf(::UpdateBoardSizeUseCase)
    factoryOf(::GetIconPreferencesAsFlowUseCase)
    factoryOf(::UpdateFlagIconUseCase)
    factoryOf(::UpdateMineIconUseCase)
    factoryOf(::GetSoundEnabledAsFlowUseCase)
    factoryOf(::UpdateSoundEnabledUseCase)
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :domain:settings:desktopTest --tests "*SettingsUseCasesTest*"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add domain/settings/src/
git commit -m "feat(settings): add board size, icon, and sound use cases"
```

---

# Phase 2 — Icon rendering

Goal: `MinesweeperBoard` renders the selected flag/mine icons. Verified via preview + emulator.

### Task 2.1: TileIcon composable

**Files:**
- Create: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/TileIcon.kt`

- [ ] **Step 1: Write the composable**

```kotlin
package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import minesweeperk.feature.play.generated.resources.Res
import minesweeperk.feature.play.generated.resources.mine
import org.jetbrains.compose.resources.painterResource

/** Renders the selected flag icon, filling its parent. Emoji variants draw as
 *  text (sized via [emojiSize]); vector variants draw as a tinted [Icon]. */
@Composable
internal fun FlagTileIcon(
    icon: FlagIcon,
    tint: Color,
    emojiSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        Text(text = emoji, fontSize = emojiSize, textAlign = TextAlign.Center, modifier = modifier)
    } else {
        val vector = when (icon) {
            FlagIcon.MATERIAL_OUTLINED -> Icons.Outlined.Flag
            else -> Icons.Filled.Flag
        }
        Icon(imageVector = vector, contentDescription = null, tint = tint, modifier = modifier.fillMaxSize())
    }
}

/** Renders the selected mine icon. Emoji variants draw as text; vector variants
 *  draw the bundled `mine` drawable (SPIKED) or a filled dot (CLASSIC_DOT). */
@Composable
internal fun MineTileIcon(
    icon: MineIcon,
    tint: Color,
    emojiSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    val emoji = icon.emoji
    if (emoji != null) {
        Text(text = emoji, fontSize = emojiSize, textAlign = TextAlign.Center, modifier = modifier)
    } else {
        when (icon) {
            MineIcon.CLASSIC_DOT -> Icon(
                painter = painterResource(Res.drawable.mine),
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
            else -> Icon( // SPIKED — the existing drawable
                painter = painterResource(Res.drawable.mine),
                contentDescription = null,
                tint = tint,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}
```

> Note: `CLASSIC_DOT` and `SPIKED` both use the existing `mine` drawable for now (the project ships only one mine drawable). If a distinct "classic dot" vector is desired later, add a `classic_mine.xml` drawable and swap the `CLASSIC_DOT` branch. This is intentionally not a placeholder — both branches are functional today.

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL (confirms `Icons.Outlined.Flag` resolves; `materialIconsExtended` is available via `ui:core`).

- [ ] **Step 3: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/TileIcon.kt
git commit -m "feat(play): add TileIcon composables for flag and mine icons"
```

### Task 2.2: Thread icons into MinesweeperBoard

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/MinesweeperBoard.kt`

- [ ] **Step 1: Add icon params to `MinesweeperBoard` and `Tile`**

In `MinesweeperBoard(...)` add two params (with defaults so previews/tests keep compiling), after `textStyle`:

```kotlin
    flagIcon: FlagIcon = FlagIcon.DEFAULT,
    mineIcon: MineIcon = MineIcon.DEFAULT,
```

Add imports:
```kotlin
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
```

Pass them down to each `Tile(...)` call (add both arguments):
```kotlin
                            Tile(
                                state = tileState,
                                revealedBorderWidth = revealedBorderWidth,
                                hiddenBorderWidth = hiddenBorderWidth,
                                textStyle = sizeAdjustedTextStyle,
                                flagIcon = flagIcon,
                                mineIcon = mineIcon,
                                modifier = Modifier
                                    .size(tileLength)
                                    .combinedClickable(
                                        onClick = { onTileClick(x, y) },
                                        onLongClick = { onTileLongClick(x, y) },
                                    )
                            )
```

- [ ] **Step 2: Update `Tile` to render via TileIcon**

Change the `Tile` signature to accept the icons:
```kotlin
@Composable
private fun Tile(
    state: TileState,
    revealedBorderWidth: Dp,
    hiddenBorderWidth: Dp,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(),
    flagIcon: FlagIcon = FlagIcon.DEFAULT,
    mineIcon: MineIcon = MineIcon.DEFAULT,
) {
```

Replace the `TileState.Hidden` flag branch body:
```kotlin
            is TileState.Hidden -> {
                if (state.flagged) {
                    FlagTileIcon(
                        icon = flagIcon,
                        tint = LocalMinesweeperBoardColorScheme.current.flag,
                        emojiSize = textStyle.fontSize,
                        modifier = Modifier.fillMaxSize(fraction = 0.65f),
                    )
                }
            }
```

Replace the `TileState.Revealed.Mine` branch:
```kotlin
            TileState.Revealed.Mine -> {
                MineTileIcon(
                    icon = mineIcon,
                    tint = LocalMinesweeperBoardColorScheme.current.mine,
                    emojiSize = textStyle.fontSize,
                    modifier = Modifier.fillMaxSize(fraction = 0.6f),
                )
            }
```

Remove the now-unused imports `androidx.compose.material.icons.Icons`, `androidx.compose.material.icons.filled.Flag`, `minesweeperk.feature.play.generated.resources.mine`, and `org.jetbrains.compose.resources.painterResource` **only if** no longer referenced (the `TilesPreview`/board previews may still compile without them). Let the compiler guide you.

- [ ] **Step 3: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run existing board tests to confirm no regression**

Run: `./gradlew :feature:play:desktopTest --tests "*MinesweeperBoardTest*"`
Expected: PASS (defaults keep behavior identical)

- [ ] **Step 5: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/MinesweeperBoard.kt
git commit -m "feat(play): render selectable flag/mine icons in board"
```

---

# Phase 3 — Board size from settings

Goal: board dimensions come from persisted `BoardSize` instead of screen measurement. Mine count derives from dimensions × difficulty density.

### Task 3.1: PlayViewModel uses BoardSize + IconPreferences

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt`
- Test: `feature/play/src/commonTest/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModelBoardSizeTest.kt`

- [ ] **Step 1: Write the failing test** (mine count derives from dimensions × density)

```kotlin
package com.kotlearn.minesweeperk.feature.play

import com.kotlearn.minesweeperk.domain.settings.BoardSize
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayViewModelBoardSizeTest {

    @Test
    fun mine_count_is_dimensions_times_density() {
        val size = BoardSize.of(columns = 10, rows = 16) // 160 tiles
        val mines = mineCountFor(size = size, difficulty = Difficulty.EASY) // 12%
        assertEquals(Difficulty.EASY.mineCountFor(size.tileCount), mines)
        assertEquals(19, mines) // round(160 * 0.12) = 19
    }
}
```

> This tests a small pure helper `mineCountFor` extracted so the calculation is unit-testable without constructing the full ViewModel (which needs Koin/Android deps).

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :feature:play:desktopTest --tests "*PlayViewModelBoardSizeTest*"`
Expected: FAIL — `mineCountFor` unresolved.

- [ ] **Step 3: Add the helper and rewire the ViewModel**

Add this top-level internal helper in `PlayViewModel.kt` (below the class):
```kotlin
internal fun mineCountFor(size: BoardSize, difficulty: Difficulty): Int =
    difficulty.mineCountFor(tileCount = size.tileCount)
```

Rewire `PlayViewModel`:
1. Add constructor params (after `getDifficultyAsFlowUseCase`):
```kotlin
    private val getBoardSizeAsFlowUseCase: GetBoardSizeAsFlowUseCase,
    private val getIconPreferencesAsFlowUseCase: GetIconPreferencesAsFlowUseCase,
```
   with imports:
```kotlin
import com.kotlearn.minesweeperk.domain.settings.BoardSize
import com.kotlearn.minesweeperk.domain.settings.GetBoardSizeAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.GetIconPreferencesAsFlowUseCase
import com.kotlearn.minesweeperk.domain.settings.IconPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
```

2. Replace the `columns`/`rows` measurement fields with a board size held from settings:
```kotlin
    // Board dimensions now come from the player's saved preference, not screen
    // measurement. Restored games keep their own stored dimensions.
    private var boardSize: BoardSize? = savedStateHandle.get<String>(KEY_BOARD_SIZE)?.let {
        val parts = it.split("x")
        parts.getOrNull(0)?.toIntOrNull()?.let { c ->
            parts.getOrNull(1)?.toIntOrNull()?.let { r -> BoardSize.of(c, r) }
        }
    }
```
   Add companion key: `const val KEY_BOARD_SIZE = "boardSize"`.

3. Expose icon preferences as UI state:
```kotlin
    val iconPreferences = getIconPreferencesAsFlowUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IconPreferences(
            flag = com.kotlearn.minesweeperk.domain.settings.FlagIcon.DEFAULT,
            mine = com.kotlearn.minesweeperk.domain.settings.MineIcon.DEFAULT,
        ))
```

4. In `init`, collect board size alongside difficulty. Replace the current `getDifficultyAsFlowUseCase().collect { ... }` block with a `combine` of difficulty + board size:
```kotlin
        viewModelScope.launch {
            combine(getDifficultyAsFlowUseCase(), getBoardSizeAsFlowUseCase()) { d, s -> d to s }
                .collect { (newDifficulty, newSize) ->
                    val difficultyChanged = difficulty != null && newDifficulty != difficulty
                    val sizeChanged = boardSize != null && newSize != boardSize
                    difficulty = newDifficulty
                    boardSize = newSize
                    savedStateHandle[KEY_DIFFICULTY] = newDifficulty.name
                    savedStateHandle[KEY_BOARD_SIZE] = "${newSize.columns}x${newSize.rows}"
                    if (_gameState.value == null || difficultyChanged || sizeChanged) {
                        startNewGame()
                    }
                }
        }
```
   Add import `kotlinx.coroutines.flow.combine`. In the `init` restore block, replace the `columns`/`rows` restore with:
```kotlin
        _gameState.value?.let { restored ->
            boardSize = BoardSize.of(columns = restored.width, rows = restored.height)
            if (restored.status == GameStatus.PLAYING && restored.hasStarted()) {
                startTimerIfNeeded()
            }
        }
```

5. Delete `onBoardMeasured(...)` entirely and remove the `columns`/`rows` private vars. Rewrite `startNewGame()`:
```kotlin
    private fun startNewGame() {
        val difficulty = difficulty ?: return
        val size = boardSize ?: return
        stopTimer()
        setElapsedSeconds(0)
        setGameState(
            createGameUseCase(
                width = size.columns,
                height = size.rows,
                mineCount = mineCountFor(size = size, difficulty = difficulty),
            )
        )
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :feature:play:desktopTest --tests "*PlayViewModelBoardSizeTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt feature/play/src/commonTest/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModelBoardSizeTest.kt
git commit -m "feat(play): drive board dimensions from saved BoardSize preference"
```

### Task 3.2: PlayScreen — drop measurement, pass icons

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt`

- [ ] **Step 1: Remove the `onBoardMeasured` wiring and pass icons to the board**

In `PlayScreen`, collect icon prefs:
```kotlin
    val iconPreferences by viewModel.iconPreferences.collectAsStateWithLifecycle()
```
Inside the board `BoxWithConstraints`, delete the `targetTileSize`/`columns`/`rows`/`LaunchedEffect(...) { viewModel.onBoardMeasured(...) }` block entirely. Keep the `BoxWithConstraints` (it still constrains the board area). Update the `MinesweeperBoard(...)` call to pass icons:
```kotlin
                    if (state != null) {
                        MinesweeperBoard(
                            tileStates = state.toTileStates(),
                            onTileClick = viewModel::revealTile,
                            onTileLongClick = viewModel::toggleFlag,
                            flagIcon = iconPreferences.flag,
                            mineIcon = iconPreferences.mine,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
```

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL (no more references to `onBoardMeasured`).

- [ ] **Step 3: Verify on emulator** (per `verify` skill)

Build & install, open Play. Expected: a 10×16 board appears immediately, tiles scale to fit, game is playable. Flag long-press shows 🚩; hitting a mine shows 💣. Rotate device — the in-progress game is preserved (no reset).

- [ ] **Step 4: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt
git commit -m "feat(play): render fixed-size board with selected icons"
```

---

# Phase 4 — Cross-platform audio (`core/audio` module)

Goal: a `SoundPlayer` that plays three bundled SFX on each platform, triggered from `PlayViewModel` and gated by the sound-enabled preference.

### Task 4.1: Scaffold the `core/audio` module

**Files:**
- Create: `core/audio/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Create: `core/audio/src/commonMain/kotlin/com/kotlearn/minesweeperk/core/audio/GameSound.kt`
- Create: `core/audio/src/commonMain/kotlin/com/kotlearn/minesweeperk/core/audio/SoundPlayer.kt`

- [ ] **Step 1: Register the module**

In `settings.gradle.kts`, add after `include(":data:core")` (grouping with other core-ish modules):
```kotlin
include(":core:audio")
```

- [ ] **Step 2: Create `core/audio/build.gradle.kts`** (mirrors `ui/core`, adds Koin + resources)

```kotlin
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlearn.kotlinMultiplatform)
    alias(libs.plugins.kotlearn.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.components.resources)
            implementation(libs.bundles.koin.compose)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.resources {
    publicResClass = true
    generateResClass = always
}
```

> Confirm the koin bundle accessor name in `gradle/libs.versions.toml` before relying on `libs.bundles.koin.compose` — the convention plugin references it as `libs.findBundle("koin.compose")`, so the type-safe accessor is `libs.bundles.koin.compose`. If the bundle is named differently, match whatever `FeatureModuleConventionPlugin.kt` uses.

- [ ] **Step 3: Create the common interface + enum**

`GameSound.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

enum class GameSound {
    EXPLOSION,
    WIN,
    FLAG,
}
```

`SoundPlayer.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

/** Plays short game sound effects. Implementations are per-platform. Callers
 *  are responsible for respecting the user's sound-enabled preference. */
interface SoundPlayer {
    fun play(sound: GameSound)
}
```

- [ ] **Step 4: Compile the module (common only)**

Run: `./gradlew :core:audio:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL (once koin bundle name is correct).

- [ ] **Step 5: Commit**

```bash
git add settings.gradle.kts core/audio/build.gradle.kts core/audio/src/commonMain
git commit -m "feat(audio): scaffold core/audio module with SoundPlayer interface"
```

### Task 4.2: Add sound assets

**Files:**
- Create: `core/audio/src/commonMain/composeResources/files/explosion.wav`
- Create: `core/audio/src/commonMain/composeResources/files/win.wav`
- Create: `core/audio/src/commonMain/composeResources/files/flag.wav`

- [ ] **Step 1: Add three short CC0/royalty-free WAV files** (< 1s each; 44.1kHz mono keeps them tiny).

Sourcing options (any one):
- Download CC0 SFX from freesound.org or mixkit.co (search "explosion", "success chime", "click"). Verify the license is CC0/public-domain.
- Or generate simple placeholder tones locally with `ffmpeg`:
  ```bash
  mkdir -p core/audio/src/commonMain/composeResources/files
  ffmpeg -f lavfi -i "sine=frequency=110:duration=0.4" -ac 1 core/audio/src/commonMain/composeResources/files/explosion.wav
  ffmpeg -f lavfi -i "sine=frequency=880:duration=0.6" -ac 1 core/audio/src/commonMain/composeResources/files/win.wav
  ffmpeg -f lavfi -i "sine=frequency=440:duration=0.1" -ac 1 core/audio/src/commonMain/composeResources/files/flag.wav
  ```
  (Replace with real SFX before release; tones are functional placeholders so the pipeline can be built and tested now.)

- [ ] **Step 2: Verify resource generation**

Run: `./gradlew :core:audio:generateComposeResClass`
Then confirm `Res.readBytes("files/explosion.wav")` will resolve (the generated `Res` object exposes `readBytes`). Compile: `./gradlew :core:audio:compileKotlinDesktop` → BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add core/audio/src/commonMain/composeResources/files/
git commit -m "feat(audio): add explosion/win/flag sound assets"
```

### Task 4.3: Platform `SoundPlayer` implementations + Koin

**Files:**
- Create: `core/audio/src/commonMain/kotlin/com/kotlearn/minesweeperk/core/audio/Module.kt`
- Create: `core/audio/src/androidMain/kotlin/com/kotlearn/minesweeperk/core/audio/Module.android.kt`
- Create: `core/audio/src/androidMain/kotlin/com/kotlearn/minesweeperk/core/audio/AndroidSoundPlayer.kt`
- Create: `core/audio/src/desktopMain/kotlin/com/kotlearn/minesweeperk/core/audio/Module.desktop.kt`
- Create: `core/audio/src/desktopMain/kotlin/com/kotlearn/minesweeperk/core/audio/DesktopSoundPlayer.kt`
- Create: `core/audio/src/iosMain/kotlin/com/kotlearn/minesweeperk/core/audio/Module.kt`
- Create: `core/audio/src/iosMain/kotlin/com/kotlearn/minesweeperk/core/audio/IosSoundPlayer.kt`

> All players share the strategy: lazily read each sound's bytes once via `Res.readBytes("files/<name>.wav")` (suspend) on an internal `CoroutineScope`, cache them, then play on demand. Reading is best-effort; a not-yet-loaded sound is skipped rather than blocking the UI.

- [ ] **Step 1: Common Koin wiring**

`Module.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import org.koin.core.module.Module
import org.koin.dsl.module

val audioModule = module {
    includes(platformAudioModule)
}

internal expect val platformAudioModule: Module
```

- [ ] **Step 2: Android implementation** (`SoundPool` + cached bytes → temp files)

`AndroidSoundPlayer.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import java.io.File

internal class AndroidSoundPlayer(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<GameSound, Int>()

    init {
        scope.launch {
            GameSound.entries.forEach { sound ->
                val bytes = Res.readBytes("files/${sound.fileName}")
                val file = File.createTempFile(sound.name, ".wav", context.cacheDir).apply {
                    writeBytes(bytes)
                    deleteOnExit()
                }
                soundIds[sound] = soundPool.load(file.absolutePath, 1)
            }
        }
    }

    override fun play(sound: GameSound) {
        val id = soundIds[sound] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }
}
```

Add the `fileName` helper to `GameSound.kt`:
```kotlin
    val fileName: String get() = when (this) {
        EXPLOSION -> "explosion.wav"
        WIN -> "win.wav"
        FLAG -> "flag.wav"
    }
```

`Module.android.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAudioModule: Module = module {
    single { AndroidSoundPlayer(context = androidContext()) } bind SoundPlayer::class
}
```

- [ ] **Step 3: Desktop implementation** (`javax.sound.sampled`)

`DesktopSoundPlayer.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import java.io.ByteArrayInputStream
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem

internal class DesktopSoundPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val cache = mutableMapOf<GameSound, ByteArray>()

    init {
        scope.launch {
            GameSound.entries.forEach { cache[it] = Res.readBytes("files/${it.fileName}") }
        }
    }

    override fun play(sound: GameSound) {
        val bytes = cache[sound] ?: return
        scope.launch {
            runCatching {
                val stream = AudioSystem.getAudioInputStream(BufferedInputStream(ByteArrayInputStream(bytes)))
                val clip = AudioSystem.getClip()
                clip.open(stream)
                clip.start()
            }
        }
    }
}
```

`Module.desktop.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAudioModule: Module = module {
    single { DesktopSoundPlayer() } bind SoundPlayer::class
}
```

- [ ] **Step 4: iOS implementation** (`AVAudioPlayer`)

`IosSoundPlayer.kt`:
```kotlin
package com.kotlearn.minesweeperk.core.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import minesweeperk.core.audio.generated.resources.Res
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class)
internal class IosSoundPlayer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
) : SoundPlayer {

    private val players = mutableMapOf<GameSound, AVAudioPlayer>()

    init {
        scope.launch {
            GameSound.entries.forEach { sound ->
                val bytes = Res.readBytes("files/${sound.fileName}")
                val data = bytes.usePinned { pinned ->
                    NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                }
                AVAudioPlayer(data = data, error = null).also {
                    it.prepareToPlay()
                    players[sound] = it
                }
            }
        }
    }

    override fun play(sound: GameSound) {
        players[sound]?.let { it.currentTime = 0.0; it.play() }
    }
}
```

`Module.kt` (iosMain):
```kotlin
package com.kotlearn.minesweeperk.core.audio

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal actual val platformAudioModule: Module = module {
    single { IosSoundPlayer() } bind SoundPlayer::class
}
```

- [ ] **Step 5: Compile all targets**

Run: `./gradlew :core:audio:compileKotlinDesktop :core:audio:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL. (iOS compiles during the full app build; fix cinterop signatures there if needed.)

- [ ] **Step 6: Commit**

```bash
git add core/audio/src
git commit -m "feat(audio): platform SoundPlayer implementations (Android/desktop/iOS)"
```

### Task 4.4: Trigger sounds from PlayViewModel (gated by preference)

**Files:**
- Modify: `feature/play/build.gradle.kts` (depend on `core:audio`)
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/Module.kt`
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt`
- Test: `feature/play/src/commonTest/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModelSoundTest.kt`

- [ ] **Step 1: Add the module dependency**

In `feature/play/build.gradle.kts`, add to `commonMain.dependencies`:
```kotlin
            implementation(projects.core.audio)
```
In `feature/play/.../Module.kt`, add `includes(audioModule)`:
```kotlin
import com.kotlearn.minesweeperk.core.audio.audioModule
// inside module { ... }
    includes(audioModule)
```

- [ ] **Step 2: Write the failing test** (fake SoundPlayer, assert transitions)

```kotlin
package com.kotlearn.minesweeperk.feature.play

import com.kotlearn.minesweeperk.core.audio.GameSound
import com.kotlearn.minesweeperk.core.audio.SoundPlayer
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeSoundPlayer : SoundPlayer {
    val played = mutableListOf<GameSound>()
    override fun play(sound: GameSound) { played += sound }
}

class PlayViewModelSoundTest {

    @Test
    fun plays_explosion_on_transition_to_lost_when_enabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.LOST)
        assertEquals(listOf(GameSound.EXPLOSION), player.played)
    }

    @Test
    fun plays_win_on_transition_to_won_when_enabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.WON)
        assertEquals(listOf(GameSound.WIN), player.played)
    }

    @Test
    fun plays_nothing_when_disabled() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = false, from = GameStatus.PLAYING, to = GameStatus.WON)
        assertEquals(emptyList(), player.played)
    }

    @Test
    fun plays_nothing_when_status_unchanged() {
        val player = FakeSoundPlayer()
        maybePlayTransitionSound(player, enabled = true, from = GameStatus.PLAYING, to = GameStatus.PLAYING)
        assertEquals(emptyList(), player.played)
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :feature:play:desktopTest --tests "*PlayViewModelSoundTest*"`
Expected: FAIL — `maybePlayTransitionSound` unresolved.

- [ ] **Step 4: Implement the helper + wire into ViewModel**

Add top-level helpers in `PlayViewModel.kt`:
```kotlin
import com.kotlearn.minesweeperk.core.audio.GameSound
import com.kotlearn.minesweeperk.core.audio.SoundPlayer

/** Plays the sound for a status transition, if any, respecting [enabled]. */
internal fun maybePlayTransitionSound(
    player: SoundPlayer,
    enabled: Boolean,
    from: GameStatus,
    to: GameStatus,
) {
    if (!enabled || from == to) return
    when (to) {
        GameStatus.LOST -> player.play(GameSound.EXPLOSION)
        GameStatus.WON -> player.play(GameSound.WIN)
        GameStatus.PLAYING -> Unit
    }
}
```

Add constructor params to `PlayViewModel`:
```kotlin
    private val soundPlayer: SoundPlayer,
    private val getSoundEnabledAsFlowUseCase: GetSoundEnabledAsFlowUseCase,
```
Track the enabled flag:
```kotlin
import com.kotlearn.minesweeperk.domain.settings.GetSoundEnabledAsFlowUseCase
// field:
    private val soundEnabled = getSoundEnabledAsFlowUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)
```
In `revealTile`, after computing `newState`, add the transition sound (reuse existing `previousStatus`):
```kotlin
        maybePlayTransitionSound(
            player = soundPlayer,
            enabled = soundEnabled.value,
            from = previousStatus,
            to = newState.status,
        )
```
In `toggleFlag`, play the flag sound when a flag is actually added or removed:
```kotlin
    fun toggleFlag(x: Int, y: Int) {
        val current = _gameState.value ?: return
        val newState = toggleFlagUseCase(current, x, y)
        if (soundEnabled.value && newState.flagCount != current.flagCount) {
            soundPlayer.play(GameSound.FLAG)
        }
        setGameState(newState)
    }
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :feature:play:desktopTest --tests "*PlayViewModelSoundTest*"`
Expected: PASS

- [ ] **Step 6: Verify audio on emulator** (per `verify` skill)

Build & install. Place a flag → hear the flag click. Hit a mine → explosion. Win a game → fanfare. Toggle sound off (after Phase 6 panel exists, or temporarily via a test) → silence.

- [ ] **Step 7: Commit**

```bash
git add feature/play/build.gradle.kts feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/Module.kt feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt feature/play/src/commonTest/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModelSoundTest.kt
git commit -m "feat(play): play sound effects on game transitions and flagging"
```

---

# Phase 5 — Fireworks win animation

Goal: a Compose-`Canvas` fireworks burst plays over the board on a win.

### Task 5.1: FireworksOverlay composable

**Files:**
- Create: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/FireworksOverlay.kt`

- [ ] **Step 1: Write the composable**

```kotlin
package com.kotlearn.minesweeperk.feature.play

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Burst(val cx: Float, val cy: Float, val delay: Float, val color: Color)

/** A looping fireworks animation drawn on a Canvas. Intended to be layered above
 *  the board while the game is won. Purely decorative; no state escapes. */
@Composable
internal fun FireworksOverlay(modifier: Modifier = Modifier) {
    val palette = remember {
        listOf(
            Color(0xFFEB392A), Color(0xFF377E22), Color(0xFF0000F5),
            Color(0xFFFFD23F), Color(0xFFFF7B00), Color(0xFF9B51E0),
        )
    }
    val bursts = remember {
        List(6) {
            Burst(
                cx = Random.nextFloat() * 0.8f + 0.1f,
                cy = Random.nextFloat() * 0.5f + 0.15f,
                delay = Random.nextFloat(),
                color = palette[it % palette.size],
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "fireworks")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "t",
    )
    val sparksPerBurst = 24

    Canvas(modifier = modifier.fillMaxSize()) {
        bursts.forEach { burst ->
            val local = ((t + burst.delay) % 1f)
            val radius = local * size.minDimension * 0.35f
            val alpha = (1f - local)
            val center = Offset(burst.cx * size.width, burst.cy * size.height)
            for (i in 0 until sparksPerBurst) {
                val angle = (i.toFloat() / sparksPerBurst) * (2f * kotlin.math.PI.toFloat())
                val p = Offset(center.x + cos(angle) * radius, center.y + sin(angle) * radius)
                drawCircle(
                    color = burst.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    radius = size.minDimension * 0.008f,
                    center = p,
                )
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/FireworksOverlay.kt
git commit -m "feat(play): add FireworksOverlay canvas animation"
```

### Task 5.2: Show fireworks on win

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt`

- [ ] **Step 1: Layer the overlay above the board when won**

In `PlayScreen`, inside the board `Box` (the one that already holds `GameEndOverlay`), add the fireworks **above** the board but the "You won!" panel stays on top (add fireworks before `GameEndOverlay` in the `Box` children so the panel renders last/on top):
```kotlin
                if (state != null && state.status == GameStatus.WON) {
                    FireworksOverlay(modifier = Modifier.fillMaxSize())
                }

                if (state != null) {
                    GameEndOverlay(
                        status = state.status,
                        elapsedSeconds = elapsedSeconds,
                        onRestart = viewModel::restart,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
```

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Verify on emulator** (per `verify` skill)

Win a game → fireworks burst behind the "You won!" panel + fanfare sound. Losing shows no fireworks.

- [ ] **Step 4: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt
git commit -m "feat(play): show fireworks celebration on win"
```

---

# Phase 6 — Inline quick-settings panel

Goal: a ⚙️ button on the play toolbar opens a liquid-glass panel to change board size, flag icon, mine icon, and sound on/off — the customization home.

### Task 6.1: Expose update actions + state from PlayViewModel

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt`

- [ ] **Step 1: Inject the update use cases and expose current state**

Add constructor params:
```kotlin
    private val updateBoardSizeUseCase: UpdateBoardSizeUseCase,
    private val updateFlagIconUseCase: UpdateFlagIconUseCase,
    private val updateMineIconUseCase: UpdateMineIconUseCase,
    private val updateSoundEnabledUseCase: UpdateSoundEnabledUseCase,
```
with imports for each. Expose the board size and sound-enabled as public UI state (the ViewModel already holds `soundEnabled` and computes `boardSize`; expose flows):
```kotlin
    val boardSizeState = getBoardSizeAsFlowUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BoardSize.DEFAULT)
    val soundEnabledState = soundEnabled // already a StateFlow<Boolean>
```
Add action methods:
```kotlin
    fun setBoardSize(columns: Int, rows: Int) {
        viewModelScope.launch { updateBoardSizeUseCase(BoardSize.of(columns, rows)) }
    }
    fun setFlagIcon(icon: FlagIcon) { viewModelScope.launch { updateFlagIconUseCase(icon) } }
    fun setMineIcon(icon: MineIcon) { viewModelScope.launch { updateMineIconUseCase(icon) } }
    fun setSoundEnabled(enabled: Boolean) { viewModelScope.launch { updateSoundEnabledUseCase(enabled) } }
```
Add imports:
```kotlin
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import com.kotlearn.minesweeperk.domain.settings.UpdateBoardSizeUseCase
import com.kotlearn.minesweeperk.domain.settings.UpdateFlagIconUseCase
import com.kotlearn.minesweeperk.domain.settings.UpdateMineIconUseCase
import com.kotlearn.minesweeperk.domain.settings.UpdateSoundEnabledUseCase
```

> Changing board size flows through settings → the existing `combine` collector in `init` detects `sizeChanged` and restarts the game automatically. No extra restart wiring needed.

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayViewModel.kt
git commit -m "feat(play): expose customization actions from PlayViewModel"
```

### Task 6.2: QuickSettingsPanel composable

**Files:**
- Create: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/QuickSettingsPanel.kt`

- [ ] **Step 1: Write the panel**

```kotlin
package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlearn.minesweeperk.domain.settings.BoardSize
import com.kotlearn.minesweeperk.domain.settings.FlagIcon
import com.kotlearn.minesweeperk.domain.settings.MineIcon
import com.kotlearn.minesweeperk.ui.core.LocalPadding
import com.kotlearn.minesweeperk.ui.core.glassContentColor
import com.kotlearn.minesweeperk.ui.core.liquidGlass

/** Inline customization panel shown over the play screen. Dismisses on scrim tap. */
@Composable
internal fun QuickSettingsPanel(
    boardSize: BoardSize,
    flagIcon: FlagIcon,
    mineIcon: MineIcon,
    soundEnabled: Boolean,
    onBoardSizeChange: (columns: Int, rows: Int) -> Unit,
    onFlagIconChange: (FlagIcon) -> Unit,
    onMineIconChange: (MineIcon) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(padding.normal),
            modifier = Modifier
                // Consume taps so clicks inside the sheet don't dismiss it.
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .fillMaxWidth()
                .liquidGlass(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(padding.large),
        ) {
            PanelLabel("Board size")
            BoardSizeStepper(boardSize = boardSize, onChange = onBoardSizeChange)

            PanelLabel("Flag icon")
            IconPickerRow(
                options = FlagIcon.entries,
                selected = flagIcon,
                onSelect = onFlagIconChange,
                render = { FlagTileIcon(icon = it, tint = Color(0xFFEB392A), emojiSize = 22.sp) },
            )

            PanelLabel("Mine icon")
            IconPickerRow(
                options = MineIcon.entries,
                selected = mineIcon,
                onSelect = onMineIconChange,
                render = { MineTileIcon(icon = it, tint = glassContentColor, emojiSize = 22.sp) },
            )

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Sound", color = glassContentColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Switch(checked = soundEnabled, onCheckedChange = onSoundEnabledChange)
            }
        }
    }
}

@Composable
private fun PanelLabel(text: String) {
    Text(text.uppercase(), color = glassContentColor.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun BoardSizeStepper(
    boardSize: BoardSize,
    onChange: (columns: Int, rows: Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Stepper(label = "Cols", value = boardSize.columns) { onChange(it, boardSize.rows) }
        Stepper(label = "Rows", value = boardSize.rows) { onChange(boardSize.columns, it) }
    }
}

@Composable
private fun Stepper(label: String, value: Int, onValue: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = glassContentColor)
        StepButton("–") { onValue((value - 1).coerceAtLeast(BoardSize.MIN_SIDE)) }
        Text("$value", color = glassContentColor, fontWeight = FontWeight.Bold)
        StepButton("+") { onValue((value + 1).coerceAtMost(BoardSize.MAX_SIDE)) }
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(32.dp).clip(shape).liquidGlass(shape = shape).clickable(onClick = onClick),
    ) {
        Text(symbol, color = glassContentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun <T> IconPickerRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    render: @Composable (T) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { option ->
            val shape = RoundedCornerShape(12.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(shape)
                    .liquidGlass(shape = shape, selected = option == selected)
                    .clickable { onSelect(option) },
            ) {
                Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) { render(option) }
            }
        }
    }
}
```

- [ ] **Step 2: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL. (If `liquidGlass(shape = ..., selected = ...)` or `RoundedCornerShape(topStart, topEnd)` signatures differ, adjust to the `liquidGlass` overload used elsewhere in `PlayScreen.kt`.)

- [ ] **Step 3: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/QuickSettingsPanel.kt
git commit -m "feat(play): add QuickSettingsPanel composable"
```

### Task 6.3: Wire the panel into PlayScreen with a ⚙️ toolbar button

**Files:**
- Modify: `feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt`

- [ ] **Step 1: Add panel-visibility state and collect the new flows**

At the top of `PlayScreen`:
```kotlin
    val boardSize by viewModel.boardSizeState.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabledState.collectAsStateWithLifecycle()
    var showPanel by rememberSaveable { mutableStateOf(false) }
```
Add imports:
```kotlin
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
```

- [ ] **Step 2: Add a ⚙️ button to `PlayToolbar`**

Give `PlayToolbar` an `onOpenSettings: () -> Unit` param and add a trailing gear button. After the title `Text(...)` in the toolbar `Row`, add:
```kotlin
            Spacer(modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .liquidGlass(shape = shape)
                    .clickable(onClick = onOpenSettings),
            ) {
                Text(text = "⚙️", fontSize = 22.sp)
            }
```
Update the call site: `PlayToolbar(onNavigateBack = onNavigateBack, onOpenSettings = { showPanel = true })`.

- [ ] **Step 3: Render the panel as a top-level overlay**

Wrap the outermost `Box` content so the panel sits above everything. At the end of the outer `Box(...)` in `PlayScreen` (after the main `Column`), add:
```kotlin
        if (showPanel) {
            QuickSettingsPanel(
                boardSize = boardSize,
                flagIcon = iconPreferences.flag,
                mineIcon = iconPreferences.mine,
                soundEnabled = soundEnabled,
                onBoardSizeChange = viewModel::setBoardSize,
                onFlagIconChange = viewModel::setFlagIcon,
                onMineIconChange = viewModel::setMineIcon,
                onSoundEnabledChange = viewModel::setSoundEnabled,
                onDismiss = { showPanel = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
```

- [ ] **Step 4: Compile**

Run: `./gradlew :feature:play:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Verify end-to-end on emulator** (per `verify` skill)

Open Play → tap ⚙️ → panel slides up. Change Cols/Rows → panel dismiss/game restarts at new size. Pick 🏴 flag and ☠️ mine → board reflects them live. Toggle Sound off → no sounds; on → sounds return. Kill & relaunch app → preferences persist.

- [ ] **Step 6: Commit**

```bash
git add feature/play/src/commonMain/kotlin/com/kotlearn/minesweeperk/feature/play/PlayScreen.kt
git commit -m "feat(play): inline quick-settings panel for board size, icons, and sound"
```

### Task 6.4: Full regression + wrap-up

- [ ] **Step 1: Run the whole test suite**

Run: `./gradlew :domain:settings:desktopTest :feature:play:desktopTest`
Expected: All PASS.

- [ ] **Step 2: Full app build for Android**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Final emulator smoke test** covering board size, icons, sounds (all three), fireworks, and persistence across process death.

- [ ] **Step 4: Commit any final fixes**, then the branch is ready for PR.

---

## Self-Review notes (spec coverage)

- Board size (explicit grid) → Phase 1 (model/persist), Phase 3 (ViewModel/screen), Phase 6 (control). ✓
- Icons (emoji + vector, player-selectable) → Phase 1 (enums), Phase 2 (render), Phase 6 (pickers). ✓
- Sounds (explosion/win/flag, all platforms, gated by toggle) → Phase 4. ✓
- Fireworks win animation → Phase 5. ✓
- Sound on/off toggle → Phase 1 (persist), Phase 6 (Switch). ✓
- Inline controls home → Phase 6. ✓
- Testing (mine-count, icon mapping, sound-trigger, settings round-trip) → Tasks 1.1, 1.2, 1.6, 3.1, 4.4. ✓
- Out-of-scope items (haptics, tile-click sound, custom assets, board scrolling) → not planned, per spec. ✓

## Known verification-dependent assumptions (fix during execution if they don't hold)

- Exact `koin.compose` bundle name for a non-feature module (Task 4.1) — confirm in `gradle/libs.versions.toml`.
- `liquidGlass` overloads / `glassContentColor` usage in the panel (Task 6.2) — match the signatures already used in `PlayScreen.kt`/`GameEndOverlay.kt`.
- iOS `AVAudioPlayer` cinterop signatures (Task 4.3) — adjust during the iOS compile.
- WAV format compatibility with `javax.sound.sampled` (desktop) — if a sourced file fails to open, re-encode as PCM WAV.
