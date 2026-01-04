package com.yourstudio.hskstroke.bishun.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentColorOption(
    val label: String,
    val lightPrimary: Color,
    val darkPrimary: Color,
    val requiresPro: Boolean,
) {
    Lilac(
        label = "Lilac",
        lightPrimary = Color(0xFFB39DDB),
        darkPrimary = Color(0xFFD1C4E9),
        requiresPro = false,
    ),
    Sapphire(
        label = "Sapphire Blue",
        lightPrimary = Color(0xFF5A7FF0),
        darkPrimary = Color(0xFF9BB8FF),
        requiresPro = false,
    ),
    Seafoam(
        label = "Seafoam Green",
        lightPrimary = Color(0xFF4CBFA7),
        darkPrimary = Color(0xFF7DE2CB),
        requiresPro = true,
    ),
    SilverPink(
        label = "Silver Pink",
        lightPrimary = Color(0xFFD6B0C0),
        darkPrimary = Color(0xFFE8C4D2),
        requiresPro = true,
    ),
    CookieBrown(
        label = "Cookie Brown",
        lightPrimary = Color(0xFFB88A6A),
        darkPrimary = Color(0xFFD6B39A),
        requiresPro = true,
    ),
    GingerBrown(
        label = "Ginger Brown",
        lightPrimary = Color(0xFFC18457),
        darkPrimary = Color(0xFFE0B38F),
        requiresPro = true,
    ),
    CarnationPink(
        label = "Dark Carnation Pink",
        lightPrimary = Color(0xFFD97AA5),
        darkPrimary = Color(0xFFF0A9C6),
        requiresPro = true,
    ),
    VioletRed(
        label = "Medium Violet Red",
        lightPrimary = Color(0xFFCC6AA3),
        darkPrimary = Color(0xFFE8A6CC),
        requiresPro = true,
    ),
    ;

    fun primary(darkTheme: Boolean): Color = if (darkTheme) darkPrimary else lightPrimary

    companion object {
        fun fromStoredIndex(index: Int): AccentColorOption = entries.getOrElse(index) { Lilac }
    }
}

