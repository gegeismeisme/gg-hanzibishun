package com.example.bishun.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bishun.data.hsk.HskEntry
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.PracticeState
import com.example.bishun.ui.character.PracticeBoardStrings
import com.example.bishun.ui.character.components.IconActionButton

@Composable
fun PracticeBoardControls(
    practiceState: PracticeState,
    isDemoPlaying: Boolean,
    courseSession: CourseSession?,
    gridMode: PracticeGrid,
    currentColorOption: StrokeColorOption,
    showTemplate: Boolean,
    showHskIcon: Boolean,
    hskEntry: HskEntry?,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onCoursePrev: () -> Unit,
    onCourseNext: () -> Unit,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggle: (Boolean) -> Unit,
    onHskInfoClick: () -> Unit,
    wordInfoEnabled: Boolean,
    onShowWordInfo: () -> Unit,
    pronunciationEnabled: Boolean,
    onPlayPronunciation: () -> Unit,
    boardStrings: PracticeBoardStrings,
    modifier: Modifier = Modifier,
) {
    val boardButtonSize = 32.dp
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconActionButton(
            icon = Icons.Filled.Create,
            description = boardStrings.startLabel,
            onClick = onStartPractice,
            enabled = !practiceState.isActive && !isDemoPlaying,
            buttonSize = boardButtonSize,
        )
        IconActionButton(
            icon = Icons.Filled.Info,
            description = boardStrings.hintLabel,
            onClick = onRequestHint,
            enabled = practiceState.isActive && !isDemoPlaying,
            buttonSize = boardButtonSize,
        )
        courseSession?.let { session ->
            IconActionButton(
                icon = Icons.Filled.ChevronLeft,
                description = boardStrings.previousLabel,
                onClick = onCoursePrev,
                enabled = session.hasPrevious,
                buttonSize = boardButtonSize,
            )
            IconActionButton(
                icon = Icons.Filled.ChevronRight,
                description = boardStrings.nextLabel,
                onClick = onCourseNext,
                enabled = session.hasNext,
                buttonSize = boardButtonSize,
            )
        }
        CanvasSettingsMenu(
            currentGrid = gridMode,
            currentColor = currentColorOption,
            showTemplate = showTemplate,
            onGridChange = onGridModeChange,
            onColorChange = onStrokeColorChange,
            onTemplateToggle = onTemplateToggle,
            description = boardStrings.settingsLabel,
            buttonSize = boardButtonSize,
        )
        IconActionButton(
            icon = Icons.AutoMirrored.Filled.VolumeUp,
            description = boardStrings.pronunciationLabel,
            onClick = onPlayPronunciation,
            enabled = pronunciationEnabled,
            buttonSize = boardButtonSize,
        )
        GlyphActionButton(
            label = "释",
            description = boardStrings.dictionaryLabel,
            onClick = onShowWordInfo,
            enabled = wordInfoEnabled,
            buttonSize = boardButtonSize,
        )
        if (showHskIcon && hskEntry != null) {
            IconActionButton(
                icon = Icons.Filled.School,
                description = boardStrings.hskLabel,
                onClick = onHskInfoClick,
                buttonSize = boardButtonSize,
            )
        }
    }
}

@Composable
private fun GlyphActionButton(
    label: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean,
    buttonSize: Dp,
) {
    Surface(
        shape = CircleShape,
        tonalElevation = if (enabled) 4.dp else 0.dp,
        color = if (enabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(buttonSize)
            .semantics { this.contentDescription = description },
        onClick = onClick,
        enabled = enabled,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun CanvasSettingsMenu(
    currentGrid: PracticeGrid,
    currentColor: StrokeColorOption,
    showTemplate: Boolean,
    onGridChange: (PracticeGrid) -> Unit,
    onColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggle: (Boolean) -> Unit,
    description: String,
    buttonSize: Dp = 40.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconActionButton(
            icon = Icons.Filled.Settings,
            description = description,
            onClick = { expanded = true },
            buttonSize = buttonSize,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                text = "Grid",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PracticeGrid.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onGridChange(mode)
                        expanded = false
                    },
                    trailingIcon = if (mode == currentGrid) {
                        { Text("*") }
                    } else null,
                )
            }
            Text(
                text = "Stroke color",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            StrokeColorOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(option.color),
                            )
                            Text(option.label)
                        }
                    },
                    onClick = {
                        onColorChange(option)
                        expanded = false
                    },
                    trailingIcon = if (option == currentColor) {
                        { Text("*") }
                    } else null,
                )
            }
            DropdownMenuItem(
                text = { Text(if (showTemplate) "Hide calligraphy template" else "Show calligraphy template") },
                onClick = {
                    onTemplateToggle(!showTemplate)
                    expanded = false
                },
            )
        }
    }
}
