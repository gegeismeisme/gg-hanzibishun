package com.example.bishun.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import com.example.bishun.data.history.PracticeHistoryEntry
import com.example.bishun.ui.character.CharacterViewModel
import com.example.bishun.ui.character.HskLevelSummary
import com.example.bishun.ui.character.HskProgressSummary
import com.example.bishun.ui.character.components.IconActionButton
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
) {
    val hskProgress by viewModel.hskProgress.collectAsState()
    val practiceHistory by viewModel.practiceHistory.collectAsState()

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "进度",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "实时查看已掌握的 HSK 字符、连胜记录与最近的练习历史。下方列表会随着练习自动更新。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onJumpToPractice) {
                    Text("继续练习")
                }
            }
        }
        item {
            HskProgressView(
                summary = hskProgress,
                practiceHistory = practiceHistory,
                onJumpToChar = onJumpToCharacter,
            )
        }
    }
}

@Composable
private fun HskProgressView(
    summary: HskProgressSummary,
    practiceHistory: List<PracticeHistoryEntry>,
    onJumpToChar: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 520.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val overview = remember(summary, practiceHistory) { buildProgressOverview(summary, practiceHistory) }
        ProgressOverviewCard(overview = overview)
        LevelBreakdownList(summary = summary)
        HorizontalDivider()
        PracticeHistorySection(history = practiceHistory, onJumpToChar = onJumpToChar)
    }
}

@Composable
private fun ProgressOverviewCard(overview: ProgressOverview) {
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatPill(
                    title = "已掌握",
                    value = "${overview.totalLearned}/${overview.totalCharacters.coerceAtLeast(overview.totalLearned)}",
                )
                StatPill(
                    title = "连胜",
                    value = if (overview.streakDays > 0) "${overview.streakDays}d" else "Start",
                )
                StatPill(
                    title = "上次练习",
                    value = overview.lastPracticeTimestamp?.let { formatRelativeDuration(it) } ?: "—",
                )
            }
            WeeklyPracticeChart(counts = overview.weeklyCounts)
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
private fun WeeklyPracticeChart(counts: List<DailyPracticeCount>) {
    val maxCount = counts.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val barHeight = 56.dp
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("近 7 天", style = MaterialTheme.typography.labelLarge)
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
private fun LevelBreakdownList(summary: HskProgressSummary) {
    if (summary.perLevel.isEmpty()) {
        Text(
            text = "开始练字后会在此展示 HSK 等级进度。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "各级别概况",
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
                    Column {
                        Text("HSK $level", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${stats.completed}/${stats.total} mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val progress = if (stats.total == 0) 0f else (stats.completed / stats.total.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.width(110.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PracticeHistorySection(
    history: List<PracticeHistoryEntry>,
    onJumpToChar: (String) -> Unit,
) {
    Text("最近练习", style = MaterialTheme.typography.titleMedium)
    if (history.isEmpty()) {
        Text(
            text = "完成一次描红后，这里会显示练习记录。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    history.asReversed().take(12).forEach { entry ->
        PracticeHistoryRow(entry = entry, onJumpToChar = onJumpToChar)
    }
}

@Composable
private fun PracticeHistoryRow(
    entry: PracticeHistoryEntry,
    onJumpToChar: (String) -> Unit,
) {
    val timeLabel = remember(entry.timestamp) { formatHistoryTimestamp(entry.timestamp) }
    val mistakesLabel = if (entry.mistakes == 0) {
        "Perfect run"
    } else {
        "${entry.mistakes} mistakes"
    }
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
                    text = "${entry.totalStrokes} strokes • $mistakesLabel",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        IconActionButton(
            icon = Icons.Filled.PlayArrow,
            description = "Load ${entry.symbol}",
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
): ProgressOverview {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    val dayFormatter = DateTimeFormatter.ofPattern("EE", Locale.getDefault())
    val grouped = history.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
    val sortedDays = grouped.keys.sortedDescending()
    var streak = 0
    var previousDay: LocalDate? = null
    loop@ for (day in sortedDays) {
        if (previousDay == null) {
            streak = 1
            previousDay = day
            continue@loop
        }
        val diff = ChronoUnit.DAYS.between(day, previousDay)
        when {
            diff == 0L -> continue@loop
            diff == 1L -> {
                streak++
                previousDay = day
            }
            else -> break@loop
        }
    }
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
        streakDays = streak,
        lastPracticeTimestamp = history.maxOfOrNull { it.timestamp },
        weeklyCounts = weeklyCounts,
    )
}

private fun formatRelativeDuration(timestamp: Long): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    if (diffMillis <= 0) return "just now"
    val minutes = diffMillis / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 60 * 24 -> "${minutes / 60}h ago"
        else -> "${minutes / (60 * 24)}d ago"
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
