package com.example.bishun.hanzi.render

import java.util.Locale

object ColorParser {
    private val hexRegex = Regex("^#([A-F0-9]{3}){1,2}$", RegexOption.IGNORE_CASE)
    private val rgbaRegex =
        Regex("^RGBA?\\((\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)(?:\\s*,\\s*(\\d*\\.?\\d+))?\\)$")

    fun parse(input: String): ColorRgba {
        val normalized = input.trim().uppercase(Locale.ROOT)
        if (hexRegex.matches(normalized)) {
            return parseHex(normalized)
        }
        val rgbaMatch = rgbaRegex.find(normalized)
        if (rgbaMatch != null) {
            val (r, g, b, alpha) = rgbaMatch.destructured
            return ColorRgba(
                r = r.toInt(),
                g = g.toInt(),
                b = b.toInt(),
                a = alpha.toFloatOrNull() ?: 1f,
            )
        }
        throw IllegalArgumentException("Unsupported color format: $input")
    }

    private fun parseHex(color: String): ColorRgba {
        var hex = color.removePrefix("#")
        if (hex.length == 3) {
            hex = buildString {
                hex.forEach { append(it).append(it) }
            }
        }
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        return ColorRgba(r, g, b)
    }
}
