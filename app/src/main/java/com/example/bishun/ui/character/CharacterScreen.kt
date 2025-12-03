package com.example.bishun.ui.character
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bishun.data.word.WordEntry
import com.example.bishun.data.hsk.HskEntry
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.bishun.data.settings.UserPreferences
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.ui.character.components.IconActionButton
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
import com.example.bishun.ui.practice.CalligraphyDemoState
import com.example.bishun.ui.practice.PracticeContent
import com.example.bishun.ui.practice.PracticeGrid
import com.example.bishun.ui.practice.StrokeColorOption
import com.example.bishun.ui.practice.WordInfoDialog
import com.example.bishun.ui.practice.rememberCalligraphyDemoController
import com.example.bishun.ui.practice.rememberTextToSpeechController
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
                    Text("Courses now live under the bottom navigation 'Courses' tab. Use that tab to browse lessons, resume sessions, or unlock content.")
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("Got it") }
                },
            )
        }
        ProfileMenuAction.PROGRESS -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(action.label) },
                text = {
                    Text("Progress analytics moved to the bottom navigation 'Progress' tab. Open that tab to review streaks, HSK status, and history.")
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) { Text("Got it") }
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

private fun formatHistoryTimestamp(timestamp: Long): String {
    return try {
        val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } catch (_: Exception) {
        "-"
    }
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
private const val SUPPORT_EMAIL = "qq260316514@gmail.com"
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

private data class CourseSymbolVisual(val symbol: String, val isActive: Boolean, val isCompleted: Boolean)

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
