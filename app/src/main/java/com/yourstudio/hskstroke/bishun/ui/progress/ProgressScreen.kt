package com.yourstudio.hskstroke.bishun.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourstudio.hskstroke.bishun.data.history.PracticeHistoryEntry
import com.yourstudio.hskstroke.bishun.ui.character.CharacterViewModel
import com.yourstudio.hskstroke.bishun.ui.character.HskLevelSummary
import com.yourstudio.hskstroke.bishun.ui.character.HskProgressSummary
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.ProgressStrings
import com.yourstudio.hskstroke.bishun.ui.character.components.IconActionButton
import com.yourstudio.hskstroke.bishun.ui.character.pickDailySymbol
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.data.history.effectivePracticeStreakDays
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel,
    onJumpToPractice: () -> Unit = {},
    onJumpToCharacter: (String) -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    languageOverride: String? = null,
) {
    val hskProgress by viewModel.hskProgress.collectAsState()
    val practiceHistory by viewModel.practiceHistory.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val strings = rememberLocalizedStrings(languageOverride)
    val progressStrings = strings.progress

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = progressStrings.title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = progressStrings.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onJumpToPractice) {
                    Text(progressStrings.jumpBackLabel)
                }
            }
        }
        item {
            HskProgressView(
                strings = strings,
                summary = hskProgress,
                practiceHistory = practiceHistory,
                dailySymbol = userPreferences.dailySymbol,
                dailyEpochDay = userPreferences.dailyEpochDay,
                dailyPracticeCompletedSymbol = userPreferences.dailyPracticeCompletedSymbol,
                dailyPracticeCompletedEpochDay = userPreferences.dailyPracticeCompletedEpochDay,
                practiceStreakDays = userPreferences.practiceStreakDays,
                practiceStreakLastEpochDay = userPreferences.practiceStreakLastEpochDay,
                onJumpToChar = onJumpToCharacter,
                onNavigateToCourses = onNavigateToCourses,
            )
        }
    }
}

@Composable
private fun HskProgressView(
    strings: LocalizedStrings,
    summary: HskProgressSummary,
    practiceHistory: List<PracticeHistoryEntry>,
    dailySymbol: String?,
    dailyEpochDay: Long?,
    dailyPracticeCompletedSymbol: String?,
    dailyPracticeCompletedEpochDay: Long?,
    practiceStreakDays: Int,
    practiceStreakLastEpochDay: Long?,
    onJumpToChar: (String) -> Unit,
    onNavigateToCourses: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val progressStrings = strings.progress
        val suggestedDailySymbol = remember(summary) { pickDailySymbol(summary) }
        val zone = remember { ZoneId.systemDefault() }
        val todayEpochDay = remember(zone) { LocalDate.now(zone).toEpochDay() }
        val resolvedDailySymbol = remember(dailyEpochDay, dailySymbol, suggestedDailySymbol, todayEpochDay) {
            val storedSymbol = dailySymbol?.trim()?.takeIf { it.isNotBlank() }
            if (dailyEpochDay == todayEpochDay && storedSymbol != null) {
                storedSymbol
            } else {
                suggestedDailySymbol
            }
        }
        val dailyCompletedToday = remember(
            resolvedDailySymbol,
            dailyPracticeCompletedSymbol,
            dailyPracticeCompletedEpochDay,
            todayEpochDay,
        ) {
            val completedSymbol = dailyPracticeCompletedSymbol?.trim().takeIf { !it.isNullOrBlank() }
            val resolvedSymbol = resolvedDailySymbol?.trim().takeIf { !it.isNullOrBlank() }
            completedSymbol != null &&
                resolvedSymbol != null &&
                dailyPracticeCompletedEpochDay == todayEpochDay &&
                completedSymbol == resolvedSymbol
        }
        DailyPracticeCard(
            strings = progressStrings,
            symbol = resolvedDailySymbol,
            completedToday = dailyCompletedToday,
            onPractice = {
                resolvedDailySymbol?.let(onJumpToChar)
            },
        )
        val effectiveStreakDays = remember(practiceStreakDays, practiceStreakLastEpochDay, todayEpochDay) {
            effectivePracticeStreakDays(practiceStreakDays, practiceStreakLastEpochDay, todayEpochDay)
        }
        val overview = remember(summary, practiceHistory, effectiveStreakDays) {
            buildProgressOverview(summary, practiceHistory, effectiveStreakDays)
        }
        ProgressOverviewCard(overview = overview, strings = strings)
        LevelBreakdownList(
            strings = strings,
            summary = summary,
            onJumpToChar = onJumpToChar,
            onNavigateToCourses = onNavigateToCourses,
        )
        HorizontalDivider()
        PracticeHistorySection(
            strings = strings,
            history = practiceHistory,
            onJumpToChar = onJumpToChar,
        )
    }
}

