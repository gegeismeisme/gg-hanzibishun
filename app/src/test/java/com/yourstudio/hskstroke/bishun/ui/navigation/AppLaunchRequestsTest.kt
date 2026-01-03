package com.yourstudio.hskstroke.bishun.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLaunchRequestsTest {

    @Test
    fun parse_returnsNullWhenMissingExtras() {
        assertNull(AppLaunchRequests.parse(action = null, value = null))
        assertNull(AppLaunchRequests.parse(action = "practice", value = null))
        assertNull(AppLaunchRequests.parse(action = null, value = "永"))
        assertNull(AppLaunchRequests.parse(action = "", value = "永"))
        assertNull(AppLaunchRequests.parse(action = "practice", value = ""))
    }

    @Test
    fun parse_parsesPractice() {
        assertEquals(
            AppLaunchRequest.PracticeSymbol("永"),
            AppLaunchRequests.parse(action = "practice", value = " 永 "),
        )
    }

    @Test
    fun parse_parsesDictionary() {
        assertEquals(
            AppLaunchRequest.DictionaryQuery("好"),
            AppLaunchRequests.parse(action = "dictionary", value = " 好 "),
        )
    }
}
