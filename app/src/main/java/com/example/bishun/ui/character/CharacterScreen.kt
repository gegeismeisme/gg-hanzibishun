package com.example.bishun.ui.character
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bishun.data.word.WordEntry
import com.example.bishun.data.hsk.HskEntry
import android.widget.Toast
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.ui.character.components.IconActionButton
import com.example.bishun.ui.practice.BoardSettings
import com.example.bishun.ui.practice.CalligraphyDemoState
import com.example.bishun.ui.practice.PracticeContent
import com.example.bishun.ui.practice.PracticeErrorBanner
import com.example.bishun.ui.practice.PracticeGrid
import com.example.bishun.ui.practice.StrokeColorOption
import com.example.bishun.ui.practice.SearchBarRow
import com.example.bishun.ui.practice.rememberCalligraphyDemoController
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
    val hskEntry by viewModel.hskEntry.collectAsState()
    val courseSession by viewModel.courseSession.collectAsState()
    val boardSettings by viewModel.boardSettings.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.courseEvents.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }
    CharacterScreen(
        modifier = modifier,
        query = query,
        uiState = uiState,
        practiceState = practiceState,
        renderSnapshot = renderSnapshot,
        demoState = demoState,
        wordEntry = wordEntry,
        hskEntry = hskEntry,
        courseSession = courseSession,
        boardSettings = boardSettings,
        onQueryChange = viewModel::updateQuery,
        onSubmit = viewModel::submitQuery,
        onClearQuery = viewModel::clearQuery,
        onPlayDemoOnce = { viewModel.playDemo(loop = false) },
        onPlayDemoLoop = { viewModel.playDemo(loop = true) },
        onStopDemo = viewModel::stopDemo,
        onStartPractice = viewModel::startPractice,
        onRequestHint = viewModel::requestHint,
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
    hskEntry: HskEntry?,
    courseSession: CourseSession?,
    boardSettings: BoardSettings,
    languageOverride: String?,
    onLoadCharacter: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
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
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    boardSettings = boardSettings,
                    isDemoPlaying = demoState.isPlaying || calligraphyDemoState.isPlaying,
                    wordEntry = wordEntry,
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
                    courseStrings = strings.courses,
                    locale = strings.locale,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