@Composable
private fun DailyPracticeCard(
    strings: ProgressStrings,
    symbol: String?,
    completedToday: Boolean,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = strings.dailyTitle, style = MaterialTheme.typography.titleMedium)
            Text(
                text = strings.dailyDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = symbol ?: "—",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                )
                Button(onClick = onPractice, enabled = !symbol.isNullOrBlank()) {
                    Text(strings.dailyPracticeLabel)
                }
            }
            if (completedToday) {
                Text(
                    text = strings.dailyCompletedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ProgressOverviewCard(
    overview: ProgressOverview,
    strings: LocalizedStrings,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val progressStrings = strings.progress
            val learnedLabel = progressStrings.learnedLabel
            val streakLabel = progressStrings.streakLabel
            val lastSessionLabel = progressStrings.lastSessionLabel
            val streakValue = when {
                overview.streakDays <= 0 -> progressStrings.streakStartLabel
                overview.streakDays == 1 -> String.format(
                    strings.locale,
                    progressStrings.streakDaySingularFormat,
                    overview.streakDays,
                )
                else -> String.format(
                    strings.locale,
                    progressStrings.streakDayPluralFormat,
                    overview.streakDays,
                )
            }
            val lastSessionValue = overview.lastPracticeTimestamp?.let {
                formatRelativeDuration(progressStrings, strings.locale, it)
            } ?: progressStrings.lastSessionNeverLabel
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatPill(
                    title = learnedLabel,
                    value = "${overview.totalLearned}/${overview.totalCharacters.coerceAtLeast(overview.totalLearned)}",
                )
                StatPill(
                    title = streakLabel,
                    value = streakValue,
                )
                StatPill(
                    title = lastSessionLabel,
                    value = lastSessionValue,
                )
            }
            WeeklyPracticeChart(strings = strings.progress, counts = overview.weeklyCounts)
        }
    }
}

