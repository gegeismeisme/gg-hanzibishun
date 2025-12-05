package com.yourstudio.hskstroke.bishun.hanzi.render

data class RenderStateOptions(
    val strokeColor: String = "#555555",
    val radicalColor: String? = null,
    val highlightColor: String = "#AAAAFF",
    val outlineColor: String = "#333333",
    val drawingColor: String = "#333333",
    val drawingFadeDurationMillis: Long = 300,
    val drawingWidth: Float = 4f,
    val strokeWidth: Float = 2f,
    val outlineWidth: Float = 2f,
    val showCharacter: Boolean = true,
    val showOutline: Boolean = true,
) {
    fun toRenderOptionsState(): RenderOptionsState {
        val strokeColorRgba = ColorParser.parse(strokeColor)
        val radical = radicalColor?.let(ColorParser::parse) ?: strokeColorRgba
        return RenderOptionsState(
            drawingFadeDuration = drawingFadeDurationMillis,
            drawingWidth = drawingWidth,
            drawingColor = ColorParser.parse(drawingColor),
            strokeColor = strokeColorRgba,
            outlineColor = ColorParser.parse(outlineColor),
            radicalColor = radical,
            highlightColor = ColorParser.parse(highlightColor),
        )
    }
}

data class RenderOptionsState(
    val drawingFadeDuration: Long,
    val drawingWidth: Float,
    val drawingColor: ColorRgba,
    val strokeColor: ColorRgba,
    val outlineColor: ColorRgba,
    val radicalColor: ColorRgba,
    val highlightColor: ColorRgba,
)
