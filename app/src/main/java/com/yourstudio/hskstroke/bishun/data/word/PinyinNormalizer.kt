package com.yourstudio.hskstroke.bishun.data.word

internal data class NormalizedPinyin(
    val plainCompact: String,
    val toneCompact: String,
    val hasTone: Boolean,
)

internal fun normalizePinyin(raw: String): NormalizedPinyin {
    val parts = raw.trim()
        .lowercase()
        .replace(',', ' ')
        .split("\\s+".toRegex())
        .filter { it.isNotBlank() }
    if (parts.isEmpty()) return NormalizedPinyin(plainCompact = "", toneCompact = "", hasTone = false)

    val plain = StringBuilder()
    val tone = StringBuilder()
    var anyTone = false
    parts.forEach { part ->
        val (syllablePlain, syllableTone) = normalizePinyinSyllable(part)
        if (syllablePlain.isNotBlank()) {
            plain.append(syllablePlain)
            tone.append(syllableTone)
            if (syllableTone.any { it in '1'..'5' }) anyTone = true
        }
    }
    return NormalizedPinyin(
        plainCompact = plain.toString(),
        toneCompact = tone.toString(),
        hasTone = anyTone,
    )
}

internal fun normalizePinyinQueryCandidates(raw: String): List<NormalizedPinyin> {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return emptyList()

    val candidates = mutableListOf<NormalizedPinyin>()
    val primary = normalizePinyin(trimmed)
    if (primary.plainCompact.isNotBlank()) candidates.add(primary)

    val hasToneDigits = trimmed.any { it in '1'..'5' }
    val hasWhitespace = trimmed.any { it.isWhitespace() }
    if (hasToneDigits && !hasWhitespace) {
        val spaced = trimmed.replace(Regex("([1-5])"), "$1 ")
        val spacedNormalized = normalizePinyin(spaced)
        if (spacedNormalized.plainCompact.isNotBlank()) candidates.add(spacedNormalized)
    }

    return candidates.distinct()
}

private fun normalizePinyinSyllable(raw: String): Pair<String, String> {
    var tone = 0
    val builder = StringBuilder()
    raw.lowercase().forEach { ch ->
        when {
            ch in '1'..'5' -> {
                tone = ch.digitToInt()
            }

            ch == ':' -> {
                val last = builder.lastOrNull()
                if (last == 'u') {
                    builder.setCharAt(builder.length - 1, 'v')
                }
            }

            else -> {
                val mapped = mapPinyinChar(ch)
                if (mapped.tone != 0) tone = mapped.tone
                if (mapped.base != null) builder.append(mapped.base)
            }
        }
    }
    val plain = builder.toString()
    val toneSuffix = if (tone in 1..5) tone.toString() else ""
    return plain to (plain + toneSuffix)
}

private data class PinyinChar(val base: Char?, val tone: Int)

private fun mapPinyinChar(ch: Char): PinyinChar {
    return when (ch) {
        in 'a'..'z' -> PinyinChar(ch, 0)
        '\u0261' -> PinyinChar('g', 0)
        'ü', 'Ü' -> PinyinChar('v', 0)

        'ā' -> PinyinChar('a', 1)
        'á' -> PinyinChar('a', 2)
        'ǎ' -> PinyinChar('a', 3)
        'à' -> PinyinChar('a', 4)
        'ē' -> PinyinChar('e', 1)
        'é' -> PinyinChar('e', 2)
        'ě' -> PinyinChar('e', 3)
        'è' -> PinyinChar('e', 4)
        'ī' -> PinyinChar('i', 1)
        'í' -> PinyinChar('i', 2)
        'ǐ' -> PinyinChar('i', 3)
        'ì' -> PinyinChar('i', 4)
        'ō' -> PinyinChar('o', 1)
        'ó' -> PinyinChar('o', 2)
        'ǒ' -> PinyinChar('o', 3)
        'ò' -> PinyinChar('o', 4)
        'ū' -> PinyinChar('u', 1)
        'ú' -> PinyinChar('u', 2)
        'ǔ' -> PinyinChar('u', 3)
        'ù' -> PinyinChar('u', 4)
        'ǖ' -> PinyinChar('v', 1)
        'ǘ' -> PinyinChar('v', 2)
        'ǚ' -> PinyinChar('v', 3)
        'ǜ' -> PinyinChar('v', 4)

        else -> PinyinChar(null, 0)
    }
}
