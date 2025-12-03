package com.example.bishun.ui.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bishun.ui.account.AccountViewModel
import com.example.bishun.ui.character.CharacterViewModel
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.HskLevelSummary

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel,
    isSignedIn: Boolean,
    unlockedLevels: Set<Int>,
    onNavigateToPractice: () -> Unit = {},
    onRequestUnlock: () -> Unit = {},
) {
    val courseCatalog by viewModel.courseCatalog.collectAsState()
    val courseSession by viewModel.courseSession.collectAsState()
    val hskProgress by viewModel.hskProgress.collectAsState()
    val completedSymbols by viewModel.completedSymbols.collectAsState()

    val sortedLevels = remember(courseCatalog) {
        courseCatalog.entries.sortedBy { it.key }
    }

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "课程",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "课程在未购买时保持预览，解锁后可完全离线使用。请在“账户”标签中完成登录/购买以解锁更多内容。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        courseSession?.let { session ->
            item {
                ActiveCourseCard(
                    session = session,
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
        if (courseCatalog.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Text(
                        text = "未找到课程数据，请确认 assets/learn-datas 已正确打包。",
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
                val nextTarget = hskProgress.nextTargets[level]
                    ?: symbols.firstOrNull { !completedSymbols.contains(it) }
                    ?: symbols.firstOrNull()
                val canAccess = AccountViewModel.FREE_LEVELS.contains(level) ||
                    unlockedLevels.contains(level)
                CourseLevelCard(
                    level = level,
                    symbols = symbols,
                    summary = summary,
                    nextTarget = nextTarget,
                    completedSymbols = completedSymbols,
                    canAccess = canAccess,
                    isSignedIn = isSignedIn,
                    onStart = { symbol ->
                        viewModel.startCourse(level, symbol)
                        onNavigateToPractice()
                    },
                    onRequestUnlock = onRequestUnlock,
                )
            }
        }
    }
}

@Composable
private fun ActiveCourseCard(
    session: CourseSession,
    onResume: () -> Unit,
    onSkip: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "正在练习：HSK ${session.level}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "当前：${session.currentSymbol ?: "-"} · 进度 ${session.progressText}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(onClick = onResume, enabled = session.currentSymbol != null) {
                    Text("继续课程")
                }
                OutlinedButton(onClick = onSkip, enabled = session.hasNext) {
                    Text("跳过")
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onRestart) {
                    Text("重新开始")
                }
                OutlinedButton(onClick = onExit) {
                    Text("退出课程")
                }
            }
        }
    }
}

@Composable
private fun CourseLevelCard(
    level: Int,
    symbols: List<String>,
    summary: HskLevelSummary?,
    nextTarget: String?,
    completedSymbols: Set<String>,
    canAccess: Boolean,
    isSignedIn: Boolean,
    onStart: (String) -> Unit,
    onRequestUnlock: () -> Unit,
) {
    val total = summary?.total ?: symbols.size
    val completed = summary?.completed ?: symbols.count { completedSymbols.contains(it) }
    val remaining = summary?.remaining ?: (total - completed).coerceAtLeast(0)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "HSK $level",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = "$completed / $total 已掌握 · 剩余 $remaining",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text(
                    text = nextTarget?.let { "下一个：$it" } ?: "暂无数据",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = "示例：${symbols.take(12).joinToString(separator = " ")}",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (canAccess) {
                Button(
                    onClick = { nextTarget?.let(onStart) },
                    enabled = nextTarget != null,
                ) {
                    Text(if (completed == 0) "开始课程" else "练习下一个")
                }
            } else {
                Button(onClick = onRequestUnlock) {
                    Text(if (isSignedIn) "解锁课程" else "登录后解锁")
                }
            }
        }
    }
}
