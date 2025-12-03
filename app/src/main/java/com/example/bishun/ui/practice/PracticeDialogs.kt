package com.example.bishun.ui.practice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bishun.R
import com.example.bishun.data.hsk.HskEntry
import com.example.bishun.data.word.WordEntry
import com.example.bishun.hanzi.model.CharacterDefinition

@Composable
fun CharacterInfoPanel(
    definition: CharacterDefinition,
    wordEntry: WordEntry?,
    onWordInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterGlyphWithGrid(
                symbol = definition.symbol,
                modifier = Modifier.size(120.dp),
            )
            if (wordEntry != null) {
                WordInfoPreview(
                    entry = wordEntry,
                    onClick = onWordInfoClick,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    text = "Info...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun WordInfoPreview(
    entry: WordEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEDE6F5),
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = entry.pinyin.ifBlank { "Pinyin..." },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${entry.radicals.ifBlank { "Rad." }} / ${entry.strokes.ifBlank { "?" }} strokes...",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.explanation.condense(40),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun WordInfoDialog(
    entry: WordEntry,
    onDismiss: () -> Unit,
    ttsController: TextToSpeechController,
) {
    val scrollState = rememberScrollState()
    val speakingAlpha by animateFloatAsState(
        targetValue = if (ttsController.isSpeaking.value) 1f else 0.5f,
        label = "speakingAlpha",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.word,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = entry.pinyin.ifBlank { "Pinyin..." },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                IconButton(
                    onClick = { ttsController.speak(entry.word) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaker",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState),
            ) {
                WordInfoStat("Radical", entry.radicals)
                WordInfoStat("Strokes", entry.strokes)
                WordInfoStat("Variant", entry.oldword)
                Text(
                    text = "Meaning",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = entry.explanation.normalizeWhitespace().ifBlank { "Meaning unavailable." },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
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
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun CharacterGlyphWithGrid(symbol: String, modifier: Modifier = Modifier) {
    val gridColor = Color(0xFFD8CCC2)
    val borderColor = Color(0xFFE7DCD3)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF7F2EE)),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = borderColor,
                cornerRadius = CornerRadius(20.dp.toPx()),
                style = Stroke(width = 2.dp.toPx()),
            )
            val width = size.width
            val height = size.height
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            val strokeWidth = 1.5.dp.toPx()
            drawLine(gridColor, Offset(halfWidth, 0f), Offset(halfWidth, height), strokeWidth)
            drawLine(gridColor, Offset(0f, halfHeight), Offset(width, halfHeight), strokeWidth)
            drawLine(gridColor, Offset(0f, 0f), Offset(width, height), strokeWidth)
            drawLine(gridColor, Offset(width, 0f), Offset(0f, height), strokeWidth)
        }
        Text(
            text = symbol,
            fontFamily = KaishuFontFamily,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
            color = Color(0xFF1F1F1F),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

private val KaishuFontFamily = FontFamily(Font(R.font.ar_pl_kaiti_m_gb))

private fun String.condense(maxChars: Int): String {
    val cleaned = replace("\\s+".toRegex(), " ").trim()
    if (cleaned.isEmpty()) return "..."
    if (cleaned.length <= maxChars) return cleaned
    return cleaned.take(maxChars).trimEnd() + "..."
}

private fun String.normalizeWhitespace(): String = replace("\\s+".toRegex(), " ").trim()
