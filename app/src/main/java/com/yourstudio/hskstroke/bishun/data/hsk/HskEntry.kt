package com.yourstudio.hskstroke.bishun.data.hsk

data class HskEntry(
    val symbol: String,
    val level: Int,
    val writingLevel: Int?,
    val traditional: String,
    val frequency: Int?,
    val examples: String,
) {
    companion object {
        fun fromCsvRow(columns: List<String>): HskEntry? {
            if (columns.size < 2) return null
            val symbol = columns[0].trim()
            if (symbol.isEmpty()) return null
            val level = columns.getOrNull(1)?.trim()?.toIntOrNull() ?: return null
            val writingLevel = columns.getOrNull(2)?.trim()?.toIntOrNull()
            val traditional = columns.getOrNull(3)?.trim().orEmpty()
            val freq = columns.getOrNull(4)?.trim()?.toIntOrNull()
            val examples = if (columns.size > 5) {
                columns.subList(5, columns.size).joinToString(separator = ",").trim()
            } else {
                ""
            }
            return HskEntry(
                symbol = symbol,
                level = level,
                writingLevel = writingLevel,
                traditional = traditional,
                frequency = freq,
                examples = examples,
            )
        }
    }
}
