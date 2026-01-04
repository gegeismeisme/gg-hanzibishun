package com.yourstudio.hskstroke.bishun.ui.practice

enum class BrushWidthOption(
    val label: String,
    val strokeWidthPx: Float,
    val requiresPro: Boolean,
) {
    Thin(
        label = "Thin",
        strokeWidthPx = 3f,
        requiresPro = false,
    ),
    Regular(
        label = "Regular",
        strokeWidthPx = 4f,
        requiresPro = false,
    ),
    Bold(
        label = "Bold",
        strokeWidthPx = 6f,
        requiresPro = true,
    ),
    ;

    companion object {
        fun fromStoredIndex(index: Int): BrushWidthOption = entries.getOrElse(index) { Regular }
    }
}

