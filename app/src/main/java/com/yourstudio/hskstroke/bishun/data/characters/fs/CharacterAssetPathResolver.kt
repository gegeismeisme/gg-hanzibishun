package com.example.bishun.data.characters.fs

import java.util.Locale

/**
 * Resolves asset filenames for a provided Hanzi. We support both code-point based names
 * (`u6c38.json`) and literal names (`永.json`) so assets can be stored flexibly.
 */
class CharacterAssetPathResolver(
    private val directory: String = DEFAULT_DIRECTORY,
) {

    fun assetCandidatesFor(input: String): List<String> {
        val normalized = input.trim()
        if (normalized.isEmpty()) return emptyList()

        val primaryCodePoint = Character.codePointAt(normalized, 0)
        val literal = String(Character.toChars(primaryCodePoint))
        val hexCode = primaryCodePoint.toString(16).lowercase(Locale.ROOT)

        val candidates = listOf(
            "$directory/u$hexCode.json",
            "$directory/${literal}.json",
        )

        return candidates.distinct()
    }

    companion object {
        private const val DEFAULT_DIRECTORY = "characters"
    }
}
