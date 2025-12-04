package com.example.bishun.ui.courses

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.example.bishun.ui.account.AccountViewModel
import com.example.bishun.ui.character.CharacterViewModel
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.CoursesStrings
import com.example.bishun.ui.character.HskLevelSummary
import com.example.bishun.ui.character.components.IconActionButton
import com.example.bishun.ui.character.rememberLocalizedStrings

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel,
    isSignedIn: Boolean,
    unlockedLevels: Set<Int>,
    onNavigateToPractice: () -> Unit = {},
    onRequestUnlock: () -> Unit = {},
    languageOverride: String? = null,
) {
    val courseCatalog by viewModel.courseCatalog.collectAsState()
    val courseSession by viewModel.courseSession.collectAsState()
    val hskProgress by viewModel.hskProgress.collectAsState()
    val completedSymbols by viewModel.completedSymbols.collectAsState()

    val startCourseAt: (Int, String) -> Unit = remember {
        { level, symbol ->
            viewModel.startCourse(level, symbol)
            onNavigateToPractice()
        }
    }
    val sortedLevels = remember(courseCatalog) { courseCatalog.entries.sortedBy { it.key } }
    var selectedFilterKey by rememberSaveable { mutableStateOf(CourseFilter.REMAINING.name) }
    val selectedFilter = remember(selectedFilterKey) { CourseFilter.valueOf(selectedFilterKey) }
    val expandedLevels = remember { mutableStateMapOf<Int, Boolean>() }
    val strings = rememberLocalizedStrings(languageOverride)
    val courseStrings = strings.courses
    val locale = strings.locale

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = courseStrings.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = courseStrings.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CourseFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilterKey = filter.name },
                        label = { Text(filter.label(courseStrings)) },
                    )
                }
            }
        }
        item { CourseLegendRow(strings = courseStrings) }
        courseSession?.let { session ->
            item {
                ActiveCourseCard(
                    session = session,
                    strings = courseStrings,
                    locale = locale,
                    onResume = {
                        val symbol = session.currentSymbol ?: return@ActiveCourseCard
                        viewModel.jumpToCharacter(symbol)
                        onNavigateToPractice()
                    },
                    onSkip = viewModel::skipCourseCharacter,
                    onRestart = viewModel::restartCourseLevel,
                    onExit = viewModel::clearCourseSession,
                )
            }
        }
        if (sortedLevels.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(
                        text = courseStrings.emptyCatalogMessage,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        } else {
            items(sortedLevels) { entry ->
                val level = entry.key
                val symbols = entry.value
                val summary = hskProgress.perLevel[level]
                    ?: HskLevelSummary(
                        completed = symbols.count { completedSymbols.contains(it) },
                        total = symbols.size,
                    )
                val fallbackNext = symbols.firstOrNull { !completedSymbols.contains(it) }
                val nextSymbol = hskProgress.nextTargets[level] ?: fallbackNext
                val canAccess = AccountViewModel.FREE_LEVELS.contains(level) || unlockedLevels.contains(level)
                val accentColor = levelColor(level)

                CourseLevelCard(
                    level = level,
                    summary = summary,
                    nextSymbol = nextSymbol,
                    canAccess = canAccess,
                    isSignedIn = isSignedIn,
                    accentColor = accentColor,
                    strings = courseStrings,
                    locale = locale,
                    onStart = { symbol -> startCourseAt(level, symbol) },
                    onRequestUnlock = onRequestUnlock,
                )

                val activeSession = courseSession
                val symbolStates = symbols.map { symbol ->
                    val active = activeSession?.level == level && activeSession.currentSymbol == symbol
                    val completed = completedSymbols.contains(symbol)
                    CourseSymbolVisual(symbol = symbol, isActive = active, isCompleted = completed)
                }.filter { visual ->
                    visual.isActive || selectedFilter.include(visual.isCompleted)
                }

                CourseSymbolGrid(
                    symbolStates = symbolStates,
                    accentColor = accentColor,
                    strings = courseStrings,
                    isExpanded = expandedLevels[level] == true,
                    onToggleExpand = {
                        val current = expandedLevels[level] == true
                        expandedLevels[level] = !current
                    }.takeIf { symbolStates.size > SYMBOL_PREVIEW_COUNT },
                    onSelect = { symbol -> startCourseAt(level, symbol) },
                    onMarkLearned = viewModel::markCourseCharacterLearned,
                )
            }
        }
    }
}

@Composable
private fun ActiveCourseCard(
    session: CourseSession,
    strings: CoursesStrings,
    locale: Locale,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val title = String.format(locale, strings.activeCourseTitleFormat, session.level)
            val status = String.format(
                locale,
                strings.activeCourseStatusFormat,
                session.currentSymbol ?: "-",
                session.progressText,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onResume, enabled = session.currentSymbol != null) {
                    Text(strings.activeResume)
                }
                OutlinedButton(onClick = onSkip, enabled = session.hasNext) {
                    Text(strings.activeSkip)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onRestart) { Text(strings.activeRestart) }
                OutlinedButton(onClick = onExit) { Text(strings.activeExit) }
            }
        }
    }
}

