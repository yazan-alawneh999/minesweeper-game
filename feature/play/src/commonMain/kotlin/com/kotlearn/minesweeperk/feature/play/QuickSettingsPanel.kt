package com.kotlearn.minesweeperk.feature.play

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    onReset: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Customize",
                    color = glassContentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                )
                ResetButton(onClick = onReset)
            }

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
private fun ResetButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .liquidGlass(shape = shape, selected = true)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = "Reset", color = glassContentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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
