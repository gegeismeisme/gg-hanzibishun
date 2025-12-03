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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    onRequestUnlock: () -> Unit = {},
) {
    val mockCourses = remember {
        listOf(
            CoursePreview("HSK 1", "基础笔画与入门字形，建议先观看演示再练习。", 25, true),
            CoursePreview("HSK 2", "巩固基础字形，未购买时提供预览内容。", 32, false),
            CoursePreview("HSK 3", "偏旁部首与结构拆解，含彩色引导。", 40, false),
        )
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
                    text = "所有课程默认以预览模式显示。登录并完成购买后，可离线解锁完整内容。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(mockCourses) { course ->
            CoursePreviewCard(
                course = course,
                onRequestUnlock = onRequestUnlock,
            )
        }
    }
}

@Composable
private fun CoursePreviewCard(
    course: CoursePreview,
    onRequestUnlock: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = course.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = "${course.lessons} 课时",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = if (course.isUnlocked) "已解锁" else "预览",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (course.isUnlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Text(
                text = course.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            val buttonLabel = if (course.isUnlocked) "前往“练习”页" else "解锁课程"
            Button(
                onClick = { if (!course.isUnlocked) onRequestUnlock() },
                enabled = !course.isUnlocked,
            ) {
                Text(buttonLabel)
            }
        }
    }
}

private data class CoursePreview(
    val title: String,
    val description: String,
    val lessons: Int,
    val isUnlocked: Boolean,
)
