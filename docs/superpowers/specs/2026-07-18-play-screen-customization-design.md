# Play Screen Customization & Engagement — Design

**Date:** 2026-07-18
**Status:** Approved (pending spec review)
**Feature branch:** `add-customize-play-screen`

## Goal

Make the Minesweeper play screen more customizable and engaging:

- Player-configurable **board size** (explicit columns × rows).
- Player-selectable **flag icon** and **mine icon** (mix of emoji + vector).
- **Sound effects** on mine explosion (loss), win, and flag toggle — on all platforms.
- A **fireworks** win celebration animation.
- A master **sound on/off** toggle.
- All customization controls surfaced via an **inline quick-settings panel** on the play screen.

Target platforms: Android, iOS, Desktop (Kotlin Multiplatform + Compose).

## Decisions (from brainstorming)

| Topic | Decision |
|---|---|
| Board sizing | Explicit grid size (columns × rows), tiles scale to fit the panel |
| Icons | Mixed set — emoji + vector options; player picks flag icon and mine icon independently |
| Sound events | Mine explosion (= loss), win fanfare, flag place/remove |
| Sound platforms | All platforms now, via `expect`/`actual` |
| Sound toggle | Yes — master on/off, persisted |
| Win celebration | Fireworks, drawn in Compose (works on all platforms) |
| Controls home | Inline quick-settings panel on the Play screen |
| Audio implementation | Custom `expect`/`actual` `SoundPlayer` (no third-party audio dependency) |

## Architecture

Follows the existing `domain` / `data` / `feature` / `ui-core` module split.

### New module: `core/audio`
- `SoundPlayer` interface in `commonMain`:
  ```kotlin
  interface SoundPlayer {
      fun play(sound: GameSound)
  }
  enum class GameSound { EXPLOSION, WIN, FLAG }
  ```
- `expect`/`actual` platform implementations:
  - **Android** — `SoundPool` (low-latency short SFX).
  - **iOS** — `AVAudioPlayer` (via platform APIs).
  - **Desktop (JVM)** — `javax.sound.sampled` `Clip`.
- Bundled short sound assets (explosion, win, flag) as module resources.
- The `SoundPlayer` itself is unconditional; suppression when sound is disabled is handled at the call site (`PlayViewModel` checks the `soundEnabled` flow before calling `play`). This keeps the interface simple and the gating unit-testable.

### `data/settings` (extend existing DataStore repo)
New persisted preferences, same pattern as `difficulty` / `username`:
- `boardColumns: Int`
- `boardRows: Int`
- `flagIcon: String` (enum name)
- `mineIcon: String` (enum name)
- `soundEnabled: Boolean`

Sensible defaults: `boardColumns = 10`, `boardRows = 16`, default flag/mine icons, `soundEnabled = true`.

### `domain/settings`
For each new preference, a `Get…AsFlowUseCase` and an `Update…UseCase`, mirroring `GetDifficultyAsFlowUseCase` / `UpdateDifficultyUseCase`.

### `feature/play`
- Inline quick-settings panel (board size, flag icon, mine icon, sound toggle).
- `TileIcon` composable mapping icon enums → rendering.
- `FireworksOverlay` composable.
- Wiring of game state transitions → `SoundPlayer`.
- `PlayViewModel` changes for explicit board dimensions.

## Component & behavior detail

