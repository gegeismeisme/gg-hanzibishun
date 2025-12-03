package com.example.bishun.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.PracticeState
import com.example.bishun.ui.character.components.IconActionButton
import kotlin.math.max
import kotlin.math.min

data class PracticeSummaryUi(
    val progressText: String,
    val statusText: String,
)

fun PracticeState.toSummary(): PracticeSummaryUi {
    val normalizedTotal = max(1, totalStrokes)
    val completedCount = when {
        isComplete -> normalizedTotal
        currentStrokeIndex <= 0 -> 0
        else -> min(currentStrokeIndex, normalizedTotal)
    }
    val defaultStatus = when {
        isComplete -> "Practice complete"
        isActive -> "Stroke ${completedCount + 1}/$normalizedTotal"
        else -> "Ready to start"
    }
    val status = statusMessage.ifBlank { defaultStatus }
    return PracticeSummaryUi(progressText = "$completedCount/$normalizedTotal", statusText = status)
}

@Composable
fun PracticeSummaryBadge(
    progressText: String,
    statusText: String,
    courseSession: CourseSession?,
    onCourseResume: (() -> Unit)?,
    onCourseSkip: (() -> Unit)?,
    onCourseRestart: (() -> Unit)?,
    onCourseExit: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val containerColor = Color(0xFFE5F4EA)
    val contentColor = Color(0xFF1E4620)
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        if (courseSession == null) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = "HSK ${courseSession.level} • ${courseSession.progressText}",
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val buttonSize = 28.dp
                IconActionButton(
                    icon = Icons.Filled.PlayArrow,
                    description = "Resume course",
                    onClick = { onCourseResume?.invoke() },
                    enabled = onCourseResume != null,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.SkipNext,
                    description = "Skip character",
                    onClick = { onCourseSkip?.invoke() },
                    enabled = onCourseSkip != null,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.RestartAlt,
                    description = "Restart level",
                    onClick = { onCourseRestart?.invoke() },
                    enabled = onCourseRestart != null,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.Close,
                    description = "Exit course",
                    onClick = { onCourseExit?.invoke() },
                    enabled = onCourseExit != null,
                    buttonSize = buttonSize,
                )
            }
        }
    }
}
