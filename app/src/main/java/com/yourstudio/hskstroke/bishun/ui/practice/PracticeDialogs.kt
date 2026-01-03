package com.yourstudio.hskstroke.bishun.ui.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.ui.character.WordInfoUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordInfoBottomSheet(
    symbol: String,
    entry: WordEntry?,
    wordInfoUiState: WordInfoUiState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    ttsController: TextToSpeechController,
) {
    val scrollState = rememberScrollState()
    val speakingAlpha by animateFloatAsState(
        targetValue = if (ttsController.isSpeaking.value) 1f else 0.5f,
        label = "ttsAlpha",
    )
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val closeSheet: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = closeSheet,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry?.word ?: symbol,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = entry?.pinyin?.ifBlank { "Pinyin..." } ?: "Pinyin...",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { ttsController.speak(entry?.word ?: symbol) },
                    enabled = ttsController.isAvailable.value,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Play pronunciation",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                    )
                }
            }
            when (wordInfoUiState) {
                WordInfoUiState.Loading,
                WordInfoUiState.Idle -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                        Text(
                            text = "Loading dictionary entry…",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                is WordInfoUiState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = wordInfoUiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                        )
                        TextButton(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                WordInfoUiState.NotFound -> {
                    Text(
                        text = "No dictionary entry available.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                WordInfoUiState.Loaded -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .verticalScroll(scrollState),
                    ) {
                        WordInfoStat("Radical", entry?.radicals.orEmpty())
                        WordInfoStat("Strokes", entry?.strokes.orEmpty())
                        WordInfoStat("Variant", entry?.oldword.orEmpty())
                        Text(
                            text = "Meaning",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        val content = entry?.explanation
                            ?.normalizeWhitespace()
                            .orEmpty()
                            .ifBlank {
                                entry?.more?.normalizeWhitespace().orEmpty()
                            }
                            .ifBlank { "Meaning unavailable." }
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = closeSheet) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun HskInfoDialog(
    entry: HskEntry,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("HSK ${entry.level}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                HskInfoRow("Writing level", entry.writingLevel?.toString() ?: "N/A")
                HskInfoRow("Traditional", entry.traditional.ifBlank { entry.symbol })
                HskInfoRow("Frequency", entry.frequency?.toString() ?: "N/A")
                Text(
                    text = "Examples",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = entry.examples.normalizeWhitespace().ifBlank { "No examples available." },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun HskInfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun WordInfoStat(label: String, value: String) {
    if (value.isBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
        )
    }
}

private fun String.normalizeWhitespace(): String = replace("\\s+".toRegex(), " ").trim()
