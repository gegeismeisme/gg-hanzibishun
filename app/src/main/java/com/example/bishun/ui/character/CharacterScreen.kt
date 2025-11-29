package com.example.bishun.ui.character

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
        onReset = viewModel::resetCharacter,
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
    onReset: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Hanzi Stroke Order",
                style = MaterialTheme.typography.headlineSmall,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text("Hanzi") },
                    placeholder = { Text("\u6c38") },
                    supportingText = { Text("Single Hanzi") },
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
                )
                IconActionButton(
                    icon = Icons.Filled.Clear,
                    description = "Clear input",
                    onClick = onClearQuery,
                    enabled = query.isNotEmpty(),
                )
                IconActionButton(
                    icon = Icons.Filled.Refresh,
                    description = "Reset canvas",
                    onClick = onReset,
                    enabled = uiState is CharacterUiState.Success,
                )
            }

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
                    demoState = demoState,
                    onPlayDemoOnce = onPlayDemoOnce,
                    onPlayDemoLoop = onPlayDemoLoop,
                    onStopDemo = onStopDemo,
                    onStartPractice = onStartPractice,
                    onRequestHint = onRequestHint,
                    onStrokeStart = onStrokeStart,
                    onStrokeMove = onStrokeMove,
                    onStrokeEnd = onStrokeEnd,
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
    demoState: DemoState,
    onPlayDemoOnce: () -> Unit,
    onPlayDemoLoop: () -> Unit,
    onStopDemo: () -> Unit,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        CharacterGlyphCard(definition)
        TeachingControls(
            symbol = definition.symbol,
            demoState = demoState,
            onPlayOnce = onPlayDemoOnce,
            onPlayLoop = onPlayDemoLoop,
            onStop = onStopDemo,
        )
        CharacterCanvas(
            definition = definition,
            renderSnapshot = renderSnapshot,
            practiceState = practiceState,
            onStrokeStart = onStrokeStart,
            onStrokeMove = onStrokeMove,
            onStrokeEnd = onStrokeEnd,
        )
        PracticePanel(
            symbol = definition.symbol,
            practiceState = practiceState,
            onStartPractice = onStartPractice,
            onRequestHint = onRequestHint,
        )
    }
}

@Composable
private fun CharacterCanvas(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
) {
    val outlineColor = Color(0xFFD6D6D6)
    val teachingStrokeColor = Color(0xFF0F0F0F)
    val canvasBackground = Color.White
    val radicalStrokeColor = MaterialTheme.colorScheme.primary
    val userStrokeColor = MaterialTheme.colorScheme.secondary
    val canvasSizeState = remember { mutableStateOf(IntSize.Zero) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(canvasBackground)
                .onSizeChanged { canvasSizeState.value = it },
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
                val snapshot = renderSnapshot
                if (snapshot == null) {
                    definition.strokes.forEach { stroke ->
                        val path = stroke.toFullPath(drawPositioner)
                        val color = if (stroke.isInRadical) radicalStrokeColor else teachingStrokeColor
                        drawStrokePath(path, color, strokeWidth)
                    }
                } else {
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
        }
    }
}

@Composable
private fun PracticePanel(
    symbol: String,
    practiceState: PracticeState,
    onStartPractice: () -> Unit,
    onRequestHint: () -> Unit,
) {
    val statusText = when {
        practiceState.isComplete -> "Practice complete! Total mistakes ${practiceState.totalMistakes}"
        practiceState.isActive -> "Stroke ${practiceState.currentStrokeIndex + 1}/${max(1, practiceState.totalStrokes)}"
        else -> "Practice not started"
    }
    val progressValue = when {
        practiceState.totalStrokes <= 0 -> 0f
        practiceState.isComplete -> 1f
        practiceState.isActive -> practiceState.currentStrokeIndex.toFloat() / practiceState.totalStrokes
        else -> 0f
    }
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(symbol = symbol, label = "Practice Mode")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconActionButton(
                    icon = Icons.Filled.PlayArrow,
                    description = "Start practice",
                    onClick = onStartPractice,
                    enabled = !practiceState.isActive,
                )
                IconActionButton(
                    icon = Icons.Filled.Info,
                    description = "Hint",
                    onClick = onRequestHint,
                    enabled = practiceState.isActive,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MistakesBadge(practiceState.totalMistakes)
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth(),
            )
            if (practiceState.statusMessage.isNotBlank()) {
                Text(
                    text = practiceState.statusMessage,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TeachingControls(
    symbol: String,
    demoState: DemoState,
    onPlayOnce: () -> Unit,
    onPlayLoop: () -> Unit,
    onStop: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SectionHeader(symbol = symbol, label = "Teaching Demo")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconActionButton(icon = Icons.Filled.PlayArrow, description = "Play demo", onClick = onPlayOnce)
                IconActionButton(icon = Icons.Filled.Refresh, description = "Loop demo", onClick = onPlayLoop)
                IconActionButton(
                    icon = Icons.Filled.Stop,
                    description = "Stop demo",
                    onClick = onStop,
                    enabled = demoState.isPlaying,
                )
            }
            val demoStatus = when {
                demoState.isPlaying && demoState.loop -> "Looping demo..."
                demoState.isPlaying -> "Playing demo..."
                else -> "Demo ready"
            }
            Text(demoStatus, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CharacterGlyphCard(definition: CharacterDefinition) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Current Character",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = definition.symbol,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFF1E1E1E),
            )
            Text(
                text = "${definition.strokeCount} strokes",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SectionHeader(symbol: String, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalligraphyGlyphBadge(symbol)
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun CalligraphyGlyphBadge(symbol: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = symbol,
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MistakesBadge(count: Int) {
    val (containerColor, contentColor) = if (count > 0) {
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = "Mistakes $count",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
            val charPoint = positioner.convertExternalPoint(down.position.toPoint())
            onStrokeStart(charPoint, down.position.toPoint())
            drag(down.id) { change ->
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
