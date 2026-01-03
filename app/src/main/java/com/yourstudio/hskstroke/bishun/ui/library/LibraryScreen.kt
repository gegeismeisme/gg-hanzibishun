package com.yourstudio.hskstroke.bishun.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.ui.character.LibraryStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.library.LibraryError
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

    var selectedTabKey by rememberSaveable { mutableStateOf(LibraryTab.Search.name) }
    val selectedTab = remember(selectedTabKey) { LibraryTab.valueOf(selectedTabKey) }
    val tabs = remember(libraryStrings) {
        listOf(
            LibraryTab.Search to libraryStrings.tabSearchLabel,
            LibraryTab.Favorites to libraryStrings.tabFavoritesLabel,
            LibraryTab.History to libraryStrings.tabHistoryLabel,
        )
    }
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = libraryStrings.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = libraryStrings.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                tabs.forEach { (tab, label) ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTabKey = tab.name },
                        text = { Text(label) },
                    )
                }
            }
        }

        when (selectedTab) {
            LibraryTab.Search -> {
                LibrarySearchTab(
                    uiState = uiState,
                    strings = libraryStrings,
                    locale = locale,
                    onQueryChange = viewModel::updateQuery,
                    onSearch = viewModel::submitQuery,
                    onClearResult = viewModel::clearResult,
                    onSelectRecent = viewModel::loadCharacter,
                    onClearHistory = viewModel::clearHistory,
                    onShowHistory = { selectedTabKey = LibraryTab.History.name },
                    onSelectWord = viewModel::selectWord,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onPracticeSymbol = onLoadInPractice,
                )
            }

            LibraryTab.Favorites -> {
                FavoritesTab(
                    favorites = uiState.favorites,
                    strings = libraryStrings,
                    onOpen = {
                        viewModel.loadCharacter(it)
                        selectedTabKey = LibraryTab.Search.name
                    },
                    onRemove = viewModel::toggleFavorite,
                    onClearAll = viewModel::clearFavorites,
                )
            }

            LibraryTab.History -> {
                HistoryTab(
                    recentWords = uiState.recentWords,
                    pinnedWords = uiState.pinnedRecentWords,
                    favorites = uiState.favorites,
                    strings = libraryStrings,
                    onOpen = {
                        viewModel.loadCharacter(it)
                        selectedTabKey = LibraryTab.Search.name
                    },
                    onTogglePinned = viewModel::togglePinnedRecent,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRemove = viewModel::removeRecentWord,
                    onClearAll = viewModel::clearHistory,
                )
            }
        }
    }
}

private enum class LibraryTab {
    Search,
    Favorites,
    History,
}

private enum class SavedSortMode {
    Recent,
    Name,
}

@Composable
private fun LibrarySearchTab(
    uiState: LibraryUiState,
    strings: LibraryStrings,
    locale: Locale,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearResult: () -> Unit,
    onSelectRecent: (String) -> Unit,
    onClearHistory: () -> Unit,
    onShowHistory: () -> Unit,
    onSelectWord: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPracticeSymbol: (String) -> Unit,
) {
    val contentScrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(contentScrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text(strings.inputLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text(strings.supportingText) },
        )
        RowActions(
            strings = strings,
            isLoading = uiState.isLoading,
            hasResult = uiState.results.isNotEmpty(),
            onSearch = onSearch,
            onClear = onClearResult,
        )
        if (uiState.recentWords.isNotEmpty()) {
            RecentSearchesRow(
                strings = strings,
                recent = uiState.recentWords,
                onSelect = onSelectRecent,
                onClear = onClearHistory,
                onShowOverflow = onShowHistory,
            )
        }
        uiState.error?.let { error ->
            val message = when (error) {
                LibraryError.EmptyQuery -> strings.errorEmpty
                LibraryError.NotFound -> strings.errorNotFound
                LibraryError.ReadFailure -> strings.errorRead
            }
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        val selectedEntry = remember(uiState.results, uiState.selectedWord) {
            uiState.results.firstOrNull { it.word == uiState.selectedWord }
                ?: uiState.results.firstOrNull()
        }
        selectedEntry?.let { entry ->
            if (uiState.results.size > 1) {
                SearchResultsRow(
                    strings = strings,
                    results = uiState.results,
                    selectedWord = entry.word,
                    onSelect = onSelectWord,
                )
            }
            WordEntryCard(
                strings = strings,
                locale = locale,
                entry = entry,
                isFavorite = uiState.favorites.contains(entry.word),
                onToggleFavorite = { onToggleFavorite(entry.word) },
                onPracticeSymbol = onPracticeSymbol,
            )
        }
        HelpCard(strings = strings)
        Spacer(modifier = Modifier.heightIn(min = 0.dp, max = 12.dp))
    }
}

