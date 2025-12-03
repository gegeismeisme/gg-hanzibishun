package com.example.bishun.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    onStartSearch: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "字典",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "后续会将离线字典（`WordRepository`）迁移到专用界面，可按字、拼音、部首检索。现阶段请在“练习”页输入汉字继续探索。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onStartSearch) {
            Text("在练习页查询")
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "功能路线图",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "• 离线字典数据来自 assets/word/word.json。\n" +
                        "• 登录解锁后可显示更多课程关联词条。\n" +
                        "• 计划支持手写反查与语音播放（沿用 TextToSpeech 控制器）。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
