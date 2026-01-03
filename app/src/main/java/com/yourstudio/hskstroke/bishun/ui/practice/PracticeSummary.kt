package com.yourstudio.hskstroke.bishun.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.ui.character.CoursesStrings
import com.yourstudio.hskstroke.bishun.ui.character.PracticeState
import com.yourstudio.hskstroke.bishun.ui.character.PracticeBoardStrings
import com.yourstudio.hskstroke.bishun.ui.character.PracticeStatus
import com.yourstudio.hskstroke.bishun.ui.character.components.IconActionButton
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

data class PracticeSummaryUi(
    val progressText: String,
    val statusText: String,
)

fun PracticeState.toSummary(
    boardStrings: PracticeBoardStrings,
    locale: Locale,
): PracticeSummaryUi {
    val normalizedTotal = max(1, totalStrokes)
    val completedCount = when {
        isComplete -> normalizedTotal
        currentStrokeIndex <= 0 -> 0
        else -> min(currentStrokeIndex, normalizedTotal)
    }
    val defaultStatus = when {
        isComplete -> boardStrings.statusPracticeCompleteLabel
        isActive -> String.format(locale, boardStrings.statusStrokeProgressFormat, completedCount + 1, normalizedTotal)
        else -> boardStrings.statusReadyLabel
    }
    val resolvedStatus = when (val state = status) {
        PracticeStatus.None -> defaultStatus
        is PracticeStatus.StartFromStroke -> String.format(
            locale,
            boardStrings.statusStartFromStrokeFormat,
            state.strokeNumber,
        )
        is PracticeStatus.TryAgain -> String.format(
            locale,
            boardStrings.statusTryAgainFormat,
            state.mistakes,
        )
        PracticeStatus.GreatContinue -> boardStrings.statusGreatContinueLabel
        PracticeStatus.BackwardsAccepted -> boardStrings.statusBackwardsAcceptedLabel
        PracticeStatus.Complete -> boardStrings.statusPracticeCompleteLabel
    }
    return PracticeSummaryUi(
        progressText = "$completedCount/$normalizedTotal",
        statusText = resolvedStatus,
    )
}

@Composable
fun PracticeSummaryBadge(
    progressText: String,
    statusText: String,
    sequenceSymbol: String?,
    sequenceProgressText: String?,
    sequenceHasPrevious: Boolean,
    sequenceHasNext: Boolean,
    onSequencePrevious: (() -> Unit)?,
    onSequenceNext: (() -> Unit)?,
    onSequenceRestart: (() -> Unit)?,
    onSequenceExit: (() -> Unit)?,
    courseStrings: CoursesStrings,
    boardStrings: PracticeBoardStrings,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
        modifier = modifier,
    ) {
        if (sequenceSymbol == null || sequenceProgressText == null) {
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
                        text = String.format(
                            locale,
                            courseStrings.activeCourseStatusFormat,
                            sequenceSymbol,
                            sequenceProgressText,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                val buttonSize = 28.dp
                IconActionButton(
                    icon = Icons.Filled.SkipPrevious,
                    description = boardStrings.previousLabel,
                    onClick = { onSequencePrevious?.invoke() },
                    enabled = onSequencePrevious != null && sequenceHasPrevious,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.SkipNext,
                    description = boardStrings.nextLabel,
                    onClick = { onSequenceNext?.invoke() },
                    enabled = onSequenceNext != null && sequenceHasNext,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.RestartAlt,
                    description = courseStrings.activeRestart,
                    onClick = { onSequenceRestart?.invoke() },
                    enabled = onSequenceRestart != null,
                    buttonSize = buttonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.Close,
                    description = courseStrings.activeExit,
                    onClick = { onSequenceExit?.invoke() },
                    enabled = onSequenceExit != null,
                    buttonSize = buttonSize,
                )
            }
        }
    }
}
