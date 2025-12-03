package com.example.bishun.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
            text = "字典",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "离线字典数据来自 word.json，可在未联网时查询汉字的拼音、部首、笔画等信息。输入汉字即可查看详情，并可一键跳到“练习”标签继续描红。",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::updateQuery,
            label = { Text("汉字") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("输入 1 个汉字。HSK1 免费课程无须登录即可练习。") },
        )
        RowActions(
            isLoading = uiState.isLoading,
            hasResult = uiState.result != null,
            onSearch = viewModel::submitQuery,
            onClear = viewModel::clearResult,
        )
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
                onPractice = {
                    onLoadInPractice(entry.word)
                },
            )
        }
        HelpCard()
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
            Text(if (isLoading) "搜索中..." else "查询")
        }
        OutlinedButton(onClick = onClear, enabled = hasResult) {
            Text("清空结果")
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
            Text("拼音：${entry.pinyin.ifBlank { "暂无" }}")
            Text("部首：${entry.radicals.ifBlank { "暂无" }}    笔画：${entry.strokes.ifBlank { "?" }}")
            Text(
                text = entry.explanation.ifBlank { entry.more.ifBlank { "暂无释义" } },
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPractice) {
                Text("在练习页描红")
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
                text = "使用说明",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = "• 数据来自内置的 word.json，可离线查询。\n" +
                    "• 可在“账户”标签解锁更多课程，与字典联动。\n" +
                    "• 更详细的手写与帮助内容请查看 docs/help-guide.md 中的教程。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
