package com.yourstudio.hskstroke.bishun.ui.practice

import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.ui.audio.VolumeSafetyDialog
import com.yourstudio.hskstroke.bishun.ui.character.AccountStrings
import com.yourstudio.hskstroke.bishun.ui.character.CourseSession
import com.yourstudio.hskstroke.bishun.ui.character.CoursesStrings
import com.yourstudio.hskstroke.bishun.ui.character.LibraryStrings
import com.yourstudio.hskstroke.bishun.ui.character.PracticeQueueSession
import com.yourstudio.hskstroke.bishun.ui.character.PracticeState
import com.yourstudio.hskstroke.bishun.ui.character.PracticeBoardStrings
import com.yourstudio.hskstroke.bishun.ui.character.WordInfoUiState
import com.yourstudio.hskstroke.bishun.ui.character.components.IconActionButton
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PracticeContent(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    courseSession: CourseSession?,
    practiceQueueSession: PracticeQueueSession?,
    boardSettings: BoardSettings,
    isDemoPlaying: Boolean,
    wordEntry: WordEntry?,
    wordInfoUiState: WordInfoUiState,
    onRequestWordInfo: (String) -> Unit,
    hskEntry: HskEntry?,
    showTemplate: Boolean,
    libraryStrings: LibraryStrings,
    accountStrings: AccountStrings,
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
    onPracticeQueueNext: () -> Unit,
    onPracticeQueuePrev: () -> Unit,
    onPracticeQueueRestart: () -> Unit,
    onPracticeQueueExit: () -> Unit,
    courseStrings: CoursesStrings,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    var showWordInfo by rememberSaveable(definition.symbol) { mutableStateOf(false) }
    var showHskDialog by rememberSaveable { mutableStateOf(false) }
    val ttsController = rememberTextToSpeechController()
    var showVolumeSafetyDialog by rememberSaveable { mutableStateOf(false) }
    var volumeAtDialog by rememberSaveable { mutableStateOf(0) }
    var pendingSpeak by remember { mutableStateOf<(() -> Unit)?>(null) }
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(AudioManager::class.java) }
    val preferencesStore = remember { UserPreferencesStore(context.applicationContext) }
    val userPreferences by preferencesStore.data.collectAsState(initial = UserPreferences())

    val requestSpeakSafely: (String) -> Unit = request@{ text ->
        if (!ttsController.isAvailable.value) return@request
        val speakNow = { ttsController.speak(text) }
        val manager = audioManager
        if (manager == null || !userPreferences.volumeSafetyEnabled) {
            speakNow()
            return@request
        }
        val maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(0)
        val currentPercent = (currentVolume * 100f / maxVolume).roundToInt().coerceIn(0, 100)
        volumeAtDialog = currentPercent
        if (currentPercent >= userPreferences.volumeSafetyThresholdPercent.coerceIn(0, 100)) {
            pendingSpeak = speakNow
            showVolumeSafetyDialog = true
        } else {
            speakNow()
        }
    }
    LaunchedEffect(showWordInfo, definition.symbol) {
        if (showWordInfo && wordInfoUiState is WordInfoUiState.Idle) {
            onRequestWordInfo(definition.symbol)
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxSize(),
    ) {
        val summary = practiceState.toSummary(boardStrings, locale)
        val activeSequenceSymbol = practiceQueueSession?.currentSymbol ?: courseSession?.currentSymbol
        val activeSequenceProgress = practiceQueueSession?.progressText ?: courseSession?.progressText
        val activeHasPrev = practiceQueueSession?.hasPrevious ?: courseSession?.hasPrevious ?: false
        val activeHasNext = practiceQueueSession?.hasNext ?: courseSession?.hasNext ?: false
        val onSequencePrev = when {
            practiceQueueSession != null -> onPracticeQueuePrev
            courseSession != null -> onCoursePrev
            else -> null
        }
        val onSequenceNext = when {
            practiceQueueSession != null -> onPracticeQueueNext
            courseSession != null -> onCourseNext
            else -> null
        }
        val onSequenceRestart = when {
            practiceQueueSession != null -> onPracticeQueueRestart
            courseSession != null -> onCourseRestart
            else -> null
        }
        val onSequenceExit = when {
            practiceQueueSession != null -> onPracticeQueueExit
            courseSession != null -> onCourseExit
            else -> null
        }
        PracticeSummaryBadge(
            progressText = summary.progressText,
            statusText = summary.statusText,
            sequenceSymbol = activeSequenceSymbol,
            sequenceProgressText = activeSequenceProgress,
            sequenceHasPrevious = activeHasPrev,
            sequenceHasNext = activeHasNext,
            onSequencePrevious = onSequencePrev,
            onSequenceNext = onSequenceNext,
            onSequenceRestart = onSequenceRestart,
            onSequenceExit = onSequenceExit,
            courseStrings = courseStrings,
            boardStrings = boardStrings,
            locale = locale,
            modifier = Modifier.fillMaxWidth(),
        )
        val gridMode = boardSettings.grid
        val strokeColorOption = boardSettings.strokeColor
        val strokeColor = strokeColorOption.color
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
        ) {
            val boardSize = if (maxWidth < maxHeight) maxWidth else maxHeight
            val boardControlButtonSize = 36.dp
            val boardControlSpacing = 12.dp
            val sideSlotWidth = boardControlButtonSize + boardControlSpacing
            val canPlaceSideControls = maxWidth >= boardSize + sideSlotWidth + sideSlotWidth

            Box(modifier = Modifier.fillMaxSize()) {
                if (canPlaceSideControls) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Spacer(modifier = Modifier.width(sideSlotWidth))
                        Box(modifier = Modifier.size(boardSize)) {
                            CharacterCanvas(
                                definition = definition,
                                renderSnapshot = renderSnapshot,
                                practiceState = practiceState,
                                gridMode = gridMode,
                                userStrokeColor = strokeColor,
                                showTemplate = showTemplate,
                                calligraphyDemoProgress = calligraphyDemoState.strokeProgress,
                                onStrokeStart = onStrokeStart,
                                onStrokeMove = onStrokeMove,
                                onStrokeEnd = onStrokeEnd,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(modifier = Modifier.width(boardControlSpacing))
                        Column(
                            modifier = Modifier
                                .width(boardControlButtonSize)
                                .padding(top = boardControlSpacing),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (hskEntry != null) {
                                IconActionButton(
                                    icon = Icons.Filled.School,
                                    description = boardStrings.hskLabel,
                                    onClick = { showHskDialog = true },
                                    buttonSize = boardControlButtonSize,
                                )
                            }
                            CanvasSettingsMenu(
                                currentGrid = gridMode,
                                currentColor = strokeColorOption,
                                showTemplate = showTemplate,
                                onGridChange = onGridModeChange,
                                onColorChange = onStrokeColorChange,
                                onTemplateToggle = { enabled ->
                                    onTemplateToggle(enabled)
                                    if (!enabled) {
                                        onStopCalligraphyDemo()
                                    }
                                },
                                description = boardStrings.settingsLabel,
                                buttonSize = boardControlButtonSize,
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(boardSize),
                    ) {
                        CharacterCanvas(
                            definition = definition,
                            renderSnapshot = renderSnapshot,
                            practiceState = practiceState,
                            gridMode = gridMode,
                            userStrokeColor = strokeColor,
                            showTemplate = showTemplate,
                            calligraphyDemoProgress = calligraphyDemoState.strokeProgress,
                            onStrokeStart = onStrokeStart,
                            onStrokeMove = onStrokeMove,
                            onStrokeEnd = onStrokeEnd,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (hskEntry != null) {
                            IconActionButton(
                                icon = Icons.Filled.School,
                                description = boardStrings.hskLabel,
                                onClick = { showHskDialog = true },
                                buttonSize = boardControlButtonSize,
                            )
                        }
                        CanvasSettingsMenu(
                            currentGrid = gridMode,
                            currentColor = strokeColorOption,
                            showTemplate = showTemplate,
                            onGridChange = onGridModeChange,
                            onColorChange = onStrokeColorChange,
                            onTemplateToggle = { enabled ->
                                onTemplateToggle(enabled)
                                if (!enabled) {
                                    onStopCalligraphyDemo()
                                }
                            },
                            description = boardStrings.settingsLabel,
                            buttonSize = boardControlButtonSize,
                        )
                    }
                }
            }
        }
        PracticeBottomBar(
            primaryLabel = if (practiceState.isActive) boardStrings.hintLabel else boardStrings.startLabel,
            onPrimaryClick = if (practiceState.isActive) onRequestHint else onStartPractice,
            primaryEnabled = !isDemoPlaying,
            onPlayPronunciation = { requestSpeakSafely(wordEntry?.word ?: definition.symbol) },
            pronunciationEnabled = !isDemoPlaying && ttsController.isAvailable.value,
            onShowDictionary = {
                showWordInfo = true
                onRequestWordInfo(definition.symbol)
            },
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
    if (showWordInfo) {
        WordInfoBottomSheet(
            symbol = definition.symbol,
            entry = wordEntry,
            wordInfoUiState = wordInfoUiState,
            strings = libraryStrings,
            accountStrings = accountStrings,
            boardStrings = boardStrings,
            onRetry = { onRequestWordInfo(definition.symbol) },
            onDismiss = { showWordInfo = false },
            onPlayPronunciation = { requestSpeakSafely(wordEntry?.word ?: definition.symbol) },
            pronunciationEnabled = ttsController.isAvailable.value,
            ttsController = ttsController,
        )
    }
    if (showHskDialog && hskEntry != null) {
        HskInfoDialog(
            entry = hskEntry,
            onDismiss = { showHskDialog = false },
        )
    }
    if (showVolumeSafetyDialog && audioManager != null) {
        VolumeSafetyDialog(
            currentVolumePercent = volumeAtDialog,
            thresholdPercent = userPreferences.volumeSafetyThresholdPercent.coerceIn(0, 100),
            lowerToPercent = userPreferences.volumeSafetyLowerToPercent.coerceIn(0, 100),
            onCheckVolume = {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_SAME,
                    AudioManager.FLAG_SHOW_UI,
                )
                showVolumeSafetyDialog = false
                pendingSpeak = null
            },
            onLowerAndPlay = {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                val target = (maxVolume * (userPreferences.volumeSafetyLowerToPercent / 100f))
                    .roundToInt()
                    .coerceIn(0, maxVolume)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                showVolumeSafetyDialog = false
                pendingSpeak?.invoke()
                pendingSpeak = null
            },
            onDismiss = {
                showVolumeSafetyDialog = false
                pendingSpeak = null
            },
        )
    }
}
