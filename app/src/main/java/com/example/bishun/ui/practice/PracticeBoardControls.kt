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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.bishun.data.hsk.HskEntry
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.PracticeState
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
            description = "Start practice",
            onClick = onStartPractice,
            enabled = !practiceState.isActive && !isDemoPlaying,
            buttonSize = boardButtonSize,
        )
        IconActionButton(
            icon = Icons.Filled.Info,
            description = "Hint",
            onClick = onRequestHint,
            enabled = practiceState.isActive && !isDemoPlaying,
            buttonSize = boardButtonSize,
        )
        courseSession?.let { session ->
            IconActionButton(
                icon = Icons.Filled.ChevronLeft,
                description = "Previous word",
                onClick = onCoursePrev,
                enabled = session.hasPrevious,
                buttonSize = boardButtonSize,
            )
            IconActionButton(
                icon = Icons.Filled.ChevronRight,
                description = "Next word",
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
            buttonSize = boardButtonSize,
        )
        if (showHskIcon && hskEntry != null) {
            IconActionButton(
                icon = Icons.Filled.School,
                description = "HSK info",
                onClick = onHskInfoClick,
                buttonSize = boardButtonSize,
            )
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
    buttonSize: Dp = 40.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconActionButton(
            icon = Icons.Filled.Settings,
            description = "Canvas settings",
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
