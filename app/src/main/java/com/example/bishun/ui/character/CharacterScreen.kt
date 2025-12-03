package com.example.bishun.ui.character
import android.graphics.Matrix as AndroidMatrix
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.graphics.PathParser as AndroidPathParser
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bishun.R
import com.example.bishun.data.word.WordEntry
import com.example.bishun.data.hsk.HskEntry
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.bishun.data.settings.UserPreferences
import com.example.bishun.hanzi.core.Positioner
import com.example.bishun.hanzi.geometry.Geometry
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.model.Stroke as ModelStroke
import com.example.bishun.hanzi.render.CharacterRenderState
import com.example.bishun.hanzi.render.ColorRgba
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.UserStrokeRenderState
import com.example.bishun.ui.character.components.IconActionButton
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
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
    val hskProgress by viewModel.hskProgress.collectAsState()
    val courseSession by viewModel.courseSession.collectAsState()
    val boardSettings by viewModel.boardSettings.collectAsState()
    val courseCatalog by viewModel.courseCatalog.collectAsState()
    val completedSymbols by viewModel.completedSymbols.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val feedbackSubmission by viewModel.feedbackSubmission.collectAsState()
    val lastFeedbackTimestamp by viewModel.lastFeedbackSubmission.collectAsState()
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
        hskProgress = hskProgress,
        courseCatalog = courseCatalog,
        courseSession = courseSession,
        completedSymbols = completedSymbols,
        boardSettings = boardSettings,
        userPreferences = userPreferences,
        lastFeedbackTimestamp = lastFeedbackTimestamp,
        feedbackSubmission = feedbackSubmission,
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
        onAnalyticsOptInChange = viewModel::setAnalyticsOptIn,
        onCrashOptInChange = viewModel::setCrashReportsOptIn,
        onPrefetchChange = viewModel::setNetworkPrefetch,
        onFeedbackDraftChange = viewModel::saveFeedbackDraft,
        onFeedbackSubmit = viewModel::submitFeedback,
        onFeedbackHandled = viewModel::consumeFeedbackSubmission,
        onLoadFeedbackLog = { viewModel.readFeedbackLog() },
        onGridModeChange = viewModel::updateGridMode,
        onStrokeColorChange = viewModel::updateStrokeColor,
        onTemplateToggleSetting = viewModel::updateTemplateVisibility,
        onCourseSelect = viewModel::startCourse,
        onCourseNext = viewModel::goToNextCourseCharacter,
        onCoursePrev = viewModel::goToPreviousCourseCharacter,
        onCourseSkip = viewModel::skipCourseCharacter,
        onCourseRestart = viewModel::restartCourseLevel,
        onCourseExit = viewModel::clearCourseSession,
        onMarkCourseLearned = viewModel::markCourseCharacterLearned,
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
    hskProgress: HskProgressSummary,
    courseCatalog: Map<Int, List<String>>,
    courseSession: CourseSession?,
    completedSymbols: Set<String>,
    boardSettings: BoardSettings,
    userPreferences: UserPreferences,
    lastFeedbackTimestamp: Long?,
    feedbackSubmission: FeedbackSubmission?,
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
    onAnalyticsOptInChange: (Boolean) -> Unit,
    onCrashOptInChange: (Boolean) -> Unit,
    onPrefetchChange: (Boolean) -> Unit,
    onFeedbackDraftChange: (String, String, String) -> Unit,
    onFeedbackSubmit: (String, String, String) -> Unit,
    onFeedbackHandled: () -> Unit,
    onLoadFeedbackLog: suspend () -> String,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggleSetting: (Boolean) -> Unit,
    onCourseSelect: (Int, String) -> Unit,
    onCourseNext: () -> Unit,
    onCoursePrev: () -> Unit,
    onCourseSkip: () -> Unit,
    onCourseRestart: () -> Unit,
    onCourseExit: () -> Unit,
    onMarkCourseLearned: (String) -> Unit,
    onLanguageChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeProfileAction by rememberSaveable { mutableStateOf<ProfileMenuAction?>(null) }
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shareFeedbackLog: () -> Unit = {
        coroutineScope.launch {
            val logText = onLoadFeedbackLog().takeIf { it.isNotBlank() }
            if (logText.isNullOrBlank()) {
                Toast.makeText(context, "Feedback log is empty.", Toast.LENGTH_LONG).show()
                return@launch
            }
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Hanzi feedback log")
                putExtra(Intent.EXTRA_TEXT, logText)
            }
            val chooser = Intent.createChooser(shareIntent, "Share feedback log")
            val canHandle = shareIntent.resolveActivity(context.packageManager) != null
            if (canHandle) {
                runCatching { context.startActivity(chooser) }
                    .onFailure {
                        Toast.makeText(context, "Unable to share log.", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(context, "No apps available to share log.", Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(feedbackSubmission) {
        val submission = feedbackSubmission ?: return@LaunchedEffect
        val subject = if (submission.topic.isNotBlank()) {
            "Hanzi Stroke Order feedback: ${submission.topic}"
        } else {
            "Hanzi Stroke Order feedback"
        }
        val contactLine = submission.contact.takeIf { it.isNotBlank() }?.let { "\n\nContact: $it" } ?: ""
        val body = submission.message.ifBlank { "(No message)" } + contactLine
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val chooser = Intent.createChooser(intent, "Send feedback")
        val canHandle = intent.resolveActivity(context.packageManager) != null
        if (canHandle) {
            runCatching { context.startActivity(chooser) }
                .onFailure {
                    Toast.makeText(context, "Couldn't open email. Log saved locally—share from Profile later.", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, "No email app installed. Log saved locally—share from Profile later.", Toast.LENGTH_LONG).show()
        }
        onFeedbackHandled()
    }
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
                onProfileAction = { activeProfileAction = it },
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
                is CharacterUiState.Error -> Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                )
                is CharacterUiState.Success -> CharacterContent(
                    definition = uiState.definition,
                    renderSnapshot = renderSnapshot,
                    practiceState = practiceState,
                    courseSession = courseSession,
                    boardSettings = boardSettings,
                    wordEntry = wordEntry,
                    hskEntry = hskEntry,
                    showTemplate = showTemplate,
                    onTemplateToggle = { enabled ->
                        onTemplateToggleSetting(enabled)
                        if (!enabled) {
                            stopCalligraphyDemo()
                        }
                    },
                    calligraphyDemoState = calligraphyDemoState,
                    isDemoPlaying = demoState.isPlaying || calligraphyDemoState.isPlaying,
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
                modifier = Modifier.weight(1f),
            )
            }
        }
        activeProfileAction?.let { action ->
            ProfileActionDialog(
                action = action,
                hskProgress = hskProgress,
                courseCatalog = courseCatalog,
                completedSymbols = completedSymbols,
                activeSession = courseSession,
                onMarkCourseLearned = onMarkCourseLearned,
                userPreferences = userPreferences,
                wordEntry = wordEntry,
                lastFeedbackTimestamp = lastFeedbackTimestamp,
                onShareLog = shareFeedbackLog,
                onResumeCourse = {
                    val symbol = courseSession?.currentSymbol
                    if (symbol != null) {
                        onLoadCharacter(symbol)
                    }
                },
                onExitCourse = onCourseExit,
                onSkipCourse = onCourseSkip,
                onRestartCourse = onCourseRestart,
                onCourseSelect = onCourseSelect,
                onJumpToChar = onLoadCharacter,
                onAnalyticsChange = onAnalyticsOptInChange,
                onCrashReportsChange = onCrashOptInChange,
                onPrefetchChange = onPrefetchChange,
                onFeedbackDraftChange = onFeedbackDraftChange,
                onFeedbackSubmit = onFeedbackSubmit,
                onDismiss = { activeProfileAction = null },
                strings = strings,
            )
        }
    }
}
@Composable
private fun SearchBarRow(
    query: String,
    uiState: CharacterUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearQuery: () -> Unit,
    demoState: DemoState,
    isPracticeActive: Boolean,
    onProfileAction: (ProfileMenuAction) -> Unit,
    calligraphyDemoState: CalligraphyDemoState,
    useCalligraphyDemo: Boolean,
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
    onPlayCalligraphyDemoOnce: () -> Unit,
    onPlayCalligraphyDemoLoop: () -> Unit,
    onStopCalligraphyDemo: () -> Unit,
    strings: LocalizedStrings,
    languageOverride: String?,
    onLanguageChange: (String?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = strings.appTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            LanguageMenu(
                currentTag = languageOverride,
                onLanguageChange = onLanguageChange,
            )
            ProfileAvatarMenu(onProfileAction = onProfileAction)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text(strings.searchLabel) },
                placeholder = { Text("\u6c38") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier
                    .width(96.dp)
                    .heightIn(max = 38.dp),
            )
            IconActionButton(
                icon = Icons.Filled.CloudDownload,
                description = strings.loadButton,
                onClick = onSubmit,
                enabled = query.isNotBlank(),
                buttonSize = 32.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Clear,
                description = strings.clearButton,
                onClick = onClearQuery,
                enabled = query.isNotEmpty(),
                buttonSize = 32.dp,
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            DemoControlRow(
                uiState = uiState,
                demoState = demoState,
                calligraphyDemoState = calligraphyDemoState,
                useCalligraphyDemo = useCalligraphyDemo,
                practiceActive = isPracticeActive,
                onSubmit = onSubmit,
                onPlayOnce = onPlayDemoOnce,
                onPlayLoop = onPlayDemoLoop,
                onStop = onStopDemo,
                onPlayCalligraphyOnce = onPlayCalligraphyDemoOnce,
                onPlayCalligraphyLoop = onPlayCalligraphyDemoLoop,
                onStopCalligraphy = onStopCalligraphyDemo,
            )
        }
    }
}
@Composable
private fun ProfileAvatarMenu(onProfileAction: (ProfileMenuAction) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val menuItems = ProfileMenuAction.values()
    Box {
        IconActionButton(
            icon = Icons.Filled.Person,
            description = "Profile",
            onClick = { expanded = true },
            buttonSize = 32.dp,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            menuItems.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label) },
                    onClick = {
                        expanded = false
                        onProfileAction(action)
                    },
                )
            }
        }
    }
}
@Composable
private fun CharacterContent(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    courseSession: CourseSession?,
    boardSettings: BoardSettings,
    isDemoPlaying: Boolean,
    wordEntry: WordEntry?,
    hskEntry: HskEntry?,
    showTemplate: Boolean,
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
        CharacterInfoPanel(
            definition = definition,
            wordEntry = wordEntry,
            onWordInfoClick = { if (wordEntry != null) showWordInfo = true },
            modifier = Modifier.fillMaxWidth(),
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
        )
    }
    if (showWordInfo && wordEntry != null) {
        WordInfoDialog(
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
@Composable
private fun DemoControlRow(
    uiState: CharacterUiState,
    demoState: DemoState,
    calligraphyDemoState: CalligraphyDemoState,
    useCalligraphyDemo: Boolean,
    practiceActive: Boolean,
    onSubmit: () -> Unit,
    onPlayOnce: () -> Unit,
    onPlayLoop: () -> Unit,
    onStop: () -> Unit,
    onPlayCalligraphyOnce: () -> Unit,
    onPlayCalligraphyLoop: () -> Unit,
    onStopCalligraphy: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isPlaying = if (useCalligraphyDemo) calligraphyDemoState.isPlaying else demoState.isPlaying
        if (isPlaying) {
            val stopHandler = if (useCalligraphyDemo) onStopCalligraphy else onStop
            IconActionButton(
                icon = Icons.Filled.Stop,
                description = "Stop demo",
                onClick = stopHandler,
                buttonSize = 36.dp,
            )
        } else {
            val playOnce = if (useCalligraphyDemo) {
                onPlayCalligraphyOnce
            } else {
                {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayOnce()
                    }
                }
            }
            val playLoop = if (useCalligraphyDemo) {
                onPlayCalligraphyLoop
            } else {
                {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayLoop()
                    }
                }
            }
            IconActionButton(
                icon = Icons.Filled.PlayArrow,
                description = "Play demo",
                onClick = playOnce,
                enabled = !practiceActive,
                buttonSize = 36.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Refresh,
                description = "Loop demo",
                onClick = playLoop,
                enabled = !practiceActive,
                buttonSize = 36.dp,
            )
        }
    }
}
@Composable
private fun CharacterCanvas(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    courseSession: CourseSession?,
    isDemoPlaying: Boolean,
    gridMode: PracticeGrid,
    userStrokeColor: Color,
    showTemplate: Boolean,
    calligraphyDemoProgress: List<Float>,
    hskEntry: HskEntry?,
    showHskHint: Boolean,
    showHskIcon: Boolean,
    onHskInfoClick: () -> Unit,
    currentColorOption: StrokeColorOption,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggle: (Boolean) -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onCourseNext: () -> Unit,
    onCoursePrev: () -> Unit,
    onCourseSkip: () -> Unit,
    onCourseRestart: () -> Unit,
    onCourseExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineColor = Color(0xFFD6D6D6)
    val teachingStrokeColor = Color(0xFF0F0F0F)
    val canvasBackground = Color.White
    val radicalStrokeColor = MaterialTheme.colorScheme.primary
    val canvasSizeState = remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(canvasBackground)
            .onSizeChanged { canvasSizeState.value = it },
        contentAlignment = Alignment.Center,
    ) {
        val positioner = remember(canvasSizeState.value) {
            val size = canvasSizeState.value
            if (size.width == 0 || size.height == 0) {
                null
            } else {
                Positioner(size.width.toFloat(), size.height.toFloat(), padding = 32f)
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBackground)
                .practicePointerInput(
                    practiceState = practiceState,
                    positioner = positioner,
                    onStrokeStart = onStrokeStart,
                    onStrokeMove = onStrokeMove,
                    onStrokeEnd = onStrokeEnd,
                ),
        ) {
            val strokeWidth = 8.dp.toPx()
            val drawPositioner = Positioner(size.width, size.height, padding = 32f)
            drawRect(
                color = outlineColor,
                style = Stroke(width = 1.dp.toPx()),
            )
            drawPracticeGrid(gridMode)
            if (showTemplate) {
                drawTemplateStrokes(
                    definition = definition,
                    positioner = drawPositioner,
                    completedStrokes = practiceState.completedStrokes,
                    completedFillColor = userStrokeColor,
                    demoProgress = calligraphyDemoProgress,
                )
            }
            val snapshot = renderSnapshot
            if (snapshot == null) {
                if (!showTemplate) {
                    definition.strokes.forEach { stroke ->
                        val path = stroke.toFullPath(drawPositioner)
                        val color = if (stroke.isInRadical) radicalStrokeColor else teachingStrokeColor
                        drawStrokePath(path, color, strokeWidth)
                    }
                }
            } else {
                if (!showTemplate) {
                    drawLayer(
                        definition = definition,
                        layerState = snapshot.character.outline,
                        baseColor = outlineColor,
                        positioner = drawPositioner,
                        strokeWidth = strokeWidth,
                    )
                    drawLayer(
                        definition = definition,
                        layerState = snapshot.character.main,
                        baseColor = teachingStrokeColor,
                        positioner = drawPositioner,
                        strokeWidth = strokeWidth,
                    )
                }
                drawLayer(
                    definition = definition,
                    layerState = snapshot.character.highlight,
                    baseColor = snapshot.options.highlightColor.asComposeColor(),
                    positioner = drawPositioner,
                    strokeWidth = strokeWidth,
                )
                snapshot.userStrokes.values.forEach { userStroke ->
                    drawUserStroke(
                        userStroke = userStroke,
                        positioner = drawPositioner,
                        color = userStrokeColor,
                        drawingWidth = snapshot.options.drawingWidth,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val boardButtonSize = 32.dp
            IconActionButton(
                icon = Icons.Filled.Create,
                description = "Start practice",
                onClick = onStartPractice,
                enabled = !practiceState.isActive && !isDemoPlaying,
                buttonSize = boardButtonSize,
            )
            IconActionButton(
                icon = Icons.Filled.Info,
                description = "Hint",
                onClick = onRequestHint,
                enabled = practiceState.isActive && !isDemoPlaying,
                buttonSize = boardButtonSize,
            )
            courseSession?.let { session ->
                IconActionButton(
                    icon = Icons.Filled.ChevronLeft,
                    description = "Previous word",
                    onClick = onCoursePrev,
                    enabled = session.hasPrevious,
                    buttonSize = boardButtonSize,
                )
                IconActionButton(
                    icon = Icons.Filled.ChevronRight,
                    description = "Next word",
                    onClick = onCourseNext,
                    enabled = session.hasNext,
                    buttonSize = boardButtonSize,
                )
            }
            CanvasSettingsMenu(
                currentGrid = gridMode,
                currentColor = currentColorOption,
                showTemplate = showTemplate,
                onGridChange = onGridModeChange,
                onColorChange = onStrokeColorChange,
                onTemplateToggle = onTemplateToggle,
                buttonSize = boardButtonSize,
            )
            if (showHskIcon && hskEntry != null) {
                IconActionButton(
                    icon = Icons.Filled.School,
                    description = "HSK info",
                    onClick = onHskInfoClick,
                    buttonSize = boardButtonSize,
                )
            }
        }
        if (showHskHint) {
            HskBadge(
                entry = hskEntry,
                fallbackSymbol = definition.symbol,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
            )
        }
    }
}
@Composable
private fun CharacterInfoPanel(
    definition: CharacterDefinition,
    wordEntry: WordEntry?,
    onWordInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterGlyphWithGrid(
                symbol = definition.symbol,
                modifier = Modifier.size(120.dp),
            )
            if (wordEntry != null) {
                WordInfoPreview(
                    entry = wordEntry,
                    onClick = onWordInfoClick,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Text(
                    text = "Info...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}
@Composable
private fun WordInfoPreview(
    entry: WordEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFEDE6F5),
        modifier = modifier
            .height(96.dp)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = entry.pinyin.ifBlank { "Pinyin..." },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${entry.radicals.ifBlank { "Rad." }} / ${entry.strokes.ifBlank { "?" }} strokes...",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.explanation.condense(40),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HskBadge(
    entry: HskEntry?,
    fallbackSymbol: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color(0xFF1E1E1E).copy(alpha = 0.8f),
        contentColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val title = entry?.let { "HSK ${it.level}" } ?: "HSK"
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = entry?.examples
                    ?.split(' ', '，', '、', '；')
                    ?.firstOrNull { it.isNotBlank() }
                    ?: entry?.symbol
                    ?: "$fallbackSymbol · reference coming soon",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
@Composable
private fun WordInfoDialog(
    entry: WordEntry,
    onDismiss: () -> Unit,
    ttsController: TextToSpeechController,
) {
    val scrollState = rememberScrollState()
    val speakingAlpha by animateFloatAsState(
        targetValue = if (ttsController.isSpeaking.value) 1f else 0.5f,
        label = "speakingAlpha",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.word,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = entry.pinyin.ifBlank { "Pinyin..." },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                IconButton(
                    onClick = { ttsController.speak(entry.word) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaker",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = speakingAlpha),
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState),
            ) {
                WordInfoStat("Radical", entry.radicals)
                WordInfoStat("Strokes", entry.strokes)
                WordInfoStat("Variant", entry.oldword)
                Text(
                    text = "Meaning",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = entry.explanation.normalizeWhitespace().ifBlank { "Meaning unavailable." },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
    )
}

@Composable
private fun HskInfoDialog(
    entry: HskEntry,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("HSK ${entry.level}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                HskInfoRow("Writing level", entry.writingLevel?.toString() ?: "N/A")
                HskInfoRow("Traditional", entry.traditional.ifBlank { entry.symbol })
                HskInfoRow("Frequency", entry.frequency?.toString() ?: "N/A")
                Text(
                    text = "Examples",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = entry.examples.normalizeWhitespace().ifBlank { "No examples available." },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun HskInfoRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun WordInfoStat(label: String, value: String) {
    if (value.isBlank()) return
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileActionDialog(
    action: ProfileMenuAction,
    hskProgress: HskProgressSummary,
    courseCatalog: Map<Int, List<String>>,
    completedSymbols: Set<String>,
    activeSession: CourseSession?,
    onResumeCourse: () -> Unit,
    onExitCourse: () -> Unit,
    onSkipCourse: () -> Unit,
    onRestartCourse: () -> Unit,
    onMarkCourseLearned: (String) -> Unit,
    userPreferences: UserPreferences,
    wordEntry: WordEntry?,
    lastFeedbackTimestamp: Long?,
    onShareLog: () -> Unit,
    onCourseSelect: (Int, String) -> Unit,
    onJumpToChar: (String) -> Unit,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashReportsChange: (Boolean) -> Unit,
    onPrefetchChange: (Boolean) -> Unit,
    onFeedbackDraftChange: (String, String, String) -> Unit,
    onFeedbackSubmit: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
    strings: LocalizedStrings,
) {
    when (action) {
        ProfileMenuAction.COURSES -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(strings.coursesDialogTitle) },
                text = {
                    Text(
                        text = "课程功能已移动到底部导航栏的“课程”标签。请通过底部 Tab 浏览课程、解锁与继续练习。",
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("好的") }
                },
            )
        }
        ProfileMenuAction.PROGRESS -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(action.label) },
                text = { Text("进度统计已移动到底部导航栏的“进度”标签。请通过底部 Tab 浏览练习历史与 HSK 完成度。") },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("好的") }
                },
            )
        }
        ProfileMenuAction.HELP -> {
            HelpDialog(strings = strings, onDismiss = onDismiss)
        }
        ProfileMenuAction.DICT -> {
            val entry = wordEntry
            if (entry != null) {
                val ttsController = rememberTextToSpeechController()
                WordInfoDialog(entry = entry, onDismiss = onDismiss, ttsController = ttsController)
            } else {
                AlertDialog(
                    onDismissRequest = onDismiss,
                    title = { Text("Dictionary") },
                    text = { Text("No dictionary data available for this character yet.") },
                    confirmButton = {
                        TextButton(onClick = onDismiss) { Text("Close") }
                    },
                )
            }
        }
        ProfileMenuAction.PRIVACY -> {
            PrivacyDialog(
                prefs = userPreferences,
                onAnalyticsChange = onAnalyticsChange,
                onCrashChange = onCrashReportsChange,
                onPrefetchChange = onPrefetchChange,
                onDismiss = onDismiss,
                strings = strings,
            )
        }
        ProfileMenuAction.FEEDBACK -> {
            FeedbackDialog(
                prefs = userPreferences,
                lastSubmittedAt = lastFeedbackTimestamp,
                onShareLog = onShareLog,
                onDraftChange = onFeedbackDraftChange,
                onSubmit = onFeedbackSubmit,
                onDismiss = onDismiss,
            )
        }
        ProfileMenuAction.SHARE_LOG -> {
            onShareLog()
            onDismiss()
        }
        else -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(action.label) },
                text = { Text(action.description) },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("Close") }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
private fun CoursePlannerView(
    summary: HskProgressSummary,
    catalog: Map<Int, List<String>>,
    completedSymbols: Set<String>,
    activeSession: CourseSession?,
    onSelect: (Int, String) -> Unit,
    onMarkLearned: (String) -> Unit,
    strings: LocalizedStrings,
) {
    val levelKeys = (summary.perLevel.keys + catalog.keys).toSortedSet()
    if (levelKeys.isEmpty()) {
        Text(
            text = strings.courseNoDataMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    var selectedFilterKey by rememberSaveable { mutableStateOf(CourseFilter.REMAINING.name) }
    val selectedFilter = CourseFilter.valueOf(selectedFilterKey)
    val expandedLevels = remember { mutableStateMapOf<Int, Boolean>() }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = strings.coursePlanHeading,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CourseFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilterKey = filter.name },
                        label = { Text(filter.label(strings)) },
                    )
                }
            }
        }
        item {
            CourseLegendRow(strings = strings)
        }
        items(levelKeys.toList()) { level ->
            val symbols = catalog[level].orEmpty()
            val completedCount = symbols.count { completedSymbols.contains(it) }
            val stats = summary.perLevel[level]
                ?: HskLevelSummary(
                    completed = completedCount,
                    total = symbols.size,
                )
            val fallbackNext = symbols.firstOrNull { !completedSymbols.contains(it) }
            val nextSymbol = summary.nextTargets[level]
                ?: fallbackNext
            CourseLevelCard(
                level = level,
                stats = stats,
                nextSymbol = nextSymbol,
                onSelect = { symbol -> onSelect(level, symbol) },
                strings = strings,
            )
            if (symbols.isNotEmpty()) {
                val accentColor = levelColor(level)
                val symbolStates = symbols.mapNotNull { symbol ->
                    val isActive = activeSession?.level == level && activeSession.currentSymbol == symbol
                    val isCompleted = completedSymbols.contains(symbol)
                    if (!selectedFilter.include(isCompleted) && !isActive) {
                        null
                    } else {
                        CourseSymbolVisual(symbol = symbol, isActive = isActive, isCompleted = isCompleted)
                    }
                }
                if (symbolStates.isNotEmpty()) {
                    val previewCount = 10
                    val isExpanded = expandedLevels[level] ?: false
                    val canToggle = symbolStates.size > previewCount
                    if (!isExpanded && canToggle) {
                        val previewSymbols = symbolStates.take(previewCount).joinToString(" ") { it.symbol }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            tonalElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = if (symbolStates.size > previewCount) "$previewSymbols ..." else previewSymbols,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (isExpanded || !canToggle) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            symbolStates.forEach { symbolState ->
                                val background = when {
                                    symbolState.isActive -> accentColor.copy(alpha = 0.25f)
                                    symbolState.isCompleted -> accentColor.copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    tonalElevation = if (symbolState.isActive) 4.dp else 0.dp,
                                    color = background,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .combinedClickable(
                                            onClick = { onSelect(level, symbolState.symbol) },
                                            onLongClick = { onMarkLearned(symbolState.symbol) },
                                        ),
                                ) {
                                    Text(
                                        text = symbolState.symbol,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (symbolState.isActive) accentColor else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }
                    if (canToggle) {
                        TextButton(onClick = { expandedLevels[level] = !isExpanded }) {
                            Text(
                                text = if (isExpanded) strings.collapseCharactersLabel else strings.expandCharactersLabel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseLevelCard(
    level: Int,
    stats: HskLevelSummary,
    nextSymbol: String?,
    onSelect: (String) -> Unit,
    strings: LocalizedStrings,
) {
    val total = stats.total.coerceAtLeast(1)
    val progress = (stats.completed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val accentColor = levelColor(level)
    val statusText = when {
        nextSymbol == null -> strings.courseLevelCompleteLabel
        stats.completed == 0 -> String.format(strings.locale, strings.courseLevelStartFormat, nextSymbol)
        else -> String.format(strings.locale, strings.courseLevelNextFormat, nextSymbol)
    }
    val levelLabel = String.format(strings.locale, strings.levelLabelFormat, level)
    val progressText = String.format(strings.locale, strings.courseLevelProgressFormat, stats.completed, stats.total, stats.remaining)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "HSK $level",
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                    )
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = levelLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = accentColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconActionButton(
                    icon = Icons.Filled.PlayArrow,
                    description = String.format(strings.locale, strings.loadLevelFormat, level),
                    onClick = { nextSymbol?.let(onSelect) },
                    enabled = nextSymbol != null,
                    buttonSize = 36.dp,
                )
            }
        }
    }
}

@Composable
private fun HelpDialog(strings: LocalizedStrings, onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    val sections = strings.helpSections
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.helpTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sections.forEach { section ->
                    HelpSectionCard(section = section)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.helpConfirm) }
        },
    )
}

@Composable
private fun HelpSectionCard(section: HelpSectionText) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(section.title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            section.bullets.forEach { bullet ->
                Text(
                    text = "- $bullet",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun PrivacyDialog(
    prefs: UserPreferences,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashChange: (Boolean) -> Unit,
    onPrefetchChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    strings: LocalizedStrings,
) {
    val context = LocalContext.current
    val contactEmail = SUPPORT_EMAIL
    val summaryPoints = strings.privacySummaryRows
    var showFullPolicy by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.privacyTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = strings.privacyIntro,
                    style = MaterialTheme.typography.bodySmall,
                )
                PrivacyToggleRow(
                    title = "Usage analytics",
                    description = "Anonymous counters for upcoming features.",
                    checked = prefs.analyticsOptIn,
                    onCheckedChange = onAnalyticsChange,
                )
                PrivacyToggleRow(
                    title = "Crash reports",
                    description = "Share lightweight logs if rendering fails.",
                    checked = prefs.crashReportsOptIn,
                    onCheckedChange = onCrashChange,
                )
                PrivacyToggleRow(
                    title = "Network prefetch",
                    description = "Allow Wi-Fi downloads of future packs.",
                    checked = prefs.networkPrefetchEnabled,
                    onCheckedChange = onPrefetchChange,
                )
                HorizontalDivider()
                Text(
                    text = strings.dataSafetyHeading,
                    style = MaterialTheme.typography.labelLarge,
                )
                summaryPoints.forEach { point ->
                    PrivacySummaryCard(PrivacySummaryRow(point.title, point.detail))
                }
                Text(
                    text = String.format(strings.locale, strings.contactSupportLabel, contactEmail),
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$contactEmail")
                            putExtra(Intent.EXTRA_SUBJECT, "Hanzi Stroke Order - Privacy question")
                        }
                        val chooser = Intent.createChooser(intent, strings.emailSupportButton)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            runCatching { context.startActivity(chooser) }
                        }
                    },
                ) {
                    Text(strings.emailSupportButton)
                }
                TextButton(onClick = { showFullPolicy = true }) {
                    Text(strings.viewPolicyButton)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
    if (showFullPolicy) {
        FullPrivacyPolicyDialog(strings = strings, onDismiss = { showFullPolicy = false })
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun FeedbackDialog(
    prefs: UserPreferences,
    lastSubmittedAt: Long?,
    onShareLog: () -> Unit,
    onDraftChange: (String, String, String) -> Unit,
    onSubmit: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var topic by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val canSubmit = message.trim().length >= 6
    val scrollState = rememberScrollState()

    LaunchedEffect(prefs.feedbackTopic, prefs.feedbackMessage, prefs.feedbackContact) {
        if (!submitted) {
            topic = prefs.feedbackTopic
            message = prefs.feedbackMessage
            contact = prefs.feedbackContact
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (submitted) "Thanks!" else "Send feedback") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (lastSubmittedAt != null) {
                    Text(
                        text = "Last sent: ${formatHistoryTimestamp(lastSubmittedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (submitted) {
                    Text(
                        text = "We stored your note locally. The next release will bundle it with diagnostic exports so nothing gets lost.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = {
                            val value = it.take(60)
                            topic = value
                            onDraftChange(value, message, contact)
                        },
                        label = { Text("Topic") },
                        placeholder = { Text("Feature request, bug...") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            val value = it.take(600)
                            message = value
                            onDraftChange(topic, value, contact)
                        },
                        label = { Text("Details") },
                        placeholder = { Text("Describe the idea or issue") },
                        minLines = 4,
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = {
                            val value = it.take(80)
                            contact = value
                            onDraftChange(topic, message, value)
                        },
                        label = { Text("Contact (optional)") },
                        placeholder = { Text("Email or handle") },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            if (submitted) {
                TextButton(onClick = onDismiss) { Text("Close") }
            } else {
                TextButton(
                    onClick = {
                        onSubmit(topic, message, contact)
                        submitted = true
                    },
                    enabled = canSubmit,
                ) {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (lastSubmittedAt != null) {
                    TextButton(onClick = onShareLog) { Text("Share log") }
                }
                if (!submitted) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        },
    )
}

private enum class ProfileMenuAction(val label: String, val description: String) {
    COURSES("Courses...", "Browse curated HSK lessons. We'll package staged practice lists so you can study without typing every character."),
    PROGRESS("Progress...", "See which strokes you've mastered. We'll save local stats per HSK level and streak information."),
    DICT("Dict...", "Open the embedded dictionary for full definitions, examples, and audio. Later, Word info can deep-link here."),
    HELP("Help...", "Tips, gestures, and onboarding videos live here so newcomers understand practice modes quickly."),
    PRIVACY("Privacy...", "Access the privacy policy, data safety notes, and toggles for analytics/log sharing before Play Store submission."),
    FEEDBACK("Feedback...", "Send bugs or feature ideas. We'll attach optional logs so it's easy to iterate together."),
    SHARE_LOG("Share log...", "Quickly share the local feedback log if email wasn't available when submitting.");
}
private data class TextToSpeechController(
    val speak: (String) -> Unit,
    val isSpeaking: State<Boolean>,
)
@Composable
private fun rememberTextToSpeechController(): TextToSpeechController {
    val context = LocalContext.current
    val speakingState = remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val textToSpeech = remember { TextToSpeech(context) { } }
    DisposableEffect(textToSpeech) {
        textToSpeech.language = Locale.CHINA
        val listener = object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                handler.post { speakingState.value = true }
            }
            override fun onDone(utteranceId: String?) {
                handler.post { speakingState.value = false }
            }
            override fun onError(utteranceId: String?) {
                handler.post { speakingState.value = false }
            }
        }
        textToSpeech.setOnUtteranceProgressListener(listener)
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }
    val speak: (String) -> Unit = { word ->
        if (word.isNotBlank()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word-$word")
        }
    }
    return TextToSpeechController(speak = speak, isSpeaking = speakingState)
}
private fun String.condense(maxChars: Int): String {
    val cleaned = replace("\\s+".toRegex(), " ").trim()
    if (cleaned.isEmpty()) return "..."
    if (cleaned.length <= maxChars) return cleaned
    return cleaned.take(maxChars).trimEnd() + "..."
}

private fun String.normalizeWhitespace(): String = replace("\\s+".toRegex(), " ").trim()

private fun formatHistoryTimestamp(timestamp: Long): String {
    return try {
        val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } catch (_: Exception) {
        "-"
    }
}

private const val SUPPORT_EMAIL = "qq260316514@gmail.com"
private data class CalligraphyDemoState(
    val isPlaying: Boolean = false,
    val strokeProgress: List<Float> = emptyList(),
)

private data class CalligraphyDemoCommand(val loop: Boolean, val token: Int)

private class CalligraphyDemoController(
    val state: State<CalligraphyDemoState>,
    val play: (Boolean) -> Unit,
    val stop: () -> Unit,
)

@Composable
private fun rememberCalligraphyDemoController(
    strokeCount: Int,
    definitionKey: Any,
): CalligraphyDemoController {
    val demoState = remember(definitionKey, strokeCount) {
        mutableStateOf(
            CalligraphyDemoState(
                isPlaying = false,
                strokeProgress = List(strokeCount) { 0f },
            ),
        )
    }
    var command by remember(definitionKey) { mutableStateOf<CalligraphyDemoCommand?>(null) }
    var tokenCounter by remember(definitionKey) { mutableStateOf(0) }

    LaunchedEffect(definitionKey, strokeCount) {
        command = null
        demoState.value = CalligraphyDemoState(
            isPlaying = false,
            strokeProgress = List(strokeCount) { 0f },
        )
    }

    LaunchedEffect(command, strokeCount, definitionKey) {
        val cmd = command ?: return@LaunchedEffect
        if (strokeCount <= 0) {
            demoState.value = CalligraphyDemoState(isPlaying = false, strokeProgress = emptyList())
            command = null
            return@LaunchedEffect
        }
        val progress = FloatArray(strokeCount) { 0f }
        demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
        outer@ while (command == cmd) {
            for (index in 0 until strokeCount) {
                val anim = Animatable(progress[index])
                anim.snapTo(0f)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = CALLIGRAPHY_DEMO_STROKE_DURATION, easing = LinearEasing),
                ) {
                    progress[index] = value
                    demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
                }
                if (command != cmd) break@outer
                delay(CALLIGRAPHY_DEMO_STROKE_GAP)
            }
            if (cmd.loop && command == cmd) {
                delay(CALLIGRAPHY_DEMO_LOOP_PAUSE)
                for (i in 0 until strokeCount) {
                    progress[i] = 0f
                }
                demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
            } else {
                break
            }
        }
        if (command == cmd) {
            command = null
            demoState.value = CalligraphyDemoState(
                isPlaying = false,
                strokeProgress = List(strokeCount) { 0f },
            )
        }
    }

    val play: (Boolean) -> Unit = { loop ->
        tokenCounter += 1
        command = CalligraphyDemoCommand(loop = loop, token = tokenCounter)
        demoState.value = demoState.value.copy(isPlaying = true)
    }
    val stop: () -> Unit = {
        command = null
        demoState.value = CalligraphyDemoState(
            isPlaying = false,
            strokeProgress = List(strokeCount) { 0f },
        )
    }

    return CalligraphyDemoController(state = demoState, play = play, stop = stop)
}

private data class PracticeSummaryUi(
    val progressText: String,
    val statusText: String,
)
private fun PracticeState.toSummary(): PracticeSummaryUi {
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
private fun PracticeSummaryBadge(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseLegendRow(strings: LocalizedStrings) {
    val legend = listOf(
        LegendEntry(strings.legendActiveLabel, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        LegendEntry(strings.legendCompletedLabel, MaterialTheme.colorScheme.surfaceVariant),
        LegendEntry(strings.legendRemainingLabel, MaterialTheme.colorScheme.surface),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = strings.legendTitle,
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            legend.forEach { entry ->
                LegendBadge(entry)
            }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (1..6).forEach { level ->
                val color = levelColor(level)
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = String.format(strings.locale, strings.levelLabelFormat, level),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Text(
            text = strings.legendGesture,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = strings.courseLegendHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class LegendEntry(val label: String, val color: Color)

@Composable
private fun LegendBadge(entry: LegendEntry) {
    Surface(
        color = entry.color,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
    ) {
        Text(
            text = entry.label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun LanguageMenu(
    currentTag: String?,
    onLanguageChange: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentChoice = languageChoices.firstOrNull { it.tag == currentTag } ?: languageChoices.first()
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = "Select language",
            )
            Text(
                text = currentChoice.label,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            languageChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(choice.label) },
                    onClick = {
                        expanded = false
                        onLanguageChange(choice.tag)
                    },
                )
            }
        }
    }
}

private data class LanguageChoice(val tag: String?, val label: String)

private val languageChoices = listOf(
    LanguageChoice(null, "System"),
    LanguageChoice("en", "English"),
    LanguageChoice("es", "Español"),
    LanguageChoice("ja", "日本語"),
)

@Composable
private fun levelColor(level: Int): Color = when (level) {
    1 -> Color(0xFF4CAF50)
    2 -> Color(0xFF2196F3)
    3 -> Color(0xFFFFB300)
    4 -> Color(0xFF9C27B0)
    5 -> Color(0xFFFF7043)
    6 -> Color(0xFF607D8B)
    else -> MaterialTheme.colorScheme.primary
}

private data class PrivacySummaryRow(val title: String, val details: String)

@Composable
private fun PrivacySummaryCard(row: PrivacySummaryRow) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(row.title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = row.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FullPrivacyPolicyDialog(strings: LocalizedStrings, onDismiss: () -> Unit) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.fullPolicyTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                strings.fullPolicySections.forEach { section ->
                    PolicySectionCard(section)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun PolicySectionCard(section: PolicySectionText) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(section.title, style = MaterialTheme.typography.titleSmall)
            section.bullets.forEach { bullet ->
                Text(
                    text = "- $bullet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CourseResumeCard(
    session: CourseSession,
    onResume: () -> Unit,
    onExit: () -> Unit,
    onSkip: (() -> Unit)? = null,
    onRestart: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Resume HSK ${session.level}",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "Next: ${session.currentSymbol.orEmpty()} (${session.progressText})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onRestart?.let {
                    TextButton(onClick = it) { Text("Restart") }
                }
                onSkip?.let {
                    TextButton(onClick = it) { Text("Skip") }
                }
                TextButton(onClick = onExit) { Text("Exit") }
                TextButton(onClick = onResume) { Text("Resume") }
            }
        }
    }
}

@Composable
private fun CourseIntroCard(strings: LocalizedStrings) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(strings.courseIntroTitle, style = MaterialTheme.typography.titleSmall)
            strings.courseIntroBullets.forEach { step ->
                Text(
                    text = "- $step",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun CourseEmptyStateCard(strings: LocalizedStrings) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(strings.courseEmptyTitle, style = MaterialTheme.typography.titleSmall)
            Text(
                text = strings.courseEmptyDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val KaishuFontFamily = FontFamily(Font(R.font.ar_pl_kaiti_m_gb))
@Composable
private fun CanvasSettingsMenu(
    currentGrid: PracticeGrid,
    currentColor: StrokeColorOption,
    showTemplate: Boolean,
    onGridChange: (PracticeGrid) -> Unit,
    onColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggle: (Boolean) -> Unit,
    buttonSize: Dp = 40.dp,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconActionButton(
            icon = Icons.Filled.Settings,
            description = "Canvas settings",
            onClick = { expanded = true },
            buttonSize = buttonSize,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Text(
                text = "Grid",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PracticeGrid.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(mode.label) },
                    onClick = {
                        onGridChange(mode)
                        expanded = false
                    },
                    trailingIcon = if (mode == currentGrid) {
                        { Text("*") }
                    } else null,
                )
            }
            Text(
                text = "Stroke color",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            StrokeColorOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(option.color),
                            )
                            Text(option.label)
                        }
                    },
                    onClick = {
                        onColorChange(option)
                        expanded = false
                    },
                    trailingIcon = if (option == currentColor) {
                        { Text("*") }
                    } else null,
                )
            }
            DropdownMenuItem(
                text = { Text(if (showTemplate) "Hide calligraphy template" else "Show calligraphy template") },
                onClick = {
                    onTemplateToggle(!showTemplate)
                    expanded = false
                },
            )
        }
    }
}
enum class PracticeGrid(val label: String) {
    NONE("None"),
    RICE("Rice grid"),
    NINE("Nine grid"),
}
enum class StrokeColorOption(val label: String, val color: Color) {
    PURPLE("Purple", Color(0xFF6750A4)),
    BLUE("Blue", Color(0xFF2F80ED)),
    GREEN("Green", Color(0xFF2F9B67)),
    RED("Red", Color(0xFFD14343)),
}
private const val CALLIGRAPHY_DEMO_STROKE_DURATION = 600
private const val CALLIGRAPHY_DEMO_STROKE_GAP = 120L
private const val CALLIGRAPHY_DEMO_LOOP_PAUSE = 400L
private fun DrawScope.drawPracticeGrid(mode: PracticeGrid) {
    val color = Color(0xFFE2D6CB)
    val inset = 4.dp.toPx()
    when (mode) {
        PracticeGrid.NONE -> return
        PracticeGrid.RICE -> drawRiceGrid(color, inset)
        PracticeGrid.NINE -> drawNineGrid(color, inset)
    }
}
private fun DrawScope.drawTemplateStrokes(
    definition: CharacterDefinition,
    positioner: Positioner,
    completedStrokes: Set<Int>,
    completedFillColor: Color,
    demoProgress: List<Float>,
) {
    val templateColor = Color(0x33AA6A39)
    val strokeStyle = Stroke(
        width = 12.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    val completedFill = completedFillColor.copy(alpha = 0.65f)
    val completedOutline = completedFillColor.copy(alpha = 0.9f)
    val completedOutlineStyle = Stroke(
        width = 6.dp.toPx(),
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
    )
    definition.strokes.forEach { stroke ->
        val androidPath = AndroidPathParser.createPathFromPathData(stroke.path)
        val scale = positioner.transformScale
        val matrix = AndroidMatrix().apply {
            setValues(
                floatArrayOf(
                    scale,
                    0f,
                    positioner.transformTranslateX,
                    0f,
                    -scale,
                    positioner.transformTranslateY,
                    0f,
                    0f,
                    1f,
                ),
            )
        }
        androidPath.transform(matrix)
        val composePath = androidPath.asComposePath()
        val progress = when {
            completedStrokes.contains(stroke.strokeNum) -> 1f
            else -> demoProgress.getOrNull(stroke.strokeNum) ?: 0f
        }
        if (progress > 0f) {
            val fillAlpha = if (progress >= 1f) 1f else progress
            drawPath(path = composePath, color = completedFill.copy(alpha = completedFill.alpha * fillAlpha), style = Fill)
            drawPath(path = composePath, color = completedOutline.copy(alpha = completedOutline.alpha * fillAlpha), style = completedOutlineStyle)
        } else {
            drawPath(path = composePath, color = templateColor, style = strokeStyle)
        }
    }
}
private fun DrawScope.drawRiceGrid(color: Color, inset: Float) {
    val left = inset
    val right = size.width - inset
    val top = inset
    val bottom = size.height - inset
    val halfWidth = (left + right) / 2f
    val halfHeight = (top + bottom) / 2f
    val strokeWidth = 1.dp.toPx()
    drawLine(color, Offset(halfWidth, top), Offset(halfWidth, bottom), strokeWidth)
    drawLine(color, Offset(left, halfHeight), Offset(right, halfHeight), strokeWidth)
    drawLine(color, Offset(left, top), Offset(right, bottom), strokeWidth)
    drawLine(color, Offset(right, top), Offset(left, bottom), strokeWidth)
}
private fun DrawScope.drawNineGrid(color: Color, inset: Float) {
    val left = inset
    val right = size.width - inset
    val top = inset
    val bottom = size.height - inset
    val width = right - left
    val height = bottom - top
    val thirdWidth = width / 3f
    val thirdHeight = height / 3f
    val strokeWidth = 1.dp.toPx()
    val x1 = left + thirdWidth
    val x2 = left + 2 * thirdWidth
    val y1 = top + thirdHeight
    val y2 = top + 2 * thirdHeight
    drawLine(color, Offset(x1, top), Offset(x1, bottom), strokeWidth)
    drawLine(color, Offset(x2, top), Offset(x2, bottom), strokeWidth)
    drawLine(color, Offset(left, y1), Offset(right, y1), strokeWidth)
    drawLine(color, Offset(left, y2), Offset(right, y2), strokeWidth)
}
@Composable
private fun CharacterGlyphWithGrid(symbol: String, modifier: Modifier = Modifier) {
    val gridColor = Color(0xFFD8CCC2)
    val borderColor = Color(0xFFE7DCD3)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF7F2EE)),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = borderColor,
                cornerRadius = CornerRadius(20.dp.toPx()),
                style = Stroke(width = 2.dp.toPx()),
            )
            val width = size.width
            val height = size.height
            val halfWidth = width / 2f
            val halfHeight = height / 2f
            val strokeWidth = 1.5.dp.toPx()
            drawLine(gridColor, Offset(halfWidth, 0f), Offset(halfWidth, height), strokeWidth)
            drawLine(gridColor, Offset(0f, halfHeight), Offset(width, halfHeight), strokeWidth)
            drawLine(gridColor, Offset(0f, 0f), Offset(width, height), strokeWidth)
            drawLine(gridColor, Offset(width, 0f), Offset(0f, height), strokeWidth)
        }
        Text(
            text = symbol,
            fontFamily = KaishuFontFamily,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp),
            color = Color(0xFF1F1F1F),
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
private fun Modifier.practicePointerInput(
    practiceState: PracticeState,
    positioner: Positioner?,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
): Modifier {
    if (positioner == null) return this
    return pointerInput(practiceState.isActive, positioner) {
        if (!practiceState.isActive) {
            awaitCancellation()
        }
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consumeAllChanges()
            val charPoint = positioner.convertExternalPoint(down.position.toPoint())
            onStrokeStart(charPoint, down.position.toPoint())
            drag(down.id) { change ->
                change.consumeAllChanges()
                val nextPoint = positioner.convertExternalPoint(change.position.toPoint())
                onStrokeMove(nextPoint, change.position.toPoint())
            }
            onStrokeEnd()
        }
    }
}
private fun DrawScope.drawLayer(
    definition: CharacterDefinition,
    layerState: CharacterRenderState,
    baseColor: Color,
    positioner: Positioner,
    strokeWidth: Float,
) {
    if (layerState.opacity <= 0f) return
    definition.strokes.forEach { stroke ->
        val state = layerState.strokes[stroke.strokeNum] ?: return@forEach
        val effectiveOpacity = layerState.opacity * state.opacity
        if (effectiveOpacity <= 0f) return@forEach
        val path = stroke.toPartialPath(positioner, state.displayPortion)
        if (path != null) {
            drawStrokePath(
                path = path,
                color = baseColor.copy(alpha = baseColor.alpha * effectiveOpacity),
                strokeWidth = strokeWidth,
            )
        }
    }
}

@Suppress("DEPRECATION")
private fun PointerInputChange.consumeAllChanges() {
    consumeDownChange()
    consumePositionChange()
}
private fun DrawScope.drawUserStroke(
    userStroke: UserStrokeRenderState,
    positioner: Positioner,
    color: Color,
    drawingWidth: Float,
) {
    if (userStroke.opacity <= 0f || userStroke.points.size < 2) return
    val path = Path().apply {
        val start = positioner.toCanvasOffset(userStroke.points.first())
        moveTo(start.x, start.y)
        userStroke.points.drop(1).forEach { point ->
            val next = positioner.toCanvasOffset(point)
            lineTo(next.x, next.y)
        }
    }
    drawStrokePath(path, color.copy(alpha = userStroke.opacity), drawingWidth)
}
private fun DrawScope.drawStrokePath(path: Path, color: Color, strokeWidth: Float) {
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )
}
private fun ModelStroke.toFullPath(positioner: Positioner): Path {
    return toPartialPath(positioner, 1f) ?: Path()
}
private fun ModelStroke.toPartialPath(positioner: Positioner, portion: Float): Path? {
    val clampedPortion = portion.coerceIn(0f, 1f)
    if (clampedPortion <= 0f) return null
    val totalLength = Geometry.length(points)
    val targetLength = totalLength * clampedPortion
    if (targetLength <= 0.0) return null
    val path = Path()
    var traversed = 0.0
    var previousPoint = points.first()
    var previousCanvas = positioner.toCanvasOffset(previousPoint)
    path.moveTo(previousCanvas.x, previousCanvas.y)
    points.drop(1).forEach { nextPoint ->
        val segLen = Geometry.distance(previousPoint, nextPoint)
        val nextCanvas = positioner.toCanvasOffset(nextPoint)
        val newTraversed = traversed + segLen
        if (newTraversed >= targetLength) {
            val remaining = (targetLength - traversed).coerceAtLeast(0.0)
            val ratio = if (segLen == 0.0) 0.0 else remaining / segLen
            val targetPoint = Point(
                x = previousPoint.x + (nextPoint.x - previousPoint.x) * ratio,
                y = previousPoint.y + (nextPoint.y - previousPoint.y) * ratio,
            )
            val targetCanvas = positioner.toCanvasOffset(targetPoint)
            path.lineTo(targetCanvas.x, targetCanvas.y)
            return path
        } else {
            path.lineTo(nextCanvas.x, nextCanvas.y)
            traversed = newTraversed
            previousPoint = nextPoint
            previousCanvas = nextCanvas
        }
    }
    return path
}
private fun ColorRgba.asComposeColor(alphaMultiplier: Float = 1f): Color {
    val alpha = (a * alphaMultiplier).coerceIn(0f, 1f)
    return Color(
        red = r / 255f,
        green = g / 255f,
        blue = b / 255f,
        alpha = alpha,
    )
}
private fun Offset.toPoint(): Point = Point(x.toDouble(), y.toDouble())
private enum class CourseFilter {
    ALL,
    REMAINING,
    COMPLETED,
    ;

    fun label(strings: LocalizedStrings): String = when (this) {
        ALL -> strings.filterAllLabel
        REMAINING -> strings.filterRemainingLabel
        COMPLETED -> strings.filterCompletedLabel
    }

    fun include(isCompleted: Boolean): Boolean = when (this) {
        ALL -> true
        REMAINING -> !isCompleted
        COMPLETED -> isCompleted
    }
}
