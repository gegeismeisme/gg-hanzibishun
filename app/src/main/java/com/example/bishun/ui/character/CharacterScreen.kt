package com.example.bishun.ui.character

import android.graphics.Matrix as AndroidMatrix
import androidx.core.graphics.PathParser as AndroidPathParser
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
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
import kotlin.math.max
import kotlin.math.min

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

    CharacterScreen(
        modifier = modifier,
        query = query,
        uiState = uiState,
        practiceState = practiceState,
        renderSnapshot = renderSnapshot,
        demoState = demoState,
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
    )
}

@Composable
fun CharacterScreen(
    query: String,
    uiState: CharacterUiState,
    practiceState: PracticeState,
    renderSnapshot: RenderStateSnapshot?,
    demoState: DemoState,
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
    modifier: Modifier = Modifier,
) {
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
                onPlayDemoOnce = onPlayDemoOnce,
                onPlayDemoLoop = onPlayDemoLoop,
                onStopDemo = onStopDemo,
            )
            when (uiState) {
                CharacterUiState.Loading -> Text("Loading...")
                is CharacterUiState.Error -> Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                )
                is CharacterUiState.Success -> CharacterContent(
                    definition = uiState.definition,
                    renderSnapshot = renderSnapshot,
                    practiceState = practiceState,
                    onStartPractice = onStartPractice,
                    onRequestHint = onRequestHint,
                    onStrokeStart = onStrokeStart,
                    onStrokeMove = onStrokeMove,
                    onStrokeEnd = onStrokeEnd,
                    modifier = Modifier.weight(1f),
                )
            }
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
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Hanzi Stroke Order",
            style = MaterialTheme.typography.headlineSmall,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Hanzi") },
                placeholder = { Text("\u6c38") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.width(96.dp),
            )
            IconActionButton(
                icon = Icons.Filled.CloudDownload,
                description = "Load character",
                onClick = onSubmit,
                enabled = query.isNotBlank(),
                buttonSize = 36.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Clear,
                description = "Clear input",
                onClick = onClearQuery,
                enabled = query.isNotEmpty(),
                buttonSize = 36.dp,
            )
            Spacer(modifier = Modifier.weight(1f, fill = true))
            DemoControlRow(
                uiState = uiState,
                demoState = demoState,
                onSubmit = onSubmit,
                onPlayOnce = onPlayDemoOnce,
                onPlayLoop = onPlayDemoLoop,
                onStop = onStopDemo,
            )
        }
    }
}

@Composable
private fun CharacterContent(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(),
    ) {
        CharacterInfoPanel(
            definition = definition,
            modifier = Modifier.fillMaxWidth(),
        )
        val summary = practiceState.toSummary()
        PracticeSummaryBadge(
            progressText = summary.progressText,
            statusText = summary.statusText,
            modifier = Modifier.fillMaxWidth(),
        )
        val gridState = rememberSaveable { mutableStateOf(PracticeGrid.NONE.ordinal) }
        val colorState = rememberSaveable { mutableStateOf(StrokeColorOption.PURPLE.ordinal) }
        val templateState = rememberSaveable { mutableStateOf(true) }
        val gridMode = PracticeGrid.entries[gridState.value]
        val strokeColorOption = StrokeColorOption.entries[colorState.value]
        val strokeColor = strokeColorOption.color
        CharacterCanvas(
            definition = definition,
            renderSnapshot = renderSnapshot,
            practiceState = practiceState,
            gridMode = gridMode,
            userStrokeColor = strokeColor,
            showTemplate = templateState.value,
            currentColorOption = strokeColorOption,
            onGridModeChange = { gridState.value = it.ordinal },
            onStrokeColorChange = { colorState.value = it.ordinal },
            onTemplateToggle = { templateState.value = it },
            onStrokeStart = onStrokeStart,
            onStrokeMove = onStrokeMove,
            onStrokeEnd = onStrokeEnd,
            onStartPractice = onStartPractice,
            onRequestHint = onRequestHint,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
        )
    }
}

@Composable
private fun DemoControlRow(
    uiState: CharacterUiState,
    demoState: DemoState,
    onSubmit: () -> Unit,
    onPlayOnce: () -> Unit,
    onPlayLoop: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (demoState.isPlaying) {
            IconActionButton(
                icon = Icons.Filled.Stop,
                description = "Stop demo",
                onClick = onStop,
                buttonSize = 36.dp,
            )
        } else {
            IconActionButton(
                icon = Icons.Filled.PlayArrow,
                description = "Play demo",
                onClick = {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayOnce()
                    }
                },
                buttonSize = 36.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Refresh,
                description = "Loop demo",
                onClick = {
                    if (uiState !is CharacterUiState.Success) {
                        onSubmit()
                    } else {
                        onPlayLoop()
                    }
                },
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
    gridMode: PracticeGrid,
    userStrokeColor: Color,
    showTemplate: Boolean,
    currentColorOption: StrokeColorOption,
    onGridModeChange: (PracticeGrid) -> Unit,
    onStrokeColorChange: (StrokeColorOption) -> Unit,
    onTemplateToggle: (Boolean) -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
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
            IconActionButton(
                icon = Icons.Filled.PlayArrow,
                description = "Start practice",
                onClick = onStartPractice,
                enabled = !practiceState.isActive,
                buttonSize = 36.dp,
            )
            IconActionButton(
                icon = Icons.Filled.Info,
                description = "Hint",
                onClick = onRequestHint,
                enabled = practiceState.isActive,
                buttonSize = 36.dp,
            )
            CanvasSettingsMenu(
                currentGrid = gridMode,
                currentColor = currentColorOption,
                showTemplate = showTemplate,
                onGridChange = onGridModeChange,
                onColorChange = onStrokeColorChange,
                onTemplateToggle = onTemplateToggle,
                buttonSize = 36.dp,
            )
        }
    }
}

@Composable
private fun CharacterInfoPanel(
    definition: CharacterDefinition,
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
            Spacer(modifier = Modifier.weight(1f))
        }
    }
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
                        { Text("✓") }
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
                        { Text("✓") }
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

private enum class PracticeGrid(val label: String) {
    NONE("None"),
    RICE("Rice grid"),
    NINE("Nine grid"),
}

private enum class StrokeColorOption(val label: String, val color: Color) {
    PURPLE("Purple", Color(0xFF6750A4)),
    BLUE("Blue", Color(0xFF2F80ED)),
    GREEN("Green", Color(0xFF2F9B67)),
    RED("Red", Color(0xFFD14343)),
}

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
        if (completedStrokes.contains(stroke.strokeNum)) {
            drawPath(path = composePath, color = completedFill, style = Fill)
            drawPath(path = composePath, color = completedOutline, style = completedOutlineStyle)
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
            down.consumeDownChange()
            val charPoint = positioner.convertExternalPoint(down.position.toPoint())
            onStrokeStart(charPoint, down.position.toPoint())
            drag(down.id) { change ->
                change.consumePositionChange()
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