@Composable
private fun FavoritesTab(
    favorites: List<String>,
    strings: LibraryStrings,
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var sortModeKey by rememberSaveable { mutableStateOf(SavedSortMode.Recent.name) }
    val sortMode = remember(sortModeKey) { SavedSortMode.valueOf(sortModeKey) }
    val sortedFavorites = remember(favorites, sortMode) {
        when (sortMode) {
            SavedSortMode.Recent -> favorites
            SavedSortMode.Name -> favorites.sorted()
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.favoritesClearDialogTitle) },
            text = { Text(strings.favoritesClearDialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                    },
                ) { Text(strings.favoritesClearConfirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.favoritesClearCancelLabel) }
            },
        )
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.favoritesHeader,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(
                onClick = { showClearDialog = true },
                enabled = favorites.isNotEmpty(),
            ) { Text(strings.favoritesClearLabel) }
        }

        SortModeRow(
            strings = strings,
            selected = sortMode,
            onSelect = { sortModeKey = it.name },
        )

        if (sortedFavorites.isEmpty()) {
            Text(strings.favoritesEmpty, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                sortedFavorites.forEach { word ->
                    FavoriteRow(
                        word = word,
                        strings = strings,
                        onOpen = { onOpen(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.heightIn(min = 0.dp, max = 24.dp))
    }
}

@Composable
private fun HistoryTab(
    recentWords: List<String>,
    pinnedWords: List<String>,
    favorites: List<String>,
    strings: LibraryStrings,
    onOpen: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemove: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var sortModeKey by rememberSaveable { mutableStateOf(SavedSortMode.Recent.name) }
    val sortMode = remember(sortModeKey) { SavedSortMode.valueOf(sortModeKey) }
    val pinnedSet = remember(pinnedWords) { pinnedWords.toSet() }
    val visiblePinned = remember(pinnedWords, sortMode) {
        when (sortMode) {
            SavedSortMode.Recent -> pinnedWords
            SavedSortMode.Name -> pinnedWords.sorted()
        }
    }
    val visibleRecent = remember(recentWords, pinnedSet, sortMode) {
        val rest = recentWords.filterNot { it in pinnedSet }
        when (sortMode) {
            SavedSortMode.Recent -> rest
            SavedSortMode.Name -> rest.sorted()
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.historyClearDialogTitle) },
            text = { Text(strings.historyClearDialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                    },
                ) { Text(strings.historyClearConfirmLabel) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.historyClearCancelLabel) }
            },
        )
    }

    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = strings.historyHeader,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(
                onClick = { showClearDialog = true },
                enabled = recentWords.isNotEmpty() || pinnedWords.isNotEmpty(),
            ) { Text(strings.historyClearLabel) }
        }

        SortModeRow(
            strings = strings,
            selected = sortMode,
            onSelect = { sortModeKey = it.name },
        )

        if (visiblePinned.isNotEmpty()) {
            Text(
                text = strings.historyPinnedHeader,
                style = MaterialTheme.typography.labelLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visiblePinned.forEach { word ->
                    HistoryRow(
                        word = word,
                        strings = strings,
                        isPinned = true,
                        isFavorite = favorites.contains(word),
                        onOpen = { onOpen(word) },
                        onTogglePinned = { onTogglePinned(word) },
                        onToggleFavorite = { onToggleFavorite(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        if (visibleRecent.isNotEmpty()) {
            Text(
                text = strings.recentHeader,
                style = MaterialTheme.typography.labelLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visibleRecent.forEach { word ->
                    HistoryRow(
                        word = word,
                        strings = strings,
                        isPinned = false,
                        isFavorite = favorites.contains(word),
                        onOpen = { onOpen(word) },
                        onTogglePinned = { onTogglePinned(word) },
                        onToggleFavorite = { onToggleFavorite(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        if (visiblePinned.isEmpty() && visibleRecent.isEmpty()) {
            Text(strings.historyEmpty, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.heightIn(min = 0.dp, max = 24.dp))
    }
}

@Composable
private fun SortModeRow(
    strings: LibraryStrings,
    selected: SavedSortMode,
    onSelect: (SavedSortMode) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        FilterChip(
            selected = selected == SavedSortMode.Recent,
            onClick = { onSelect(SavedSortMode.Recent) },
            label = { Text(strings.sortRecentLabel) },
        )
        FilterChip(
            selected = selected == SavedSortMode.Name,
            onClick = { onSelect(SavedSortMode.Name) },
            label = { Text(strings.sortNameLabel) },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecentSearchesRow(
    strings: LibraryStrings,
    recent: List<String>,
    onSelect: (String) -> Unit,
    onClear: () -> Unit,
    onShowOverflow: () -> Unit,
) {
    val inline = recent.take(RECENT_INLINE_LIMIT)
    val hasOverflow = recent.size > RECENT_INLINE_LIMIT
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
            inline.forEach { symbol ->
                AssistChip(
                    onClick = { onSelect(symbol) },
                    label = { Text(symbol) },
                )
            }
            if (hasOverflow) {
                AssistChip(
                    onClick = onShowOverflow,
                    label = { Text(strings.recentOverflowLabel) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchResultsRow(
    strings: LibraryStrings,
    results: List<WordEntry>,
    selectedWord: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = strings.resultsHeader,
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            results.forEach { entry ->
                FilterChip(
                    selected = entry.word == selectedWord,
                    onClick = { onSelect(entry.word) },
                    label = { Text(entry.word) },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordEntryCard(
    strings: LibraryStrings,
    locale: Locale,
    entry: WordEntry,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onPracticeSymbol: (String) -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = entry.word,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                )
                TextButton(onClick = onToggleFavorite) {
                    Text(if (isFavorite) strings.favoritesSavedLabel else strings.favoritesSaveLabel)
                }
            }
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
                style = MaterialTheme.typography.bodyMedium,
            )
            val practiceTargets = remember(entry.word) {
                entry.word.asSequence()
                    .filter { it in '\u4e00'..'\u9fff' }
                    .map { it.toString() }
                    .distinct()
                    .toList()
            }
            if (practiceTargets.size <= 1) {
                val symbol = practiceTargets.firstOrNull() ?: entry.word.take(1)
                Button(onClick = { onPracticeSymbol(symbol) }) {
                    Text(strings.practiceButtonLabel)
                }
            } else {
                Text(
                    text = strings.practiceCharactersLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    practiceTargets.forEach { symbol ->
                        AssistChip(
                            onClick = { onPracticeSymbol(symbol) },
                            label = { Text(symbol) },
                        )
                    }
                }
            }
        }
    }
}
private const val RECENT_INLINE_LIMIT = 3

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

@Composable
private fun FavoriteRow(
    word: String,
    strings: LibraryStrings,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by rememberSaveable(word) { mutableStateOf(false) }
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = strings.moreActionsLabel,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(strings.favoritesRemoveLabel) },
                    onClick = {
                        expanded = false
                        onRemove()
                    },
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    word: String,
    strings: LibraryStrings,
    isPinned: Boolean,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by rememberSaveable(word) { mutableStateOf(false) }
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = strings.moreActionsLabel,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(if (isPinned) strings.historyUnpinLabel else strings.historyPinLabel) },
                    onClick = {
                        expanded = false
                        onTogglePinned()
                    },
                )
                DropdownMenuItem(
                    text = { Text(if (isFavorite) strings.favoritesRemoveLabel else strings.favoritesAddLabel) },
                    onClick = {
                        expanded = false
                        onToggleFavorite()
                    },
                )
                DropdownMenuItem(
                    text = { Text(strings.historyRemoveLabel) },
                    onClick = {
                        expanded = false
                        onRemove()
                    },
                )
            }
        }
    }
}
