package com.example.bishun.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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

    CharacterScreen(
        modifier = modifier,
        query = query,
        uiState = uiState,
        practiceState = practiceState,
        renderSnapshot = renderSnapshot,
        onQueryChange = viewModel::updateQuery,
        onSubmit = viewModel::submitQuery,
        onPlayDemo = viewModel::playDemo,
        onReset = viewModel::resetCharacter,
        onStartPractice = viewModel::startPractice,
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
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPlayDemo: () -> Unit,
    onReset: () -> Unit,
    onStartPractice: () -> Unit,
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
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Enter Hanzi") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSubmit) { Text("Load") }
                Button(onClick = onPlayDemo, enabled = uiState is CharacterUiState.Success) {
                    Text("Demo")
                }
                Button(onClick = onReset, enabled = uiState is CharacterUiState.Success) {
                    Text("Reset")
                }
            }
            when (uiState) {
                CharacterUiState.Loading -> Text("Loading...")
                is CharacterUiState.Error -> Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                )
                is CharacterUiState.Success -> CharacterCanvas(
                    definition = uiState.definition,
                    renderSnapshot = renderSnapshot,
                    practiceState = practiceState,
                    onStartPractice = onStartPractice,
                    onStrokeStart = onStrokeStart,
                    onStrokeMove = onStrokeMove,
                    onStrokeEnd = onStrokeEnd,
                )
            }
        }
    }
}

@Composable
private fun CharacterCanvas(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    practiceState: PracticeState,
    onStartPractice: () -> Unit,
    onStrokeStart: (Point, Point) -> Unit,
    onStrokeMove: (Point, Point) -> Unit,
    onStrokeEnd: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val outlineColor = colorScheme.outlineVariant
    val strokeColor = colorScheme.onSurface
    val gridBackground = colorScheme.surfaceVariant
    val practiceColor = colorScheme.primary
    val userStrokeColor = renderSnapshot?.options?.drawingColor?.asComposeColor() ?: colorScheme.primary
    val canvasSizeState = remember { mutableStateOf(IntSize.Zero) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Character: ${definition.symbol}",
            style = MaterialTheme.typography.titleMedium,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
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
                    .background(gridBackground)
                    .practicePointerInput(
                        practiceState = practiceState,
                        positioner = positioner,
                        onStrokeStart = onStrokeStart,
                        onStrokeMove = onStrokeMove,
                        onStrokeEnd = onStrokeEnd,
                    ),
            ) {
                val strokeWidth = 6.dp.toPx()
                val drawPositioner = Positioner(size.width, size.height, padding = 32f)
                drawRect(
                    color = outlineColor,
                    style = Stroke(width = 1.dp.toPx()),
                )
                val snapshot = renderSnapshot
                if (snapshot == null) {
                    definition.strokes.forEach { stroke ->
                        val path = stroke.toFullPath(drawPositioner)
                        val color = if (stroke.isInRadical) practiceColor else strokeColor
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
                        baseColor = strokeColor,
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
            PracticeControls(
                practiceState = practiceState,
                isCanvasReady = positioner != null,
                onStartPractice = onStartPractice,
            )
        }
        PracticeStatus(practiceState)
    }
}

@Composable
private fun BoxScope.PracticeControls(
    practiceState: PracticeState,
    isCanvasReady: Boolean,
    onStartPractice: () -> Unit,
) {
    Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onStartPractice,
            enabled = isCanvasReady && !practiceState.isActive,
        ) {
            Text("Start Practice")
        }
    }
}

@Composable
private fun PracticeStatus(practiceState: PracticeState) {
    val statusText = when {
        practiceState.isComplete -> "Practice complete! Total mistakes ${practiceState.totalMistakes}"
        practiceState.isActive -> "Stroke ${practiceState.currentStrokeIndex + 1}/${max(1, practiceState.totalStrokes)}, mistakes ${practiceState.totalMistakes}"
        else -> "Practice not started"
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = statusText, style = MaterialTheme.typography.bodyMedium)
        if (practiceState.statusMessage.isNotBlank()) {
            Text(text = practiceState.statusMessage, color = MaterialTheme.colorScheme.secondary)
        }
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
                change.consume()
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
