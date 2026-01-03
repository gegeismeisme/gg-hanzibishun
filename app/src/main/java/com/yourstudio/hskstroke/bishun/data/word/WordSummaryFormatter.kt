package com.yourstudio.hskstroke.bishun.data.word

private val whitespaceRegex = Regex("\\s+")
private val leadingNumberingRegex = Regex("^\\s*[0-9一二三四五六七八九十]+[\\.、\\)]?\\s*")

fun buildExplanationSummary(explanation: String, maxChars: Int = 48): String? {
    val trimmed = explanation.trim()
    if (trimmed.isBlank()) return null

    val normalized = trimmed
        .replace('\n', ' ')
        .replace('\r', ' ')
        .replace(whitespaceRegex, " ")
        .trim()
    if (normalized.isBlank()) return null

    val firstSegment = normalized
        .split('；', ';', '。')
        .firstOrNull()
        ?.trim()
        .orEmpty()
    val withoutLeadingNumber = firstSegment.replace(leadingNumberingRegex, "").trim()
    val candidate = withoutLeadingNumber.ifBlank { firstSegment }.trim()
    if (candidate.isBlank()) return null

    val limit = maxChars.coerceAtLeast(4)
    if (candidate.length <= limit) return candidate
    return candidate.take(limit - 1).trimEnd() + "…"
}

