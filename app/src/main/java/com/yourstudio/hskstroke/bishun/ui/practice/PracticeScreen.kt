package com.yourstudio.hskstroke.bishun.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.ui.character.CourseSession
import com.yourstudio.hskstroke.bishun.ui.character.CoursesStrings
import com.yourstudio.hskstroke.bishun.ui.character.PracticeState
import com.yourstudio.hskstroke.bishun.ui.character.PracticeBoardStrings
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun PracticeContent(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    courseSession: CourseSession?,
    boardSettings: BoardSettings,
    isDemoPlaying: Boolean,
    wordEntry: WordEntry?,
    hskEntry: HskEntry?,
    showTemplate: Boolean,
    boardStrings: PracticeBoardStrings,
    onTemplateToggle: (Boolean) -> Unit,
    calligraphyDemoState: CalligraphyDemoState,
    onStopCalligraphyDemo: () -> Unit,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onCourseNext: () -> Unit,
    onCoursePrev: () -> Unit,
    onCourseSkip: () -> Unit,
    onCourseRestart: () -> Unit,
    onResumeCourse: (String) -> Unit,
    onCourseExit: () -> Unit,
    courseStrings: CoursesStrings,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    var showWordInfo by rememberSaveable(definition.symbol) { mutableStateOf(false) }
    var showHskHint by rememberSaveable(definition.symbol) { mutableStateOf(false) }
    var showHskIcon by rememberSaveable(definition.symbol) { mutableStateOf(false) }
    var showHskDialog by rememberSaveable { mutableStateOf(false) }
    val ttsController = rememberTextToSpeechController()
    if (wordEntry == null && showWordInfo) {
        showWordInfo = false
    }
    LaunchedEffect(definition.symbol, hskEntry) {
        if (hskEntry != null) {
            showHskIcon = false
            showHskHint = true
            delay(3500)
            showHskHint = false
            showHskIcon = true
        } else {
            showHskHint = false
            showHskIcon = false
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        val summary = practiceState.toSummary()
        PracticeSummaryBadge(
            progressText = summary.progressText,
            statusText = summary.statusText,
            courseSession = courseSession,
            onCourseResume = {
                val targetSymbol = courseSession?.currentSymbol ?: definition.symbol
                onResumeCourse(targetSymbol)
            },
            onCourseSkip = onCourseSkip,
            onCourseRestart = onCourseRestart,
            onCourseExit = onCourseExit,
            courseStrings = courseStrings,
            locale = locale,
            modifier = Modifier.fillMaxWidth(),
        )
        val gridMode = boardSettings.grid
        val strokeColorOption = boardSettings.strokeColor
        val strokeColor = strokeColorOption.color
        CharacterCanvas(
            definition = definition,
            renderSnapshot = renderSnapshot,
            practiceState = practiceState,
            courseSession = courseSession,
            isDemoPlaying = isDemoPlaying,
            gridMode = gridMode,
            userStrokeColor = strokeColor,
            showTemplate = showTemplate,
            calligraphyDemoProgress = calligraphyDemoState.strokeProgress,
            hskEntry = hskEntry,
            showHskHint = showHskHint,
            showHskIcon = showHskIcon && hskEntry != null,
            onHskInfoClick = { showHskDialog = true },
            boardStrings = boardStrings,
            currentColorOption = strokeColorOption,
            onGridModeChange = onGridModeChange,
            onStrokeColorChange = onStrokeColorChange,
            onTemplateToggle = {
                onTemplateToggle(it)
                if (!it) {
                    onStopCalligraphyDemo()
                }
            },
            onStrokeStart = onStrokeStart,
            onStrokeMove = onStrokeMove,
            onStrokeEnd = onStrokeEnd,
            onStartPractice = onStartPractice,
            onRequestHint = onRequestHint,
            onCourseNext = onCourseNext,
            onCoursePrev = onCoursePrev,
            onCourseSkip = onCourseSkip,
            onCourseRestart = onCourseRestart,
            onCourseExit = onCourseExit,
            onWordInfoClick = { if (wordEntry != null) showWordInfo = true },
            wordInfoAvailable = wordEntry != null,
            onPlayPronunciation = { wordEntry?.let { ttsController.speak(it.word) } },
            pronunciationAvailable = wordEntry != null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
        )
    }
    if (showWordInfo && wordEntry != null) {
        WordInfoBottomSheet(
            entry = wordEntry,
            onDismiss = { showWordInfo = false },
            ttsController = ttsController,
        )
    }
    if (showHskDialog && hskEntry != null) {
        HskInfoDialog(
            entry = hskEntry,
            onDismiss = { showHskDialog = false },
        )
    }
}
