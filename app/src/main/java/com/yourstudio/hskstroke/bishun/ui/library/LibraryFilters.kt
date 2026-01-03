package com.yourstudio.hskstroke.bishun.ui.library

import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.normalizePinyin

internal fun matchesLibraryFilterQuery(word: String, entry: WordEntry?, rawQuery: String): Boolean {
    val query = rawQuery.trim()
    if (query.isBlank()) return true
    if (word.contains(query)) return true

    val pinyin = entry?.pinyin?.trim().orEmpty()
    if (pinyin.isBlank()) return false

    val normalizedPinyin = normalizePinyin(pinyin)

    val normalizedQueries = buildList {
        val primary = normalizePinyin(query)
        if (primary.plainCompact.isNotBlank()) add(primary)

        val hasToneDigits = query.any { it in '1'..'5' }
        val hasWhitespace = query.any { it.isWhitespace() }
        if (hasToneDigits && !hasWhitespace) {
            val spaced = query.replace(Regex("([1-5])"), "$1 ")
            val spacedNormalized = normalizePinyin(spaced)
            if (spacedNormalized.plainCompact.isNotBlank() && spacedNormalized != primary) {
                add(spacedNormalized)
            }
        }
    }

    return normalizedQueries.any { normalizedQuery ->
        if (normalizedQuery.hasTone) {
            normalizedPinyin.toneCompact.contains(normalizedQuery.toneCompact)
        } else {
            normalizedPinyin.plainCompact.contains(normalizedQuery.plainCompact)
        }
    }
}
