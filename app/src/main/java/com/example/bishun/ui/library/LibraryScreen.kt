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
import androidx.compose.material3.AssistChip
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
import com.example.bishun.ui.character.LibraryStrings
import com.example.bishun.ui.character.rememberLocalizedStrings
import java.util.Locale

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    onLoadInPractice: (String) -> Unit = {},
    languageOverride: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = rememberLocalizedStrings(languageOverride)
    val libraryStrings = strings.library
    val locale = strings.locale

    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = libraryStrings.title,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = libraryStrings.description,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            label = { Text(libraryStrings.inputLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text(libraryStrings.supportingText) },
        )
        QuickSuggestionsRow(
            strings = libraryStrings,
            suggestions = libraryStrings.quickSuggestions,
            onSuggestionClick = viewModel::loadCharacter,
        )
        RowActions(
            strings = libraryStrings,
            isLoading = uiState.isLoading,
            hasResult = uiState.result != null,
            onSearch = viewModel::submitQuery,
            onClear = viewModel::clearResult,
        )
        if (uiState.recentSearches.isNotEmpty()) {
            RecentSearchesRow(
                strings = libraryStrings,
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
                strings = libraryStrings,
                locale = locale,
                entry = entry,
                onPractice = { onLoadInPractice(entry.word) },
            )
        }
        HelpCard(strings = libraryStrings)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickSuggestionsRow(
    strings: LibraryStrings,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = strings.quickTryLabel,
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
    strings: LibraryStrings,
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
                text = strings.recentHeader,
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(onClick = onClear) {
                Text(strings.recentClear)
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            recent.forEach { symbol ->
                AssistChip(
                    onClick = { onSelect(symbol) },
                    label = { Text(symbol) },
                )
            }
        }
    }
}

@Composable
private fun RowActions(
    strings: LibraryStrings,
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
            val lookupLabel = if (isLoading) {
                strings.lookupLoadingLabel
            } else {
                strings.lookupLabel
            }
            Text(lookupLabel)
        }
        OutlinedButton(onClick = onClear, enabled = hasResult) {
            Text(strings.clearResultLabel)
        }
    }
}

@Composable
private fun WordEntryCard(
    strings: LibraryStrings,
    locale: Locale,
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
            val fallback = strings.valueNotAvailable
            val unknown = strings.valueUnknown
            Text(
                text = String.format(
                    locale,
                    strings.pinyinLabelFormat,
                    entry.pinyin.ifBlank { fallback },
                ),
            )
            Text(
                String.format(
                    locale,
                    strings.radicalsStrokesFormat,
                    entry.radicals.ifBlank { fallback },
                    entry.strokes.ifBlank { unknown },
                ),
            )
            entry.oldword.takeIf { it.isNotBlank() }?.let {
                Text(String.format(locale, strings.traditionalLabelFormat, it))
            }
            Text(
                text = entry.explanation.ifBlank {
                    entry.more.ifBlank { strings.definitionFallback }
                },
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPractice) {
                Text(strings.practiceButtonLabel)
            }
        }
    }
}

@Composable
private fun HelpCard(strings: LibraryStrings) {
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
                text = strings.helpTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = strings.helpBody,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
