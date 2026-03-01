/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.core

import org.junit.Assert.assertEquals
import org.junit.Test

class InputMethodOrderTest {

    @Test
    fun `moves pinyin to first while keeping others relative order`() {
        val inputMethods = listOf("keyboard-us", "pinyin", "rime")

        val reordered = InputMethodOrder.prioritize(inputMethods, preferred = "pinyin")

        assertEquals(listOf("pinyin", "keyboard-us", "rime"), reordered)
    }

    @Test
    fun `keeps order when pinyin is already first`() {
        val inputMethods = listOf("pinyin", "keyboard-us", "rime")

        val reordered = InputMethodOrder.prioritize(inputMethods, preferred = "pinyin")

        assertEquals(inputMethods, reordered)
    }

    @Test
    fun `keeps order when preferred input method does not exist`() {
        val inputMethods = listOf("keyboard-us", "rime")

        val reordered = InputMethodOrder.prioritize(inputMethods, preferred = "pinyin")

        assertEquals(inputMethods, reordered)
    }
}