### 1. Board size (explicit grid)
- Board dimensions come from the persisted `boardColumns` / `boardRows`, **not** from screen measurement. This removes the current `onBoardMeasured` auto-sizing path and simplifies rotation handling — dimensions no longer depend on the screen, so a rotation cannot change them.
- Control in the panel: presets **Small 9×9 · Medium 12×16 · Large 16×20**, plus +/− steppers, bounded ~5–30 per side.
- Mine count = `round(cols × rows × difficulty.minePercent)`. Difficulty (mine density) and board size (dimensions) remain orthogonal; the existing Settings difficulty control is untouched.
- Tiles scale to fit the panel using the existing `minOf(tileWidth, tileHeight)` logic in `MinesweeperBoard`; the size bounds prevent tiles from becoming too small.
- **Changing board size starts a fresh game** (dimensions can't change on an in-progress board). Changing icons or sound is applied live with no reset.
- SavedState still persists the in-progress game across process death; on restore, the game's own stored dimensions win (an in-progress game is never resized under the player).

### 2. Icons
- `FlagIcon` enum: `RED_FLAG` (🚩), `BLACK_FLAG` (🏴), `PIN` (📍) as emoji; `MATERIAL_FILLED`, `MATERIAL_OUTLINED` as vector.
- `MineIcon` enum: `BOMB` (💣), `EXPLOSION` (💥), `SKULL` (☠️) as emoji; `CLASSIC_DOT` (filled circle), `SPIKED` (existing `Res.drawable.mine`) as vector.
- Each enum carries enough metadata (emoji string or vector/painter reference) for `TileIcon` to render it.
- Persisted as enum name (like `difficulty`).
- `TileIcon` composable: emoji rendered as `Text`, vector rendered as `Icon` (vector or `painterResource`).
- `MinesweeperBoard` accepts the selected `flagIcon` / `mineIcon` and renders accordingly (replacing the hardcoded `Icons.Filled.Flag` and `Res.drawable.mine`).

### 3. Inline quick-settings panel
- A ⚙️ button added to `PlayToolbar`.
- Opens a liquid-glass panel (bottom-sheet style) over the play screen, styled consistently with existing `liquidGlass` surfaces.
- Contents: Board size control, Flag icon picker (row of tappable swatches), Mine icon picker, Sound on/off toggle.
- Selections write straight through to settings use cases; icon/sound changes reflect immediately, board-size changes trigger a new game.

### 4. Sounds
- `SoundPlayer` injected into `PlayViewModel` (with the `soundEnabled` flow).
- Triggers on state transitions:
  - reveal → `LOST`: `GameSound.EXPLOSION`
  - reveal → `WON`: `GameSound.WIN`
  - flag toggled: `GameSound.FLAG`
- When `soundEnabled` is false, playback is suppressed.

### 5. Fireworks
- `FireworksOverlay`: a Compose `Canvas` particle animation (radiating spark bursts) shown when `status == GameStatus.WON`.
- Plays for a few seconds, layered above the board alongside the existing `GameEndOverlay` "You won!" panel.
- Self-contained animation state; no domain changes.

## Data flow

```
Settings panel (Play screen)
   -> Update…UseCase -> data/settings DataStore
        -> Get…AsFlowUseCase -> PlayViewModel / MinesweeperBoard (icons, dims, sound flag)

revealTile / toggleFlag (PlayViewModel)
   -> GameState transition
        -> SoundPlayer.play(...)         (gated by soundEnabled)
        -> status == WON  -> FireworksOverlay + GameEndOverlay
        -> status == LOST -> GameEndOverlay
```

## Testing (TDD, matching existing test style)

Unit tests (commonTest):
- Mine-count-from-dimensions: `round(cols × rows × minePercent)` across sizes/difficulties.
- Icon enum → render mapping (each `FlagIcon` / `MineIcon` resolves to the expected render kind).
- Sound-trigger logic: a fake `SoundPlayer` asserts the correct `GameSound` fires on each transition, and that nothing fires when `soundEnabled` is false.
- Settings read/write for the new preferences (defaults + round-trip).

Verified live on the Android emulator (per project `verify` skill):
- Platform `SoundPlayer` implementations (actual audio playback).
- Fireworks animation.
- Inline panel interaction and board-size restart behavior.

## Out of scope (YAGNI)

- Haptic feedback.
- Per-difficulty or per-theme icon bundles beyond the flag/mine pickers.
- Tile-reveal click sound and background music (deliberately excluded to avoid noise).
- Custom user-supplied icons/sounds (only the curated preset sets).
- Board scrolling for oversized grids — size bounds keep the board fit-to-screen instead.