@Composable
private fun RowScope.StatPill(title: String, value: String) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WeeklyPracticeChart(
    strings: ProgressStrings,
    counts: List<DailyPracticeCount>,
) {
    val maxCount = counts.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val barHeight = 56.dp
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = strings.weeklyChartTitle, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(),
        ) {
            counts.forEach { day ->
                val ratio = (day.count / maxCount.toFloat()).coerceIn(0f, 1f)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .height(barHeight)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        if (ratio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(ratio)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        if (day.isToday) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    ),
                            )
                        }
                    }
                    Text(
                        text = day.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (day.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBreakdownList(
    strings: LocalizedStrings,
    summary: HskProgressSummary,
    onJumpToChar: (String) -> Unit,
    onNavigateToCourses: () -> Unit,
) {
    if (summary.perLevel.isEmpty()) {
        Text(
            text = strings.progress.levelsEmpty,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = strings.progress.levelsTitle,
            style = MaterialTheme.typography.labelLarge,
        )
        summary.perLevel.toSortedMap().forEach { (level, stats) ->
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val progressStrings = strings.progress
                    Column {
                        Text(
                            text = String.format(strings.locale, progressStrings.levelLabelFormat, level),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = String.format(
                                strings.locale,
                                progressStrings.levelMasteredFormat,
                                stats.completed,
                                stats.total,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val progress = if (stats.total == 0) 0f else (stats.completed / stats.total.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.width(110.dp),
                    )
                    val nextTarget = summary.nextTargets[level]
                    val isComplete = stats.total > 0 && stats.completed >= stats.total
                    if (!isComplete && nextTarget != null) {
                        IconActionButton(
                            icon = Icons.Filled.PlayArrow,
                            description = progressStrings.levelJumpDescription,
                            onClick = { nextTarget.let(onJumpToChar) },
                            enabled = true,
                            buttonSize = 32.dp,
                        )
                    } else {
                        TextButton(onClick = onNavigateToCourses) {
                            Text(progressStrings.levelBrowseCourses)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeHistorySection(
    strings: LocalizedStrings,
    history: List<PracticeHistoryEntry>,
    onJumpToChar: (String) -> Unit,
) {
    Text(text = strings.progress.historyTitle, style = MaterialTheme.typography.titleMedium)
    if (history.isEmpty()) {
        Text(
            text = strings.progress.historyEmpty,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    history.asReversed().take(12).forEach { entry ->
        PracticeHistoryRow(strings = strings, entry = entry, onJumpToChar = onJumpToChar)
    }
}

@Composable
private fun PracticeHistoryRow(
    strings: LocalizedStrings,
    entry: PracticeHistoryEntry,
    onJumpToChar: (String) -> Unit,
) {
    val timeLabel = remember(entry.timestamp) { formatHistoryTimestamp(entry.timestamp) }
    val progressStrings = strings.progress
    val mistakesLabel = if (entry.mistakes == 0) {
        progressStrings.historyMistakesPerfect
    } else {
        String.format(strings.locale, progressStrings.historyMistakesFormat, entry.mistakes)
    }
    val strokesWithMistakes = String.format(
        strings.locale,
        progressStrings.historyStrokesFormat,
        entry.totalStrokes,
        mistakesLabel,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = entry.symbol,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 32.sp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = strokesWithMistakes,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        val playDescription = String.format(strings.locale, progressStrings.historyLoadCharacterFormat, entry.symbol)
        IconActionButton(
            icon = Icons.Filled.PlayArrow,
            description = playDescription,
            onClick = { onJumpToChar(entry.symbol) },
            buttonSize = 36.dp,
        )
    }
}

private data class ProgressOverview(
    val totalLearned: Int,
    val totalCharacters: Int,
    val streakDays: Int,
    val lastPracticeTimestamp: Long?,
    val weeklyCounts: List<DailyPracticeCount>,
)

private data class DailyPracticeCount(val label: String, val count: Int, val isToday: Boolean)

private fun buildProgressOverview(
    summary: HskProgressSummary,
    history: List<PracticeHistoryEntry>,
    streakDays: Int,
): ProgressOverview {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val dayFormatter = DateTimeFormatter.ofPattern("EE", Locale.getDefault())
    val grouped = history.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
    val weeklyCounts = (6 downTo 0).map { offset ->
        val day = today.minusDays(offset.toLong())
        DailyPracticeCount(
            label = dayFormatter.format(day),
            count = grouped[day]?.size ?: 0,
            isToday = day == today,
        )
    }
    return ProgressOverview(
        totalLearned = summary.totalCompleted,
        totalCharacters = summary.totalCharacters.takeIf { it > 0 } ?: summary.totalCompleted,
        streakDays = streakDays,
        lastPracticeTimestamp = history.maxOfOrNull { it.timestamp },
        weeklyCounts = weeklyCounts,
    )
}

private fun formatRelativeDuration(
    strings: ProgressStrings,
    locale: Locale,
    timestamp: Long,
): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis <= 0) return strings.relativeJustNow
    val minutes = diffMillis / 60_000
    return when {
        minutes < 1 -> strings.relativeJustNow
        minutes < 60 -> String.format(locale, strings.relativeMinutesFormat, minutes)
        minutes < 60 * 24 -> String.format(locale, strings.relativeHoursFormat, minutes / 60)
        else -> String.format(locale, strings.relativeDaysFormat, minutes / (60 * 24))
    }
}

private fun formatHistoryTimestamp(timestamp: Long): String {
    return try {
        val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } catch (_: Exception) {
        "-"
    }
}
