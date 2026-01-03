package com.yourstudio.hskstroke.bishun.ui.library

import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFiltersTest {

    @Test
    fun matchesLibraryFilterQuery_blankMatches() {
        val hello = "\u4f60\u597d"
        assertTrue(matchesLibraryFilterQuery(hello, null, ""))
        assertTrue(matchesLibraryFilterQuery(hello, null, "   "))
    }

    @Test
    fun matchesLibraryFilterQuery_matchesWordText() {
        val hello = "\u4f60\u597d"
        assertTrue(matchesLibraryFilterQuery(hello, null, "\u4f60"))
        assertFalse(matchesLibraryFilterQuery(hello, null, "\u4ed6"))
    }

    @Test
    fun matchesLibraryFilterQuery_matchesPinyinWithOrWithoutTone() {
        val hello = "\u4f60\u597d"
        val entry = wordEntry(word = hello, pinyin = "n\u01d0 h\u01ceo")

        assertTrue(matchesLibraryFilterQuery(hello, entry, "nihao"))
        assertTrue(matchesLibraryFilterQuery(hello, entry, "ni3hao3"))
        assertTrue(matchesLibraryFilterQuery(hello, entry, "hao"))
        assertTrue(matchesLibraryFilterQuery(hello, entry, "hao3"))

        assertFalse(matchesLibraryFilterQuery(hello, entry, "ni4"))
        assertFalse(matchesLibraryFilterQuery(hello, null, "nihao"))
    }

    @Test
    fun matchesLibraryFilterQuery_supportsUmlautAndScriptG() {
        val umlautWord = "\u7565"
        val umlautEntry = wordEntry(word = umlautWord, pinyin = "l\u00fc\u00e8")
        assertTrue(matchesLibraryFilterQuery(umlautWord, umlautEntry, "l\u00fc"))
        assertTrue(matchesLibraryFilterQuery(umlautWord, umlautEntry, "lv"))
        assertTrue(matchesLibraryFilterQuery(umlautWord, umlautEntry, "lve4"))

        val scriptGWord = "\u4e01"
        val scriptGEntry = wordEntry(word = scriptGWord, pinyin = "d\u012bn\u0261")
        assertTrue(matchesLibraryFilterQuery(scriptGWord, scriptGEntry, "ding"))
        assertTrue(matchesLibraryFilterQuery(scriptGWord, scriptGEntry, "ding1"))
        assertTrue(matchesLibraryFilterQuery(scriptGWord, scriptGEntry, "d\u012bn\u0261"))
    }

    private fun wordEntry(word: String, pinyin: String): WordEntry {
        return WordEntry(
            word = word,
            oldword = "",
            strokes = "",
            pinyin = pinyin,
            radicals = "",
            explanation = "",
            more = "",
        )
    }
}
