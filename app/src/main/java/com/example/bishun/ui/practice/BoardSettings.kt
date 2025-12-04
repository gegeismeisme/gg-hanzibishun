package com.example.bishun.ui.practice

data class BoardSettings(
    val grid: PracticeGrid = PracticeGrid.NONE,
    val strokeColor: StrokeColorOption = StrokeColorOption.PURPLE,
    val showTemplate: Boolean = true,
)
