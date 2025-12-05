package com.yourstudio.hskstroke.bishun.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.ui.character.CharacterUiState
import com.yourstudio.hskstroke.bishun.ui.character.DemoState
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.components.IconActionButton

@Composable
fun SearchBarRow(
    query: String,
    uiState: CharacterUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    demoState: DemoState,
    isPracticeActive: Boolean,
    calligraphyDemoState: CalligraphyDemoState,
    useCalligraphyDemo: Boolean,
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
    onPlayCalligraphyDemoOnce: () -> Unit,
    onPlayCalligraphyDemoLoop: () -> Unit,
    onStopCalligraphyDemo: () -> Unit,
    strings: LocalizedStrings,
    languageOverride: String?,
    onLanguageChange: (String?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            LanguageMenu(
                currentTag = languageOverride,
                onLanguageChange = onLanguageChange,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(strings.searchLabel) },
                placeholder = { Text("\u6c38") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                textStyle = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .width(92.dp)
                    .heightIn(min = 56.dp),
            )
            IconActionButton(
                icon = Icons.Filled.CloudDownload,
                description = strings.loadButton,
                onClick = onSubmit,
                enabled = query.isNotBlank(),
                buttonSize = 32.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Clear,
                description = strings.clearButton,
                onClick = onClearQuery,
                enabled = query.isNotEmpty(),
                buttonSize = 32.dp,
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            DemoControlRow(
                uiState = uiState,
                demoState = demoState,
                calligraphyDemoState = calligraphyDemoState,
                useCalligraphyDemo = useCalligraphyDemo,
                practiceActive = isPracticeActive,
                onSubmit = onSubmit,
                onPlayOnce = onPlayDemoOnce,
                onPlayLoop = onPlayDemoLoop,
                onStop = onStopDemo,
                onPlayCalligraphyOnce = onPlayCalligraphyDemoOnce,
                onPlayCalligraphyLoop = onPlayCalligraphyDemoLoop,
                onStopCalligraphy = onStopCalligraphyDemo,
            )
        }
    }
}

@Composable
private fun DemoControlRow(
    uiState: CharacterUiState,
    demoState: DemoState,
    calligraphyDemoState: CalligraphyDemoState,
    useCalligraphyDemo: Boolean,
    practiceActive: Boolean,
    onSubmit: () -> Unit,
    onPlayOnce: () -> Unit,
    onPlayLoop: () -> Unit,
    onStop: () -> Unit,
    onPlayCalligraphyOnce: () -> Unit,
    onPlayCalligraphyLoop: () -> Unit,
    onStopCalligraphy: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isPlaying = if (useCalligraphyDemo) calligraphyDemoState.isPlaying else demoState.isPlaying
        if (isPlaying) {
            val stopHandler = if (useCalligraphyDemo) onStopCalligraphy else onStop
            IconActionButton(
                icon = Icons.Filled.Stop,
                description = "Stop demo",
                onClick = stopHandler,
                buttonSize = 36.dp,
            )
        } else {
            val playOnce = if (useCalligraphyDemo) {
                onPlayCalligraphyOnce
            } else {
                {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayOnce()
                    }
                }
            }
            val playLoop = if (useCalligraphyDemo) {
                onPlayCalligraphyLoop
            } else {
                {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayLoop()
                    }
                }
            }
            IconActionButton(
                icon = Icons.Filled.PlayArrow,
                description = "Play demo",
                onClick = playOnce,
                enabled = !practiceActive,
                buttonSize = 36.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Refresh,
                description = "Loop demo",
                onClick = playLoop,
                enabled = !practiceActive,
                buttonSize = 36.dp,
            )
        }
    }
}

@Composable
private fun LanguageMenu(
    currentTag: String?,
    onLanguageChange: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentChoice = languageChoices.firstOrNull { it.tag == currentTag } ?: languageChoices.first()
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = "Select language",
            )
            Text(
                text = currentChoice.label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languageChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice.label) },
                    onClick = {
                        expanded = false
                        onLanguageChange(choice.tag)
                    },
                )
            }
        }
    }
}

private data class LanguageChoice(val tag: String?, val label: String)

private val languageChoices = listOf(
    LanguageChoice(null, "System"),
    LanguageChoice("en", "English"),
    LanguageChoice("es", "Español"),
    LanguageChoice("ja", "日本語"),
)



