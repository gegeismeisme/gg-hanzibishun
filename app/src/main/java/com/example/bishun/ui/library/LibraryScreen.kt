package com.example.bishun.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bishun.data.word.WordEntry

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    onLoadInPractice: (String) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Dictionary",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Look up pinyin, radicals, and quick definitions entirely offline. Enter a single character and jump straight back to Practice when you're ready to trace.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            label = { Text("Chinese character") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("Tip: type one character (HSK 1 is unlocked by default).") },
        )
        QuickSuggestionsRow(onSuggestionClick = viewModel::loadCharacter)
        RowActions(
            isLoading = uiState.isLoading,
            hasResult = uiState.result != null,
            onSearch = viewModel::submitQuery,
            onClear = viewModel::clearResult,
        )
        if (uiState.recentSearches.isNotEmpty()) {
            RecentSearchesRow(
                recent = uiState.recentSearches,
                onSelect = viewModel::loadCharacter,
                onClear = viewModel::clearHistory,
            )
        }
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        uiState.result?.let { entry ->
            WordEntryCard(
                entry = entry,
                onPractice = { onLoadInPractice(entry.word) },
            )
        }
        HelpCard()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSuggestionsRow(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf("永", "你", "学", "心", "写")
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Try:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        suggestions.forEach { symbol ->
            OutlinedButton(onClick = { onSuggestionClick(symbol) }) {
                Text(symbol)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesRow(
    recent: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent searches",
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(onClick = onClear) {
                Text("Clear")
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            recent.forEach { symbol ->
                OutlinedButton(onClick = { onSelect(symbol) }) {
                    Text(symbol)
                }
            }
        }
    }
}

@Composable
private fun RowActions(
    isLoading: Boolean,
    hasResult: Boolean,
    onSearch: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(onClick = onSearch, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp),
                    strokeWidth = 2.dp,
                )
            }
            Text(if (isLoading) "Searching..." else "Lookup")
        }
        OutlinedButton(onClick = onClear, enabled = hasResult) {
            Text("Clear result")
        }
    }
}

@Composable
private fun WordEntryCard(
    entry: WordEntry,
    onPractice: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = entry.word,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text("Pinyin · ${entry.pinyin.ifBlank { "N/A" }}")
            Text("Radical · ${entry.radicals.ifBlank { "N/A" }}    Strokes · ${entry.strokes.ifBlank { "?" }}")
            entry.oldword.takeIf { it.isNotBlank() }?.let {
                Text("Traditional · $it")
            }
            Text(
                text = entry.explanation.ifBlank { entry.more.ifBlank { "No definition available." } },
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPractice) {
                Text("Practice this character")
            }
        }
    }
}

@Composable
private fun HelpCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Tips",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "- Data comes from the bundled word.json so it works offline.\n" +
                    "- Unlock additional lessons in the Account tab to pair dictionary lookups with courses.\n" +
                    "- For handwriting and privacy details, see docs/help-guide.md inside the project.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