@Composable
private fun CourseLevelCard(
    level: Int,
    summary: HskLevelSummary,
    nextSymbol: String?,
    canAccess: Boolean,
    isSignedIn: Boolean,
    accentColor: Color,
    strings: CoursesStrings,
    locale: Locale,
    onStart: (String) -> Unit,
    onRequestUnlock: () -> Unit,
) {
    val total = summary.total.coerceAtLeast(1)
    val progress = (summary.completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = String.format(locale, strings.levelHeaderFormat, level),
                        style = MaterialTheme.typography.titleSmall.copy(color = accentColor),
                    )
                    Text(
                        text = String.format(
                            locale,
                            strings.levelProgressFormat,
                            summary.completed,
                            summary.total,
                            summary.remaining,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = String.format(locale, strings.levelChipFormat, level),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = accentColor),
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val statusText = nextSymbol?.let {
                    String.format(locale, strings.levelNextFormat, it)
                } ?: strings.levelCompletedLabel
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                when {
                    !canAccess -> {
                        Button(onClick = onRequestUnlock) {
                            Text(if (isSignedIn) strings.lockedUnlockLabel else strings.lockedSignInLabel)
                        }
                    }
                    nextSymbol != null -> {
                        IconActionButton(
                            icon = Icons.Filled.PlayArrow,
                            description = String.format(locale, strings.iconStartDescriptionFormat, level),
                            onClick = { onStart(nextSymbol) },
                            buttonSize = 36.dp,
                        )
                    }
                    else -> {
                        TextButton(onClick = {}) { Text(strings.lockedGreatJobLabel) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CourseSymbolGrid(
    symbolStates: List<CourseSymbolVisual>,
    accentColor: Color,
    strings: CoursesStrings,
    isExpanded: Boolean,
    onToggleExpand: (() -> Unit)?,
    onSelect: (String) -> Unit,
    onMarkLearned: (String) -> Unit,
) {
    if (symbolStates.isEmpty()) {
        Text(
            text = strings.symbolEmptyMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    val displaySymbols = if (isExpanded || onToggleExpand == null) {
        symbolStates
    } else {
        symbolStates.take(SYMBOL_PREVIEW_COUNT)
    }
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        displaySymbols.forEach { visual ->
            val background = when {
                visual.isActive -> accentColor.copy(alpha = 0.25f)
                visual.isCompleted -> accentColor.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surface
            }
            val contentColor = when {
                visual.isActive -> accentColor
                visual.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.onSurface
            }
            Surface(
                color = background,
                contentColor = contentColor,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = if (visual.isActive) 4.dp else 0.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { onSelect(visual.symbol) },
                        onLongClick = { onMarkLearned(visual.symbol) },
                    ),
            ) {
                Text(
                    text = visual.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
    onToggleExpand?.let { toggle ->
        if (symbolStates.size > SYMBOL_PREVIEW_COUNT) {
            TextButton(onClick = toggle) {
                Text(if (isExpanded) strings.symbolCollapseLabel else strings.symbolShowAllLabel)
            }
        }
    }
}

@Composable
private fun CourseLegendRow(strings: CoursesStrings) {
    val legend = listOf(
        CourseLegendEntry(strings.legendActive, Color(0xFF4CAF50).copy(alpha = 0.2f)),
        CourseLegendEntry(strings.legendCompleted, MaterialTheme.colorScheme.surfaceVariant),
        CourseLegendEntry(strings.legendRemaining, MaterialTheme.colorScheme.surface),
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = strings.legendTitle, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            legend.forEach { entry ->
                Surface(
                    color = entry.color,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = entry.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
        Text(
            text = strings.legendHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private enum class CourseFilter {
    ALL,
    REMAINING,
    COMPLETED;

    fun include(isCompleted: Boolean): Boolean = when (this) {
        ALL -> true
        REMAINING -> !isCompleted
        COMPLETED -> isCompleted
    }
}

private fun CourseFilter.label(strings: CoursesStrings): String = when (this) {
    CourseFilter.ALL -> strings.filterAll
    CourseFilter.REMAINING -> strings.filterRemaining
    CourseFilter.COMPLETED -> strings.filterCompleted
}

private data class CourseSymbolVisual(
    val symbol: String,
    val isActive: Boolean,
    val isCompleted: Boolean,
)

private data class CourseLegendEntry(val label: String, val color: Color)

private fun levelColor(level: Int): Color = when (level) {
    1 -> Color(0xFF4CAF50)
    2 -> Color(0xFF2196F3)
    3 -> Color(0xFFFFB300)
    4 -> Color(0xFF9C27B0)
    5 -> Color(0xFFFF7043)
    6 -> Color(0xFF607D8B)
    else -> Color(0xFF6750A4)
}

private const val SYMBOL_PREVIEW_COUNT = 12
