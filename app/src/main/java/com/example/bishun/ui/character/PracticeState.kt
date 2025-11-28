package com.example.bishun.ui.character

data class PracticeState(
    val isActive: Boolean = false,
    val isComplete: Boolean = false,
    val currentStrokeIndex: Int = 0,
    val totalStrokes: Int = 0,
    val totalMistakes: Int = 0,
    val statusMessage: String = "",
)
