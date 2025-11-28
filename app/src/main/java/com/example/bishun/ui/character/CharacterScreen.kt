package com.example.bishun.ui.character

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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

    CharacterScreen(
        modifier = modifier,
        query = query,
        uiState = uiState,
        renderSnapshot = renderSnapshot,
        onQueryChange = viewModel::updateQuery,
        onSubmit = viewModel::submitQuery,
        onPlayDemo = viewModel::playDemo,
        onReset = viewModel::resetCharacter,
    )
}

@Composable
fun CharacterScreen(
    query: String,
    uiState: CharacterUiState,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onPlayDemo: () -> Unit,
    onReset: () -> Unit,
    renderSnapshot: RenderStateSnapshot?,
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
                text = "汉字笔顺",
                style = MaterialTheme.typography.headlineSmall,
            )
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("输入汉字") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSubmit) {
                    Text("加载")
                }
                Button(onClick = onPlayDemo, enabled = uiState is CharacterUiState.Success) {
                    Text("演示")
                }
                Button(onClick = onReset, enabled = uiState is CharacterUiState.Success) {
                    Text("重置")
                }
            }
            when (uiState) {
                CharacterUiState.Loading -> Text("加载中...")
                is CharacterUiState.Error -> Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                )
                is CharacterUiState.Success -> CharacterCanvas(
                    definition = uiState.definition,
                    renderSnapshot = renderSnapshot,
                )
            }
        }
    }
}

@Composable
private fun CharacterCanvas(
    definition: CharacterDefinition,
    renderSnapshot: RenderStateSnapshot?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val outlineColor = colorScheme.outlineVariant
        val radicalColor = colorScheme.primary
        val strokeColor = colorScheme.onSurface
        val gridBackground = colorScheme.surfaceVariant
        Text(
            text = "字形：${definition.symbol}",
            style = MaterialTheme.typography.titleMedium,
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(gridBackground),
        ) {
            val strokeWidth = 6.dp.toPx()
            val positioner = Positioner(size.width, size.height, padding = 32f)
            drawRect(
                color = outlineColor,
                style = Stroke(width = 1.dp.toPx()),
            )
            val snapshot = renderSnapshot
            if (snapshot == null) {
                definition.strokes.forEach { stroke ->
                    val path = stroke.toFullPath(positioner)
                    val color = if (stroke.isInRadical) radicalColor else strokeColor
                    drawStrokePath(path, color, strokeWidth)
                }
            } else {
                drawLayer(
                    definition = definition,
                    layerState = snapshot.character.outline,
                    baseColor = outlineColor,
                    positioner = positioner,
                    strokeWidth = strokeWidth,
                )
                drawLayer(
                    definition = definition,
                    layerState = snapshot.character.main,
                    baseColor = strokeColor,
                    positioner = positioner,
                    strokeWidth = strokeWidth,
                )
                drawLayer(
                    definition = definition,
                    layerState = snapshot.character.highlight,
                    baseColor = snapshot.options.highlightColor.asComposeColor(),
                    positioner = positioner,
                    strokeWidth = strokeWidth,
                )
            }
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

private fun ModelStroke.toPartialPath(
    positioner: Positioner,
    portion: Float,
): Path? {
    val clampedPortion = portion.coerceIn(0f, 1f)
    if (clampedPortion <= 0f) return null
    val targetLength = Geometry.length(points) * clampedPortion
    if (targetLength <= 0.0) return null
    val path = Path()
    var accumulated = 0.0
    var previousPoint = points.first()
    var previousCanvas = positioner.toCanvasOffset(previousPoint)
    path.moveTo(previousCanvas.x, previousCanvas.y)
    points.drop(1).forEach { nextPoint ->
        val segmentLength = Geometry.distance(previousPoint, nextPoint)
        val nextCanvas = positioner.toCanvasOffset(nextPoint)
        val newAccumulated = accumulated + segmentLength
        if (newAccumulated >= targetLength) {
            val remaining = (targetLength - accumulated).coerceAtLeast(0.0)
            val ratio = if (segmentLength == 0.0) 0.0 else remaining / segmentLength
            val targetPoint = Point(
                x = previousPoint.x + (nextPoint.x - previousPoint.x) * ratio,
                y = previousPoint.y + (nextPoint.y - previousPoint.y) * ratio,
            )
            val targetCanvas = positioner.toCanvasOffset(targetPoint)
            path.lineTo(targetCanvas.x, targetCanvas.y)
            return path
        } else {
            path.lineTo(nextCanvas.x, nextCanvas.y)
            accumulated = newAccumulated
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
