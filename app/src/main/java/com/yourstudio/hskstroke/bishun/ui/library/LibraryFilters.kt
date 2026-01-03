package com.yourstudio.hskstroke.bishun.ui.library

import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.normalizePinyin
import com.yourstudio.hskstroke.bishun.data.word.normalizePinyinQueryCandidates

internal fun matchesLibraryFilterQuery(word: String, entry: WordEntry?, rawQuery: String): Boolean {
    val query = rawQuery.trim()
    if (query.isBlank()) return true
    if (word.contains(query)) return true

    val pinyin = entry?.pinyin?.trim().orEmpty()
    if (pinyin.isBlank()) return false

    val normalizedPinyin = normalizePinyin(pinyin)
    val normalizedQueries = normalizePinyinQueryCandidates(query)

    return normalizedQueries.any { normalizedQuery ->
        if (normalizedQuery.hasTone) {
            normalizedPinyin.toneCompact.contains(normalizedQuery.toneCompact)
        } else {
            normalizedPinyin.plainCompact.contains(normalizedQuery.plainCompact)
        }
    }
}
