package com.kotlearn.minesweeperk.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kotlearn.minesweeperk.domain.settings.Difficulty
import com.kotlearn.minesweeperk.ui.core.LocalDimensions
import com.kotlearn.minesweeperk.ui.core.LocalPadding
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val username by viewModel.username.collectAsStateWithLifecycle()
    val difficulty by viewModel.difficulty.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    SettingsContent(
        username = username,
        selectedDifficulty = difficulty,
        isSaving = isSaving,
        onUsernameChange = viewModel::setUsername,
        onDifficultySelected = viewModel::setDifficulty,
        onSave = viewModel::save,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    username: String,
    selectedDifficulty: Difficulty,
    isSaving: Boolean,
    onUsernameChange: (String) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    val dimensions = LocalDimensions.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(LiquidGlass.backgroundBrush),
    ) {
        CompositionLocalProvider(LocalContentColor provides glassContentColor) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(padding.normal),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding.large)
                    .widthIn(max = dimensions.maxWidthSmall)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "Settings",
                    color = glassContentColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )

                DifficultySection(
                    selectedDifficulty = selectedDifficulty,
                    onDifficultySelected = onDifficultySelected,
                )

                UsernameSection(
                    username = username,
                    isSaving = isSaving,
                    onUsernameChange = onUsernameChange,
                    onSave = onSave,
                )
            }
        }
    }
}

@Composable
private fun DifficultySection(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    Column(
        verticalArrangement = Arrangement.spacedBy(padding.small),
        modifier = modifier
            .liquidGlass(shape = RoundedCornerShape(24.dp))
            .padding(padding.normal)
            .fillMaxWidth(),
    ) {
        SectionLabel(text = "Difficulty")
        Difficulty.entries.forEach { difficulty ->
            DifficultyOption(
                difficulty = difficulty,
                selected = difficulty == selectedDifficulty,
                onClick = { onDifficultySelected(difficulty) },
            )
        }
    }
}

@Composable
private fun DifficultyOption(
    difficulty: Difficulty,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .liquidGlass(shape = shape, selected = selected)
            .padding(horizontal = padding.normal, vertical = padding.small),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = difficulty.label,
                color = glassContentColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "${difficulty.width} × ${difficulty.height} · ${difficulty.mineCount} mines",
                color = glassContentColor.copy(alpha = 0.75f),
                fontSize = 13.sp,
            )
        }
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = if (selected) "Selected" else null,
            tint = Color.White,
            modifier = Modifier.alpha(if (selected) 1f else 0f),
        )
    }
}

@Composable
private fun UsernameSection(
    username: String,
    isSaving: Boolean,
    onUsernameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val padding = LocalPadding.current
    Column(
        verticalArrangement = Arrangement.spacedBy(padding.small),
        modifier = modifier
            .liquidGlass(shape = RoundedCornerShape(24.dp))
            .padding(padding.normal)
            .fillMaxWidth(),
    ) {
        SectionLabel(text = "Username")
        TextField(
            value = username,
            onValueChange = onUsernameChange,
            singleLine = true,
            enabled = !isSaving,
            keyboardOptions = KeyboardOptions.Default,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White.copy(alpha = 0.12f),
                textColor = glassContentColor,
                cursorColor = LiquidGlass.accent,
                focusedIndicatorColor = LiquidGlass.accent,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(18.dp))
                .liquidGlass(shape = RoundedCornerShape(18.dp), selected = true)
                .clickable(enabled = !isSaving, onClick = onSave),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = "Save",
                    color = glassContentColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = glassContentColor.copy(alpha = 0.7f),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Preview
@Composable
private fun SettingsContentPreview() {
    SettingsContent(
        username = "Anonymous",
        selectedDifficulty = Difficulty.HARD,
        isSaving = false,
        onUsernameChange = {},
        onDifficultySelected = {},
        onSave = {},
        modifier = Modifier.fillMaxSize(),
    )
}