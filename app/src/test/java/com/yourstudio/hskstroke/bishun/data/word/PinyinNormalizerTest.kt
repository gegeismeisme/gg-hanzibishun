package com.yourstudio.hskstroke.bishun.data.word

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PinyinNormalizerTest {

    @Test
    fun normalizePinyin_handlesDiacritics() {
        val normalized = normalizePinyin("yǒng")
        assertEquals("yong", normalized.plainCompact)
        assertEquals("yong3", normalized.toneCompact)
        assertTrue(normalized.hasTone)
    }

    @Test
    fun normalizePinyin_handlesToneDigits() {
        val normalized = normalizePinyin("han4 zi4")
        assertEquals("hanzi", normalized.plainCompact)
        assertEquals("han4zi4", normalized.toneCompact)
        assertTrue(normalized.hasTone)
    }

    @Test
    fun normalizePinyin_handlesUmlaut() {
        val normalized = normalizePinyin("lüè")
        assertEquals("lve", normalized.plainCompact)
        assertEquals("lve4", normalized.toneCompact)
        assertTrue(normalized.hasTone)
    }

    @Test
    fun normalizePinyin_handlesColonUmlaut() {
        val normalized = normalizePinyin("lu:4")
        assertEquals("lv", normalized.plainCompact)
        assertEquals("lv4", normalized.toneCompact)
        assertTrue(normalized.hasTone)
    }

    @Test
    fun normalizePinyin_handlesScriptG() {
        val normalized = normalizePinyin("dīnɡ")
        assertEquals("ding", normalized.plainCompact)
        assertEquals("ding1", normalized.toneCompact)
        assertTrue(normalized.hasTone)
    }

    @Test
    fun normalizePinyin_empty_isSafe() {
        val normalized = normalizePinyin("   ")
        assertEquals("", normalized.plainCompact)
        assertEquals("", normalized.toneCompact)
        assertFalse(normalized.hasTone)
    }
}
