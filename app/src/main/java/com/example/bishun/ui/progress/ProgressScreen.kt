package com.example.bishun.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    onJumpToPractice: () -> Unit = {},
) {
    val mockHistory = remember {
        listOf(
            ProgressHighlight("今日目标", "完成 3 个新字的描红与演示。"),
            ProgressHighlight("连胜记录", "保持 5 日练字打卡，坚持下去！"),
            ProgressHighlight("错题提醒", "最近 2 个字的第 3 笔容易出错，可在课程页重练。"),
        )
    }
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
                    text = "正式版本会在此呈现练习历史、HSK 完成度与周统计。当前仅提供示意卡片。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onJumpToPractice) {
                    Text("继续练习")
                }
            }
        }
        items(mockHistory) { highlight ->
            ProgressCard(highlight = highlight)
        }
    }
}

@Composable
private fun ProgressCard(highlight: ProgressHighlight) {
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
                text = highlight.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Divider()
            Text(
                text = highlight.detail,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private data class ProgressHighlight(
    val title: String,
    val detail: String,
)
