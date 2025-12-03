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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bishun.R
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
            text = stringResource(R.string.library_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.library_description),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            label = { Text(stringResource(R.string.library_input_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text(stringResource(R.string.library_supporting_text)) },
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
            text = stringResource(R.string.library_quick_try),
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
                text = stringResource(R.string.library_recent_header),
                style = MaterialTheme.typography.labelLarge,
            )
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.library_recent_clear))
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
                stringResource(R.string.library_button_lookup_loading)
            } else {
                stringResource(R.string.library_button_lookup)
            }
            Text(lookupLabel)
        }
        OutlinedButton(onClick = onClear, enabled = hasResult) {
            Text(stringResource(R.string.library_button_clear_result))
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
            val fallback = stringResource(R.string.library_value_not_available)
            val unknown = stringResource(R.string.library_value_unknown)
            Text(stringResource(R.string.library_pinyin_label, entry.pinyin.ifBlank { fallback }))
            Text(
                stringResource(
                    R.string.library_radicals_strokes,
                    entry.radicals.ifBlank { fallback },
                    entry.strokes.ifBlank { unknown },
                ),
            )
            entry.oldword.takeIf { it.isNotBlank() }?.let {
                Text(stringResource(R.string.library_traditional_label, it))
            }
            Text(
                text = entry.explanation.ifBlank { entry.more.ifBlank { stringResource(R.string.library_definition_fallback) } },
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPractice) {
                Text(stringResource(R.string.library_button_practice))
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
                text = stringResource(R.string.library_help_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.library_help_body),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
