package com.yourstudio.hskstroke.bishun.ui.character

import android.app.Activity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressSummary
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import android.widget.Toast
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.ui.character.components.IconActionButton
import com.yourstudio.hskstroke.bishun.ui.practice.BoardSettings
import com.yourstudio.hskstroke.bishun.ui.practice.CalligraphyDemoState
import com.yourstudio.hskstroke.bishun.ui.practice.PracticeContent
import com.yourstudio.hskstroke.bishun.ui.practice.PracticeErrorBanner
import com.yourstudio.hskstroke.bishun.ui.practice.PracticeGrid
import com.yourstudio.hskstroke.bishun.ui.practice.StrokeColorOption
import com.yourstudio.hskstroke.bishun.ui.practice.SearchBarRow
import com.yourstudio.hskstroke.bishun.ui.practice.rememberCalligraphyDemoController
import com.yourstudio.hskstroke.bishun.ui.testing.TestTags
import java.time.LocalDate
import java.time.ZoneId

private enum class HomePracticeTab {
    Practice,
    Daily,
}
@Composable
fun CharacterRoute(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel = viewModel(
        factory = CharacterViewModel.factory(LocalContext.current),
    ),
) {
    val query by viewModel.query.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val renderSnapshot by viewModel.renderSnapshot.collectAsState()
    val practiceState by viewModel.practiceState.collectAsState()
    val demoState by viewModel.demoState.collectAsState()
    val wordEntry by viewModel.wordEntry.collectAsState()
    val wordInfoUiState by viewModel.wordInfoUiState.collectAsState()
    val hskEntry by viewModel.hskEntry.collectAsState()
    val hskProgress by viewModel.hskProgress.collectAsState()
    val courseSession by viewModel.courseSession.collectAsState()
    val practiceQueueSession by viewModel.practiceQueueSession.collectAsState()
    val boardSettings by viewModel.boardSettings.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.courseEvents.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(
        userPreferences.dailySymbol,
        userPreferences.dailyEpochDay,
        userPreferences.dailyPinyin,
        userPreferences.dailyExplanationSummary,
    ) {
        viewModel.ensureDailyPracticeDetailsLoaded()
    }
    CharacterScreen(
        modifier = modifier,
        query = query,
        uiState = uiState,
        practiceState = practiceState,
        renderSnapshot = renderSnapshot,
        demoState = demoState,
        wordEntry = wordEntry,
        wordInfoUiState = wordInfoUiState,
        hskEntry = hskEntry,
        hskProgress = hskProgress,
        courseSession = courseSession,
        practiceQueueSession = practiceQueueSession,
        boardSettings = boardSettings,
        dailySymbol = userPreferences.dailySymbol,
        dailyEpochDay = userPreferences.dailyEpochDay,
        dailyPinyin = userPreferences.dailyPinyin,
        dailyExplanationSummary = userPreferences.dailyExplanationSummary,
        dailyPracticeCompletedSymbol = userPreferences.dailyPracticeCompletedSymbol,
        dailyPracticeCompletedEpochDay = userPreferences.dailyPracticeCompletedEpochDay,
        onQueryChange = viewModel::updateQuery,
        onSubmit = viewModel::submitQuery,
        onClearQuery = viewModel::clearQuery,
        onPlayDemoOnce = { viewModel.playDemo(loop = false) },
        onPlayDemoLoop = { viewModel.playDemo(loop = true) },
        onStopDemo = viewModel::stopDemo,
        onStartPractice = viewModel::startPractice,
        onStartDailyPractice = viewModel::startDailyPractice,
        onRequestHint = viewModel::requestHint,
        onRequestWordInfo = viewModel::requestWordInfo,
        onStrokeStart = viewModel::onPracticeStrokeStart,
        onStrokeMove = viewModel::onPracticeStrokeMove,
        onStrokeEnd = viewModel::onPracticeStrokeEnd,
        onGridModeChange = viewModel::updateGridMode,
        onStrokeColorChange = viewModel::updateStrokeColor,
        onTemplateToggleSetting = viewModel::updateTemplateVisibility,
        onCourseNext = viewModel::goToNextCourseCharacter,
        onCoursePrev = viewModel::goToPreviousCourseCharacter,
        onCourseSkip = viewModel::skipCourseCharacter,
        onCourseRestart = viewModel::restartCourseLevel,
        onCourseExit = viewModel::clearCourseSession,
        onPracticeQueueNext = viewModel::goToNextPracticeQueueCharacter,
        onPracticeQueuePrev = viewModel::goToPreviousPracticeQueueCharacter,
        onPracticeQueueRestart = viewModel::restartPracticeQueue,
        onPracticeQueueExit = viewModel::exitPracticeQueue,
        languageOverride = userPreferences.languageOverride,
        onLanguageChange = viewModel::setLanguageOverride,
        onLoadCharacter = viewModel::jumpToCharacter,
    )
}
@Composable
fun CharacterScreen(
    query: String,
    uiState: CharacterUiState,
    practiceState: PracticeState,
    renderSnapshot: RenderStateSnapshot?,
    demoState: DemoState,
    wordEntry: WordEntry?,
    wordInfoUiState: WordInfoUiState,
    hskEntry: HskEntry?,
    hskProgress: HskProgressSummary,
    courseSession: CourseSession?,
    practiceQueueSession: PracticeQueueSession?,
    boardSettings: BoardSettings,
    dailySymbol: String?,
    dailyEpochDay: Long?,
    dailyPinyin: String?,
    dailyExplanationSummary: String?,
    dailyPracticeCompletedSymbol: String?,
    dailyPracticeCompletedEpochDay: Long?,
    languageOverride: String?,
    onLoadCharacter: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
    onStartPractice: () -> Unit,
    onStartDailyPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onRequestWordInfo: (String) -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggleSetting: (Boolean) -> Unit,
    onCourseNext: () -> Unit,
    onCoursePrev: () -> Unit,
    onCourseSkip: () -> Unit,
    onCourseRestart: () -> Unit,
    onCourseExit: () -> Unit,
    onPracticeQueueNext: () -> Unit,
    onPracticeQueuePrev: () -> Unit,
    onPracticeQueueRestart: () -> Unit,
    onPracticeQueueExit: () -> Unit,
    onLanguageChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showTemplate = boardSettings.showTemplate
    val strings = rememberLocalizedStrings(languageOverride)
    val calligraphyDemoController = if (uiState is CharacterUiState.Success && showTemplate) {
        rememberCalligraphyDemoController(
            strokeCount = uiState.definition.strokeCount,
            definitionKey = uiState.definition.symbol,
        )
    } else {
        null
    }
    val calligraphyDemoState = calligraphyDemoController?.state?.value ?: CalligraphyDemoState()
    val useCalligraphyDemo = showTemplate && calligraphyDemoController != null
    val playCalligraphyDemo: (Boolean) -> Unit = calligraphyDemoController?.play ?: {}
    val stopCalligraphyDemo: () -> Unit = calligraphyDemoController?.stop ?: {}
    var selectedHomeTabKey by rememberSaveable { mutableStateOf(HomePracticeTab.Practice.name) }
    val selectedHomeTab = remember(selectedHomeTabKey) { HomePracticeTab.valueOf(selectedHomeTabKey) }
    val homeTabs = remember(strings) {
        listOf(
            Triple(HomePracticeTab.Practice, strings.progress.dailyPracticeLabel, TestTags.HOME_TAB_PRACTICE),
            Triple(HomePracticeTab.Daily, strings.progress.dailyTitle, TestTags.HOME_TAB_DAILY),
        )
    }
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchBarRow(
                query = query,
                uiState = uiState,
                onQueryChange = onQueryChange,
                onSubmit = onSubmit,
                onClearQuery = onClearQuery,
                demoState = demoState,
                isPracticeActive = practiceState.isActive,
                calligraphyDemoState = calligraphyDemoState,
                useCalligraphyDemo = useCalligraphyDemo,
                onPlayDemoOnce = onPlayDemoOnce,
                onPlayDemoLoop = onPlayDemoLoop,
                onStopDemo = onStopDemo,
                onPlayCalligraphyDemoOnce = { playCalligraphyDemo(false) },
                onPlayCalligraphyDemoLoop = { playCalligraphyDemo(true) },
                onStopCalligraphyDemo = stopCalligraphyDemo,
                strings = strings,
                languageOverride = languageOverride,
                onLanguageChange = onLanguageChange,
            )
            TabRow(
                selectedTabIndex = selectedHomeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                homeTabs.forEach { (tab, label, tag) ->
                    Tab(
                        modifier = Modifier.testTag(tag),
                        selected = selectedHomeTab == tab,
                        onClick = { selectedHomeTabKey = tab.name },
                        text = { Text(label) },
                    )
                }
            }
            val zone = remember { ZoneId.systemDefault() }
            val todayEpochDay = remember(zone) { LocalDate.now(zone).toEpochDay() }
            val suggestedDailySymbol = remember(hskProgress) { pickDailySymbol(hskProgress) }
            val resolvedDailySymbol = remember(dailySymbol, dailyEpochDay, suggestedDailySymbol, todayEpochDay) {
                val storedSymbol = dailySymbol?.trim()?.takeIf { it.isNotBlank() }
                if (dailyEpochDay == todayEpochDay && storedSymbol != null) {
                    storedSymbol
                } else {
                    suggestedDailySymbol
                }
            }
            val resolvedDailyPinyin = remember(
                resolvedDailySymbol,
                dailySymbol,
                dailyEpochDay,
                todayEpochDay,
                dailyPinyin,
            ) {
                val storedSymbol = dailySymbol?.trim()?.takeIf { it.isNotBlank() }
                val resolvedSymbol = resolvedDailySymbol?.trim()?.takeIf { it.isNotBlank() }
                val pinyin = dailyPinyin?.trim()?.takeIf { it.isNotBlank() }
                if (dailyEpochDay == todayEpochDay && storedSymbol != null && storedSymbol == resolvedSymbol) {
                    pinyin
                } else {
                    null
                }
            }
            val resolvedDailyExplanationSummary = remember(
                resolvedDailySymbol,
                dailySymbol,
                dailyEpochDay,
                todayEpochDay,
                dailyExplanationSummary,
            ) {
                val storedSymbol = dailySymbol?.trim()?.takeIf { it.isNotBlank() }
                val resolvedSymbol = resolvedDailySymbol?.trim()?.takeIf { it.isNotBlank() }
                val summary = dailyExplanationSummary?.trim()?.takeIf { it.isNotBlank() }
                if (dailyEpochDay == todayEpochDay && storedSymbol != null && storedSymbol == resolvedSymbol) {
                    summary
                } else {
                    null
                }
            }
            val dailyCompletedToday = remember(
                resolvedDailySymbol,
                dailyPracticeCompletedSymbol,
                dailyPracticeCompletedEpochDay,
                todayEpochDay,
            ) {
                val completedSymbol = dailyPracticeCompletedSymbol?.trim().takeIf { !it.isNullOrBlank() }
                val resolvedSymbol = resolvedDailySymbol?.trim().takeIf { !it.isNullOrBlank() }
                completedSymbol != null &&
                    resolvedSymbol != null &&
                    dailyPracticeCompletedEpochDay == todayEpochDay &&
                    completedSymbol == resolvedSymbol
            }
            when (selectedHomeTab) {
                HomePracticeTab.Daily -> {
                    DailyPracticeBadge(
                        title = strings.progress.dailyTitle,
                        practiceLabel = strings.progress.dailyPracticeLabel,
                        completedLabel = strings.progress.dailyCompletedLabel,
                        symbol = resolvedDailySymbol,
                        pinyin = resolvedDailyPinyin,
                        explanationSummary = resolvedDailyExplanationSummary,
                        completedToday = dailyCompletedToday,
                        onPractice = {
                            selectedHomeTabKey = HomePracticeTab.Practice.name
                            onStartDailyPractice()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.HOME_DAILY_BADGE),
                    )
                }

                HomePracticeTab.Practice -> {
                    when (uiState) {
                        CharacterUiState.Loading -> Text(strings.loadingLabel)
                        is CharacterUiState.Error -> PracticeErrorBanner(
                            message = uiState.message,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        is CharacterUiState.Success -> PracticeContent(
                            definition = uiState.definition,
                            renderSnapshot = renderSnapshot,
                            practiceState = practiceState,
                            courseSession = courseSession,
                            practiceQueueSession = practiceQueueSession,
                            boardSettings = boardSettings,
                            isDemoPlaying = demoState.isPlaying || calligraphyDemoState.isPlaying,
                            wordEntry = wordEntry,
                            wordInfoUiState = wordInfoUiState,
                            onRequestWordInfo = onRequestWordInfo,
                            hskEntry = hskEntry,
                            showTemplate = showTemplate,
                            boardStrings = strings.boardControls,
                            onTemplateToggle = { enabled ->
                                onTemplateToggleSetting(enabled)
                                if (!enabled) {
                                    stopCalligraphyDemo()
                                }
                            },
                            calligraphyDemoState = calligraphyDemoState,
                            onStopCalligraphyDemo = stopCalligraphyDemo,
                            onStartPractice = {
                                stopCalligraphyDemo()
                                onStartPractice()
                            },
                            onRequestHint = onRequestHint,
                            onStrokeStart = onStrokeStart,
                            onStrokeMove = onStrokeMove,
                            onStrokeEnd = onStrokeEnd,
                            onGridModeChange = onGridModeChange,
                            onStrokeColorChange = onStrokeColorChange,
                            onCourseNext = onCourseNext,
                            onCoursePrev = onCoursePrev,
                            onCourseSkip = onCourseSkip,
                            onCourseRestart = onCourseRestart,
                            onResumeCourse = onLoadCharacter,
                            onCourseExit = onCourseExit,
                            onPracticeQueueNext = onPracticeQueueNext,
                            onPracticeQueuePrev = onPracticeQueuePrev,
                            onPracticeQueueRestart = onPracticeQueueRestart,
                            onPracticeQueueExit = onPracticeQueueExit,
                            courseStrings = strings.courses,
                            locale = strings.locale,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(TestTags.HOME_PRACTICE_CONTENT),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPracticeBadge(
    title: String,
    practiceLabel: String,
    completedLabel: String,
    symbol: String?,
    pinyin: String?,
    explanationSummary: String?,
    completedToday: Boolean,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !symbol.isNullOrBlank()
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = symbol ?: "—",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(
                    onClick = onPractice,
                    enabled = enabled,
                    modifier = Modifier.testTag(TestTags.HOME_DAILY_PRACTICE_BUTTON),
                ) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(practiceLabel)
                }
            }
            if (completedToday) {
                Text(
                    text = completedLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (!pinyin.isNullOrBlank() || !explanationSummary.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (!pinyin.isNullOrBlank()) {
                        Text(
                            text = pinyin,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!explanationSummary.isNullOrBlank()) {
                        Text(
                            text = explanationSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
