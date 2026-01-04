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
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.ui.character.LibraryStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.library.LibraryError
import com.yourstudio.hskstroke.bishun.ui.testing.TestTags
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel,
    onLoadInPractice: (String) -> Unit = {},
    onLoadPracticeQueue: (List<String>) -> Unit = {},
    languageOverride: String? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = rememberLocalizedStrings(languageOverride)
    val libraryStrings = strings.library
    val locale = strings.locale
    val context = LocalContext.current
    val preferencesStore = remember { UserPreferencesStore(context.applicationContext) }
    val userPreferences by preferencesStore.data.collectAsState(initial = UserPreferences())
    val isPro = userPreferences.isPro

    var selectedTabKey by rememberSaveable { mutableStateOf(LibraryTab.Search.name) }
    val selectedTab = remember(selectedTabKey) { LibraryTab.valueOf(selectedTabKey) }
    val tabs = remember(libraryStrings) {
        listOf(
            LibraryTab.Search to libraryStrings.tabSearchLabel,
            LibraryTab.Favorites to libraryStrings.tabFavoritesLabel,
            LibraryTab.History to libraryStrings.tabHistoryLabel,
        )
    }

    var practiceDialogWord by rememberSaveable { mutableStateOf<String?>(null) }
    val requestPractice: (String) -> Unit = { rawWord ->
        val word = rawWord.trim()
        if (word.isNotBlank()) {
            viewModel.recordRecentWord(word)
            val targets = word.asSequence()
                .filter { it in '\u4e00'..'\u9fff' }
                .map { it.toString() }
                .distinct()
                .toList()
            if (targets.size <= 1) {
                val symbol = targets.firstOrNull() ?: word.take(1)
                onLoadInPractice(symbol)
            } else {
                practiceDialogWord = word
            }
        }
    }
    val requestPracticeSelection: (List<String>) -> Unit = { words ->
        val targets = words.asSequence()
            .flatMap { it.asSequence() }
            .filter { it in '\u4e00'..'\u9fff' }
            .map { it.toString() }
            .distinct()
            .toList()
        when (targets.size) {
            0 -> {
                val fallback = words.firstOrNull()?.trim()?.take(1).orEmpty()
                if (fallback.isNotBlank()) {
                    onLoadInPractice(fallback)
                }
            }

            1 -> onLoadInPractice(targets.first())
            else -> onLoadPracticeQueue(targets)
        }
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
                    details = uiState.wordDetails,
                    strings = libraryStrings,
                    locale = locale,
                    isPro = isPro,
                    onPractice = requestPractice,
                    onPracticeSelected = requestPracticeSelection,
                    onOpen = {
                        viewModel.loadCharacter(it)
                        selectedTabKey = LibraryTab.Search.name
                    },
                    onRemove = viewModel::toggleFavorite,
                    onRemoveSelected = viewModel::removeFavorites,
                    onClearAll = viewModel::clearFavorites,
                )
            }

            LibraryTab.History -> {
                HistoryTab(
                    recentWords = uiState.recentWords,
                    pinnedWords = uiState.pinnedRecentWords,
                    favorites = uiState.favorites,
                    details = uiState.wordDetails,
                    strings = libraryStrings,
                    locale = locale,
                    isPro = isPro,
                    onPractice = requestPractice,
                    onPracticeSelected = requestPracticeSelection,
                    onOpen = {
                        viewModel.loadCharacter(it)
                        selectedTabKey = LibraryTab.Search.name
                    },
                    onTogglePinned = viewModel::togglePinnedRecent,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onRemove = viewModel::removeRecentWord,
                    onPinSelected = viewModel::pinRecentWords,
                    onUnpinSelected = viewModel::unpinRecentWords,
                    onSaveSelected = viewModel::addFavorites,
                    onUnsaveSelected = viewModel::removeFavorites,
                    onRemoveSelected = viewModel::removeRecentWords,
                    onClearAll = viewModel::clearHistory,
                )
            }
        }
    }

    practiceDialogWord?.let { word ->
        val characters = remember(word) {
            word.asSequence()
                .filter { it in '\u4e00'..'\u9fff' }
                .map { it.toString() }
                .distinct()
                .toList()
        }
        AlertDialog(
            onDismissRequest = { practiceDialogWord = null },
            title = { Text(libraryStrings.practiceCharactersLabel) },
            text = {
                if (characters.isEmpty()) {
                    Text(word.take(1), style = MaterialTheme.typography.titleLarge)
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        characters.forEach { symbol ->
                            AssistChip(
                                onClick = {
                                    practiceDialogWord = null
                                    onLoadInPractice(symbol)
                                },
                                label = { Text(symbol) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        practiceDialogWord = null
                        val targets = characters.ifEmpty { listOf(word.take(1)) }
                        if (targets.size <= 1) {
                            onLoadInPractice(targets.first())
                        } else {
                            onLoadPracticeQueue(targets)
                        }
                    },
                ) {
                    Text(libraryStrings.practiceCharactersLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { practiceDialogWord = null }) {
                    Text(strings.account.cancelLabel)
                }
            },
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FavoritesTab(
    favorites: List<String>,
    details: Map<String, WordEntry>,
    strings: LibraryStrings,
    locale: Locale,
    isPro: Boolean,
    onPractice: (String) -> Unit,
    onPracticeSelected: (List<String>) -> Unit,
    onOpen: (String) -> Unit,
    onRemove: (String) -> Unit,
    onRemoveSelected: (Collection<String>) -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var sortModeKey by rememberSaveable { mutableStateOf(SavedSortMode.Recent.name) }
    val sortMode = remember(sortModeKey) { SavedSortMode.valueOf(sortModeKey) }
    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedWords by remember { mutableStateOf(setOf<String>()) }
    var filterQuery by rememberSaveable { mutableStateOf("") }
    val sortedFavorites = remember(favorites, sortMode) {
        when (sortMode) {
            SavedSortMode.Recent -> favorites
            SavedSortMode.Name -> favorites.sorted()
        }
    }
    val filteredFavorites = remember(sortedFavorites, filterQuery, details) {
        sortedFavorites.filter { word -> matchesLibraryFilterQuery(word, details[word], filterQuery) }
    }

    androidx.compose.runtime.LaunchedEffect(favorites, filteredFavorites) {
        if (favorites.isEmpty()) {
            isSelecting = false
            selectedWords = emptySet()
        } else if (selectedWords.isNotEmpty()) {
            val allowed = filteredFavorites.toSet()
            selectedWords = selectedWords.filter { it in allowed }.toSet()
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
                text = if (isSelecting) {
                    String.format(locale, strings.selectedCountFormat, selectedWords.size)
                } else {
                    strings.favoritesHeader
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isSelecting) {
                    TextButton(
                        onClick = {
                            isSelecting = false
                            selectedWords = emptySet()
                        },
                    ) { Text(strings.doneLabel) }
                } else {
                    TextButton(
                        onClick = { isSelecting = true },
                        enabled = favorites.isNotEmpty(),
                    ) { Text(strings.editLabel) }
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        enabled = favorites.isNotEmpty(),
                    ) { Text(strings.favoritesClearLabel) }
                }
            }
        }

        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it.take(64) },
            label = { Text(strings.favoritesFilterLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (isSelecting) {
            val allSelected = filteredFavorites.isNotEmpty() && selectedWords.size == filteredFavorites.size
            val orderedSelection = remember(filteredFavorites, selectedWords) { filteredFavorites.filter { it in selectedWords } }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = {
                        selectedWords = if (allSelected) {
                            emptySet()
                        } else {
                            filteredFavorites.toSet()
                        }
                    },
                    enabled = filteredFavorites.isNotEmpty(),
                ) { Text(if (allSelected) strings.deselectAllLabel else strings.selectAllLabel) }
                OutlinedButton(
                    onClick = {
                        onPracticeSelected(orderedSelection)
                        selectedWords = emptySet()
                        isSelecting = false
                    },
                    enabled = orderedSelection.isNotEmpty() && isPro,
                ) {
                    Text(
                        text = if (isPro) strings.practiceCharactersLabel else "${strings.practiceCharactersLabel} · Pro",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                OutlinedButton(
                    onClick = {
                        onRemoveSelected(selectedWords)
                        selectedWords = emptySet()
                        isSelecting = false
                    },
                    enabled = selectedWords.isNotEmpty(),
                ) { Text(strings.favoritesRemoveLabel) }
            }
        }

        SortModeRow(
            strings = strings,
            selected = sortMode,
            onSelect = { sortModeKey = it.name },
        )

        if (favorites.isEmpty()) {
            Text(strings.favoritesEmpty, style = MaterialTheme.typography.bodyMedium)
        } else if (filteredFavorites.isEmpty() && filterQuery.isNotBlank()) {
            Text(strings.filterNoResultsLabel, style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredFavorites.forEach { word ->
                    FavoriteRow(
                        word = word,
                        strings = strings,
                        entry = details[word],
                        isSelectionMode = isSelecting,
                        isSelected = selectedWords.contains(word),
                        onToggleSelected = { selectedWords = toggleSelection(selectedWords, word) },
                        onOpen = { onOpen(word) },
                        onPractice = { onPractice(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.heightIn(min = 0.dp, max = 24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryTab(
    recentWords: List<String>,
    pinnedWords: List<String>,
    favorites: List<String>,
    details: Map<String, WordEntry>,
    strings: LibraryStrings,
    locale: Locale,
    isPro: Boolean,
    onPractice: (String) -> Unit,
    onPracticeSelected: (List<String>) -> Unit,
    onOpen: (String) -> Unit,
    onTogglePinned: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onRemove: (String) -> Unit,
    onPinSelected: (List<String>) -> Unit,
    onUnpinSelected: (Collection<String>) -> Unit,
    onSaveSelected: (List<String>) -> Unit,
    onUnsaveSelected: (Collection<String>) -> Unit,
    onRemoveSelected: (Collection<String>) -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var sortModeKey by rememberSaveable { mutableStateOf(SavedSortMode.Recent.name) }
    val sortMode = remember(sortModeKey) { SavedSortMode.valueOf(sortModeKey) }
    var isSelecting by rememberSaveable { mutableStateOf(false) }
    var selectedWords by remember { mutableStateOf(setOf<String>()) }
    var filterQuery by rememberSaveable { mutableStateOf("") }
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
    val filteredPinned = remember(visiblePinned, filterQuery, details) {
        visiblePinned.filter { word -> matchesLibraryFilterQuery(word, details[word], filterQuery) }
    }
    val filteredRecent = remember(visibleRecent, filterQuery, details) {
        visibleRecent.filter { word -> matchesLibraryFilterQuery(word, details[word], filterQuery) }
    }
    val allVisible = remember(filteredPinned, filteredRecent) { filteredPinned + filteredRecent }

    androidx.compose.runtime.LaunchedEffect(recentWords, pinnedWords, allVisible) {
        if (recentWords.isEmpty() && pinnedWords.isEmpty()) {
            isSelecting = false
            selectedWords = emptySet()
        } else if (selectedWords.isNotEmpty()) {
            val allowed = allVisible.toSet()
            selectedWords = selectedWords.filter { it in allowed }.toSet()
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
                text = if (isSelecting) {
                    String.format(locale, strings.selectedCountFormat, selectedWords.size)
                } else {
                    strings.historyHeader
                },
                style = MaterialTheme.typography.titleMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isSelecting) {
                    TextButton(
                        onClick = {
                            isSelecting = false
                            selectedWords = emptySet()
                        },
                    ) { Text(strings.doneLabel) }
                } else {
                    TextButton(
                        onClick = { isSelecting = true },
                        enabled = recentWords.isNotEmpty() || pinnedWords.isNotEmpty(),
                    ) { Text(strings.editLabel) }
                    OutlinedButton(
                        onClick = { showClearDialog = true },
                        enabled = recentWords.isNotEmpty() || pinnedWords.isNotEmpty(),
                    ) { Text(strings.historyClearLabel) }
                }
            }
        }

        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it.take(64) },
            label = { Text(strings.historyFilterLabel) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (isSelecting) {
            val favoritesSet = remember(favorites) { favorites.toSet() }
            val selectedPinned = remember(selectedWords, pinnedSet) { selectedWords.filter { it in pinnedSet }.toSet() }
            val selectedUnpinned = remember(selectedWords, pinnedSet) { selectedWords.filterNot { it in pinnedSet }.toSet() }
            val selectedFavorites = remember(selectedWords, favoritesSet) { selectedWords.filter { it in favoritesSet }.toSet() }
            val selectedNotFavorites = remember(selectedWords, favoritesSet) { selectedWords.filterNot { it in favoritesSet }.toSet() }
            val orderedToPin = remember(allVisible, selectedUnpinned) { allVisible.filter { it in selectedUnpinned } }
            val orderedToSave = remember(allVisible, selectedNotFavorites) { allVisible.filter { it in selectedNotFavorites } }
            val orderedSelection = remember(allVisible, selectedWords) { allVisible.filter { it in selectedWords } }
            val allSelected = allVisible.isNotEmpty() && selectedWords.size == allVisible.size

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AssistChip(
                    onClick = {
                        selectedWords = if (allSelected) emptySet() else allVisible.toSet()
                    },
                    label = { Text(if (allSelected) strings.deselectAllLabel else strings.selectAllLabel) },
                )
                AssistChip(
                    onClick = {
                        onPracticeSelected(orderedSelection)
                        selectedWords = emptySet()
                        isSelecting = false
                    },
                    enabled = orderedSelection.isNotEmpty() && isPro,
                    label = { Text(if (isPro) strings.practiceCharactersLabel else "${strings.practiceCharactersLabel} · Pro") },
                )
                AssistChip(
                    onClick = {
                        onPinSelected(orderedToPin)
                        selectedWords = emptySet()
                    },
                    enabled = orderedToPin.isNotEmpty(),
                    label = { Text(strings.historyPinLabel) },
                )
                AssistChip(
                    onClick = {
                        onUnpinSelected(selectedPinned)
                        selectedWords = emptySet()
                    },
                    enabled = selectedPinned.isNotEmpty(),
                    label = { Text(strings.historyUnpinLabel) },
                )
                AssistChip(
                    onClick = {
                        onSaveSelected(orderedToSave)
                        selectedWords = emptySet()
                    },
                    enabled = orderedToSave.isNotEmpty(),
                    label = { Text(strings.favoritesAddLabel) },
                )
                AssistChip(
                    onClick = {
                        onUnsaveSelected(selectedFavorites)
                        selectedWords = emptySet()
                    },
                    enabled = selectedFavorites.isNotEmpty(),
                    label = { Text(strings.favoritesRemoveLabel) },
                )
                AssistChip(
                    onClick = {
                        onRemoveSelected(selectedWords)
                        selectedWords = emptySet()
                    },
                    enabled = selectedWords.isNotEmpty(),
                    label = { Text(strings.deleteLabel) },
                )
            }
        }

        SortModeRow(
            strings = strings,
            selected = sortMode,
            onSelect = { sortModeKey = it.name },
        )

        if (filteredPinned.isNotEmpty()) {
            Text(
                text = strings.historyPinnedHeader,
                style = MaterialTheme.typography.labelLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredPinned.forEach { word ->
                    HistoryRow(
                        word = word,
                        strings = strings,
                        entry = details[word],
                        isPinned = true,
                        isFavorite = favorites.contains(word),
                        isSelectionMode = isSelecting,
                        isSelected = selectedWords.contains(word),
                        onToggleSelected = { selectedWords = toggleSelection(selectedWords, word) },
                        onOpen = { onOpen(word) },
                        onPractice = { onPractice(word) },
                        onTogglePinned = { onTogglePinned(word) },
                        onToggleFavorite = { onToggleFavorite(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        if (filteredRecent.isNotEmpty()) {
            Text(
                text = strings.recentHeader,
                style = MaterialTheme.typography.labelLarge,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredRecent.forEach { word ->
                    HistoryRow(
                        word = word,
                        strings = strings,
                        entry = details[word],
                        isPinned = false,
                        isFavorite = favorites.contains(word),
                        isSelectionMode = isSelecting,
                        isSelected = selectedWords.contains(word),
                        onToggleSelected = { selectedWords = toggleSelection(selectedWords, word) },
                        onOpen = { onOpen(word) },
                        onPractice = { onPractice(word) },
                        onTogglePinned = { onTogglePinned(word) },
                        onToggleFavorite = { onToggleFavorite(word) },
                        onRemove = { onRemove(word) },
                    )
                }
            }
        }

        if (filteredPinned.isEmpty() && filteredRecent.isEmpty()) {
            val hasAnyHistory = recentWords.isNotEmpty() || pinnedWords.isNotEmpty()
            val message = when {
                !hasAnyHistory -> strings.historyEmpty
                filterQuery.isNotBlank() -> strings.filterNoResultsLabel
                else -> strings.historyEmpty
            }
            Text(message, style = MaterialTheme.typography.bodyMedium)
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
                Button(
                    onClick = { onPracticeSymbol(symbol) },
                    modifier = Modifier.testTag(TestTags.LIBRARY_PRACTICE_BUTTON),
                ) {
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
    entry: WordEntry?,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelected: () -> Unit,
    onOpen: () -> Unit,
    onPractice: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by rememberSaveable(word) { mutableStateOf(false) }
    val subtitle = remember(entry) { entry?.let(::buildEntrySubtitle).orEmpty() }
    Card(
        onClick = if (isSelectionMode) onToggleSelected else onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelected() },
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!isSelectionMode) {
                IconButton(onClick = onPractice) {
                    Icon(
                        imageVector = Icons.Outlined.Gesture,
                        contentDescription = strings.practiceButtonLabel,
                    )
                }
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
}

@Composable
private fun HistoryRow(
    word: String,
    strings: LibraryStrings,
    entry: WordEntry?,
    isPinned: Boolean,
    isFavorite: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelected: () -> Unit,
    onOpen: () -> Unit,
    onPractice: () -> Unit,
    onTogglePinned: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemove: () -> Unit,
) {
    var expanded by rememberSaveable(word) { mutableStateOf(false) }
    val subtitle = remember(entry) { entry?.let(::buildEntrySubtitle).orEmpty() }
    Card(
        onClick = if (isSelectionMode) onToggleSelected else onOpen,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelected() },
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (!isSelectionMode) {
                IconButton(onClick = onPractice) {
                    Icon(
                        imageVector = Icons.Outlined.Gesture,
                        contentDescription = strings.practiceButtonLabel,
                    )
                }
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
}

private fun buildEntrySubtitle(entry: WordEntry): String {
    val pinyin = entry.pinyin.trim()
    val definition = entry.explanation.ifBlank { entry.more }.trim()
    val condensed = definition.replace(WHITESPACE_REGEX, " ").trim()
    val clipped = if (condensed.length > 80) condensed.take(80).trimEnd() + "…" else condensed
    return when {
        pinyin.isNotBlank() && clipped.isNotBlank() -> "$pinyin · $clipped"
        pinyin.isNotBlank() -> pinyin
        clipped.isNotBlank() -> clipped
        else -> ""
    }
}

private val WHITESPACE_REGEX = Regex("\\s+")

private fun toggleSelection(selected: Set<String>, word: String): Set<String> {
    return if (word in selected) {
        selected - word
    } else {
        selected + word
    }
}
