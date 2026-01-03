package com.yourstudio.hskstroke.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.di.CharacterDataModule
import com.yourstudio.hskstroke.bishun.data.daily.DailyPracticeUseCase
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.WordRepository
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.hsk.HskCourseCatalogBuilder
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressCalculator
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressSummary
import com.yourstudio.hskstroke.bishun.data.hsk.HskRepository
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressStore
import com.yourstudio.hskstroke.bishun.data.history.PracticeHistoryEntry
import com.yourstudio.hskstroke.bishun.data.history.PracticeHistoryStore
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderState
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.ui.practice.BoardSettings
import com.yourstudio.hskstroke.bishun.ui.practice.PracticeGrid
import com.yourstudio.hskstroke.bishun.ui.practice.StrokeColorOption
import com.yourstudio.hskstroke.bishun.widget.DailyHanziWidgetUpdater
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class CharacterViewModel(
    private val appContext: Context,
    private val repository: CharacterDefinitionRepository,
    private val wordRepository: WordRepository,
    private val hskRepository: HskRepository,
    private val hskProgressStore: HskProgressStore,
    private val practiceHistoryStore: PracticeHistoryStore,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _query = MutableStateFlow(DEFAULT_CHAR)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private val _renderSnapshot = MutableStateFlow<RenderStateSnapshot?>(null)
    val renderSnapshot: StateFlow<RenderStateSnapshot?> = _renderSnapshot.asStateFlow()
    private val renderController = PracticeRenderController(
        scope = viewModelScope,
        snapshots = _renderSnapshot,
    )
    private val renderState: RenderState?
        get() = renderController.state

    private val _practiceState = MutableStateFlow(PracticeState())
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()
    private val _demoState = MutableStateFlow(DemoState())
    val demoState: StateFlow<DemoState> = _demoState.asStateFlow()
    private val wordInfoController = WordInfoController(
        wordRepository = wordRepository,
        scope = viewModelScope,
        currentSymbol = { currentDefinition?.symbol },
    )
    val wordEntry: StateFlow<WordEntry?> = wordInfoController.wordEntry
    val wordInfoUiState: StateFlow<WordInfoUiState> = wordInfoController.uiState
    private val _hskEntry = MutableStateFlow<HskEntry?>(null)
    val hskEntry: StateFlow<HskEntry?> = _hskEntry.asStateFlow()
    private val _hskProgress = MutableStateFlow(HskProgressSummary())
    val hskProgress: StateFlow<HskProgressSummary> = _hskProgress.asStateFlow()
    private val _practiceHistory = MutableStateFlow<List<PracticeHistoryEntry>>(emptyList())
    val practiceHistory: StateFlow<List<PracticeHistoryEntry>> = _practiceHistory.asStateFlow()
    private val _courseSession = MutableStateFlow<CourseSession?>(null)
    val courseSession: StateFlow<CourseSession?> = _courseSession.asStateFlow()
    private val _practiceQueueSession = MutableStateFlow<PracticeQueueSession?>(null)
    val practiceQueueSession: StateFlow<PracticeQueueSession?> = _practiceQueueSession.asStateFlow()
    private val _boardSettings = MutableStateFlow(BoardSettings())
    val boardSettings: StateFlow<BoardSettings> = _boardSettings.asStateFlow()
    private val _courseCatalog = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val courseCatalog: StateFlow<Map<Int, List<String>>> = _courseCatalog.asStateFlow()
    private val _completedSymbols = MutableStateFlow<Set<String>>(emptySet())
    val completedSymbols: StateFlow<Set<String>> = _completedSymbols.asStateFlow()
    private val _courseEvents = MutableSharedFlow<CourseEvent>()
    val courseEvents: SharedFlow<CourseEvent> = _courseEvents.asSharedFlow()
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val courseSessionController = CourseSessionController(
        scope = viewModelScope,
        userPreferencesStore = userPreferencesStore,
        hskProgressStore = hskProgressStore,
        courseSession = _courseSession,
        practiceQueueSession = _practiceQueueSession,
        courseEvents = _courseEvents,
        loadCharacter = ::loadCharacter,
    )

    private val practiceController = PracticeInteractionController(
        scope = viewModelScope,
        practiceState = _practiceState,
        demoState = _demoState,
        definitionProvider = { currentDefinition },
        renderStateProvider = { renderState },
        renderSnapshotProvider = { _renderSnapshot.value },
        onPracticeCompleted = ::handlePracticeCompleted,
    )

    private var currentDefinition: CharacterDefinition? = null
    private var loadCharacterJob: Job? = null
    private var loadCharacterToken: Long = 0
    private var pendingAutoStartPracticeToken: Long? = null
    private var pendingAutoStartPracticeSymbol: String? = null
    private var dailyDetailsJob: Job? = null
    private var dailyDetailsRequest: Pair<String, Long>? = null
    private var courseEntries: Map<Int, List<String>> = emptyMap()

    init {
        viewModelScope.launch {
            val catalog = loadCourseCatalogIfNeeded()
            updateHskProgress(hskProgressStore.completed.value, catalog)
        }
        observeHskProgress()
        observePracticeHistory()
        observeUserPreferences()
        loadCharacter(DEFAULT_CHAR)
    }

    fun updateQuery(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            _query.value = ""
            return
        }
        val codePoint = Character.codePointAt(trimmed, 0)
        _query.value = String(Character.toChars(codePoint))
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun submitQuery() {
        loadCharacter(_query.value)
    }

    fun jumpToCharacter(symbol: String) {
        loadCharacter(symbol)
    }

    fun startPracticeForSymbol(symbol: String) {
        val trimmed = symbol.trim()
        if (trimmed.isBlank()) return
        val target = firstCodePoint(trimmed)
        if (target.isBlank()) return
        if (currentDefinition?.symbol == target) {
            startPractice()
            return
        }

        pendingAutoStartPracticeToken = loadCharacterToken + 1
        pendingAutoStartPracticeSymbol = target
        loadCharacter(target)
    }

    fun startDailyPractice() {
        val zone = ZoneId.systemDefault()
        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val suggestedSymbol = pickDailySymbol(_hskProgress.value)?.trim()?.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            val snapshot = DailyPracticeUseCase.ensureTodaySnapshot(
                context = appContext,
                todayEpochDay = todayEpochDay,
                suggestedSymbol = suggestedSymbol,
                ensureDetails = false,
                preferencesStore = userPreferencesStore,
                wordRepository = wordRepository,
            )
            val resolvedSymbol = snapshot.symbol ?: return@launch
            runCatching { DailyHanziWidgetUpdater.updateAll(appContext) }
            startPracticeForSymbol(resolvedSymbol)
        }
    }

    fun ensureDailyPracticeDetailsLoaded() {
        val zone = ZoneId.systemDefault()
        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val prefs = _userPreferences.value
        val symbol = prefs.dailySymbol
            ?.trim()
            ?.takeIf { it.isNotBlank() && prefs.dailyEpochDay == todayEpochDay }
            ?: return

        val hasDetails = !prefs.dailyPinyin.isNullOrBlank() || !prefs.dailyExplanationSummary.isNullOrBlank()
        if (hasDetails) return

        val requestKey = symbol to todayEpochDay
        if (dailyDetailsJob?.isActive == true && dailyDetailsRequest == requestKey) return
        dailyDetailsRequest = requestKey

        dailyDetailsJob = viewModelScope.launch {
            DailyPracticeUseCase.ensureTodaySnapshot(
                context = appContext,
                todayEpochDay = todayEpochDay,
                ensureDetails = true,
                preferencesStore = userPreferencesStore,
                wordRepository = wordRepository,
            )
            runCatching { DailyHanziWidgetUpdater.updateAll(appContext) }
        }
    }

    fun startPracticeQueue(symbols: List<String>) {
        courseSessionController.startPracticeQueue(symbols)
    }

    fun goToNextPracticeQueueCharacter() {
        courseSessionController.goToNextPracticeQueueCharacter()
    }

    fun goToPreviousPracticeQueueCharacter() {
        courseSessionController.goToPreviousPracticeQueueCharacter()
    }

    fun restartPracticeQueue() {
        courseSessionController.restartPracticeQueue()
    }

    fun exitPracticeQueue() {
        courseSessionController.exitPracticeQueue()
    }

    fun startCourse(level: Int, symbol: String) {
        courseSessionController.startCourse(level, symbol, currentDefinition?.symbol)
    }

    fun goToNextCourseCharacter() {
        courseSessionController.goToNextCourseCharacter()
    }

    fun goToPreviousCourseCharacter() {
        courseSessionController.goToPreviousCourseCharacter()
    }

    fun skipCourseCharacter() {
        courseSessionController.skipCourseCharacter()
    }

    fun restartCourseLevel() {
        courseSessionController.restartCourseLevel()
    }

    fun markCourseCharacterLearned(symbol: String) {
        courseSessionController.markCourseCharacterLearned(symbol)
    }

    fun playDemo(loop: Boolean = false) {
        practiceController.playDemo(loop)
    }

    fun stopDemo() {
        practiceController.stopDemo()
    }

    fun resetCharacter() {
        practiceController.resetCharacter()
    }

    fun startPractice() {
        practiceController.startPractice()
    }

    fun onPracticeStrokeStart(charPoint: Point, externalPoint: Point) {
        practiceController.onPracticeStrokeStart(charPoint, externalPoint)
    }

    fun onPracticeStrokeMove(charPoint: Point, externalPoint: Point) {
        practiceController.onPracticeStrokeMove(charPoint, externalPoint)
    }

    fun onPracticeStrokeEnd() {
        practiceController.onPracticeStrokeEnd()
    }

    private fun loadCharacter(input: String) {
        val trimmed = input.trim().ifEmpty { return }
        val symbol = firstCodePoint(trimmed)
        loadCharacterJob?.cancel()
        loadCharacterToken += 1
        val token = loadCharacterToken
        _query.value = symbol
        _uiState.value = CharacterUiState.Loading
        loadCharacterJob = viewModelScope.launch {
            val result = repository.load(symbol)
            if (token != loadCharacterToken) return@launch
            result.fold(
                onSuccess = {
                    _uiState.value = CharacterUiState.Success(it)
                    currentDefinition = it
                    setupRenderState(it)
                    practiceController.onDefinitionLoaded(it)
                    wordInfoController.reset()
                    loadHskInfo(it.symbol)
                    courseSessionController.alignSessions(it.symbol)
                    if (pendingAutoStartPracticeToken == token && pendingAutoStartPracticeSymbol == it.symbol) {
                        pendingAutoStartPracticeToken = null
                        pendingAutoStartPracticeSymbol = null
                        startPractice()
                    }
                },
                onFailure = {
                    val error = when (it) {
                        is java.io.IOException -> CharacterLoadError.NotFound
                        else -> CharacterLoadError.LoadFailed
                    }
                    _uiState.value = CharacterUiState.Error(error)
                    wordInfoController.reset()
                    _hskEntry.value = null
                    if (pendingAutoStartPracticeToken == token) {
                        pendingAutoStartPracticeToken = null
                        pendingAutoStartPracticeSymbol = null
                    }
                },
            )
        }
    }

    private fun setupRenderState(definition: CharacterDefinition) {
        renderController.setup(definition)
    }

    fun requestWordInfo(symbol: String? = currentDefinition?.symbol) {
        wordInfoController.request(symbol)
    }

    private fun loadHskInfo(symbol: String) {
        viewModelScope.launch {
            runCatching { hskRepository.get(symbol) }
                .onSuccess { _hskEntry.value = it }
                .onFailure { _hskEntry.value = null }
        }
    }

    private fun observeHskProgress() {
        viewModelScope.launch {
            hskProgressStore.completed.collect { completed ->
                _completedSymbols.value = completed
                val catalog = loadCourseCatalogIfNeeded()
                updateHskProgress(completed, catalog)
            }
        }
    }

    private suspend fun loadCourseCatalogIfNeeded(): Map<Int, List<String>> {
        if (courseEntries.isNotEmpty()) {
            if (_courseCatalog.value.isEmpty()) {
                _courseCatalog.value = courseEntries
            }
            courseSessionController.updateCatalog(courseEntries)
            courseSessionController.restoreCourseSession(
                level = _userPreferences.value.courseLevel,
                symbol = _userPreferences.value.courseSymbol,
            )
            return courseEntries
        }

        val entries = runCatching { hskRepository.allEntries().toList() }
            .getOrElse { emptyList() }
        val catalog = HskCourseCatalogBuilder.build(entries)

        courseEntries = catalog
        _courseCatalog.value = catalog
        courseSessionController.updateCatalog(catalog)
        courseSessionController.restoreCourseSession(
            level = _userPreferences.value.courseLevel,
            symbol = _userPreferences.value.courseSymbol,
        )
        return catalog
    }

    private fun updateHskProgress(completed: Set<String>, catalog: Map<Int, List<String>>) {
        val summary = HskProgressCalculator.calculateSummary(completed, catalog)
        _hskProgress.value = summary
        maybeRefreshDailyPractice(summary)
    }

    private fun maybeRefreshDailyPractice(summary: HskProgressSummary) {
        val zone = ZoneId.systemDefault()
        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val prefs = _userPreferences.value
        val storedEpochDay = prefs.dailyEpochDay
        val storedSymbol = prefs.dailySymbol?.trim()?.takeIf { it.isNotBlank() }
        val suggestedSymbol = pickDailySymbol(summary)?.trim()?.takeIf { it.isNotBlank() }

        val shouldUpdate = storedEpochDay != todayEpochDay || (storedSymbol == null && suggestedSymbol != null)
        if (!shouldUpdate) return

        viewModelScope.launch {
            DailyPracticeUseCase.ensureTodaySnapshot(
                context = appContext,
                todayEpochDay = todayEpochDay,
                suggestedSymbol = suggestedSymbol,
                ensureDetails = false,
                preferencesStore = userPreferencesStore,
                wordRepository = wordRepository,
            )
            runCatching { DailyHanziWidgetUpdater.updateAll(appContext) }
        }
    }

    private fun observePracticeHistory() {
        viewModelScope.launch {
            practiceHistoryStore.history.collect { entries ->
                _practiceHistory.value = entries
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesStore.data.collect { prefs ->
                _userPreferences.value = prefs
                _boardSettings.value = BoardSettings(
                    grid = PracticeGrid.entries.getOrElse(prefs.gridMode) { PracticeGrid.NONE },
                    strokeColor = StrokeColorOption.entries.getOrElse(prefs.strokeColor) { StrokeColorOption.PURPLE },
                    showTemplate = prefs.showTemplate,
                )
                if (prefs.courseLevel == null || prefs.courseSymbol == null) {
                    _courseSession.value = null
                } else {
                    _courseSession.value = null
                    courseSessionController.restoreCourseSession(prefs.courseLevel, prefs.courseSymbol)
                }
            }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            practiceHistoryStore.clear()
            hskProgressStore.clear()
            userPreferencesStore.clearAll()
            _practiceHistory.value = emptyList()
            _completedSymbols.value = emptySet()
            _courseSession.value = null
            _practiceQueueSession.value = null
            _userPreferences.value = UserPreferences()
            _boardSettings.value = BoardSettings()
            val catalog = loadCourseCatalogIfNeeded()
            updateHskProgress(emptySet(), catalog)
        }
    }

    fun updateGridMode(mode: PracticeGrid) {
        viewModelScope.launch {
            userPreferencesStore.setGridMode(mode.ordinal)
        }
    }

    fun updateStrokeColor(option: StrokeColorOption) {
        viewModelScope.launch {
            userPreferencesStore.setStrokeColor(option.ordinal)
        }
    }

    fun updateTemplateVisibility(show: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setShowTemplate(show)
        }
    }

    fun setLanguageOverride(localeTag: String?) {
        viewModelScope.launch {
            userPreferencesStore.setLanguageOverride(localeTag)
        }
    }

    fun clearCourseSession() {
        courseSessionController.clearCourseSession()
    }

    private fun firstCodePoint(input: String): String {
        val codePoint = Character.codePointAt(input, 0)
        return String(Character.toChars(codePoint))
    }

    private fun handlePracticeCompleted(completion: PracticeInteractionController.PracticeCompletion) {
        viewModelScope.launch {
            hskProgressStore.add(completion.symbol)
            practiceHistoryStore.record(
                PracticeHistoryEntry(
                    symbol = completion.symbol,
                    timestamp = System.currentTimeMillis(),
                    totalStrokes = completion.totalStrokes,
                    mistakes = completion.mistakes,
                    completed = true,
                ),
            )
            val todayEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
            userPreferencesStore.recordPracticeCompletion(completion.symbol, todayEpochDay)
            runCatching { DailyHanziWidgetUpdater.updateAll(appContext) }
        }
        courseSessionController.advanceAfterPracticeCompletion(completion.symbol)
    }

    fun requestHint() {
        practiceController.requestHint()
    }

    override fun onCleared() {
        super.onCleared()
        renderController.dispose()
        wordInfoController.reset()
    }

    companion object {
        private const val DEFAULT_CHAR = "\u6c38"

        fun factory(appContext: Context): ViewModelProvider.Factory {
            val applicationContext = appContext.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = CharacterDataModule.provideDefinitionRepository(applicationContext)
                    val wordRepo = WordRepository(applicationContext)
                    val hskRepo = HskRepository(applicationContext)
                    val progressStore = HskProgressStore(applicationContext)
                    val historyStore = PracticeHistoryStore(applicationContext)
                    val prefsStore = UserPreferencesStore(applicationContext)
                    return CharacterViewModel(
                        applicationContext,
                        repo,
                        wordRepo,
                        hskRepo,
                        progressStore,
                        historyStore,
                        prefsStore,
                    ) as T
                }
            }
        }
    }
}

