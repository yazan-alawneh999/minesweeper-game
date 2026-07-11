---
name: verify
description: How to run and drive MinesweeperK to verify changes end-to-end
---

# Verifying MinesweeperK

Compose Multiplatform app (android / desktop / ios). The reliable verification
surface is the **Android emulator via adb** — driving the desktop window on
macOS is blocked (no assistive access for osascript clicks, no screen
recording permission for screencapture/CGWindowList).

## Recipe

```bash
export PATH="$PATH:$HOME/Library/Android/sdk/platform-tools:$HOME/Library/Android/sdk/emulator"
adb devices                                    # an emulator is usually already running
./gradlew :composeApp:installDebug
adb shell am force-stop com.kotlearn.minesweeperk
adb shell am start -n com.kotlearn.minesweeperk/.MainActivity
adb exec-out screencap -p > shot.png           # screenshot (Read the png to view)
adb shell input tap X Y                        # tap
adb shell input swipe X Y X Y 700              # long-press (flag a tile)
```

Screen is 1344x2992 (Pixel 10 Pro XL AVD). Screenshots read back scaled —
multiply displayed coordinates by the factor noted under the image.

## Gotchas

- A stale `androidx.compose.ui.tooling.PreviewActivity` from
  `com.kotlearn.minesweeperk.feature.play.test` may sit on top; force-stop
  both packages before screenshotting.
- Menu buttons sit mid-screen: Play ≈ (672, 1353) on the menu.
- Compile/test the touched modules directly (`:feature:play:desktopTest`,
  `:domain:game:desktopTest`) — `:composeApp` may be broken by in-progress
  IDE edits.
