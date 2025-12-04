package com.example.bishun.ui.practice

import androidx.compose.ui.graphics.Color

data class BoardSettings(
    val grid: PracticeGrid = PracticeGrid.NONE,
    val strokeColor: StrokeColorOption = StrokeColorOption.PURPLE,
    val showTemplate: Boolean = true,
)

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
