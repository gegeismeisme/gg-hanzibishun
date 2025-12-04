package com.example.bishun.ui.practice

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.bishun.data.hsk.HskEntry
import com.example.bishun.hanzi.core.Positioner
import com.example.bishun.hanzi.geometry.Geometry
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.model.Stroke as ModelStroke
import com.example.bishun.hanzi.render.CharacterRenderState
import com.example.bishun.hanzi.render.ColorRgba
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.UserStrokeRenderState
import com.example.bishun.ui.character.CourseSession
import com.example.bishun.ui.character.PracticeState
import com.example.bishun.ui.character.components.IconActionButton
import kotlinx.coroutines.awaitCancellation

@Composable
fun CharacterCanvas(
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
    onWordInfoClick: () -> Unit,
    wordInfoAvailable: Boolean,
    onPlayPronunciation: () -> Unit,
    pronunciationAvailable: Boolean,
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
        PracticeBoardControls(
            practiceState = practiceState,
            isDemoPlaying = isDemoPlaying,
            courseSession = courseSession,
            gridMode = gridMode,
            currentColorOption = currentColorOption,
            showTemplate = showTemplate,
            showHskIcon = showHskIcon,
            hskEntry = hskEntry,
            onStartPractice = onStartPractice,
            onRequestHint = onRequestHint,
            onCoursePrev = onCoursePrev,
            onCourseNext = onCourseNext,
            onGridModeChange = onGridModeChange,
            onStrokeColorChange = onStrokeColorChange,
            onTemplateToggle = onTemplateToggle,
            onHskInfoClick = onHskInfoClick,
            onShowWordInfo = onWordInfoClick,
            wordInfoEnabled = wordInfoAvailable,
            onPlayPronunciation = onPlayPronunciation,
            pronunciationEnabled = pronunciationAvailable,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        )
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
                    ?.split(' ', '，', '。', '；')
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
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        join = androidx.compose.ui.graphics.StrokeJoin.Round,
    )
    val completedFill = completedFillColor.copy(alpha = 0.65f)
    val completedOutline = completedFillColor.copy(alpha = 0.9f)
    val completedOutlineStyle = Stroke(
        width = 6.dp.toPx(),
        cap = androidx.compose.ui.graphics.StrokeCap.Round,
        join = androidx.compose.ui.graphics.StrokeJoin.Round,
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
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round,
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
