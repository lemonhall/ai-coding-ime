/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard

import org.fcitx.fcitx5.android.core.FcitxKeyMapping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextKeyboardLayoutTest {

    @Test
    fun `puts tab key to the left of a on second row`() {
        val secondRow = TextKeyboard.Layout[1]

        assertEquals(10, secondRow.size)

        val tabAction = secondRow.first()
            .behaviors
            .filterIsInstance<KeyDef.Behavior.Press>()
            .single()
            .action as? KeyAction.SymAction

        assertNotNull(tabAction)
        assertEquals(FcitxKeyMapping.FcitxKey_Tab, tabAction?.sym?.sym)

        val letters = secondRow.drop(1).map {
            assertTrue(it is AlphabetKey)
            (it as AlphabetKey).character
        }
        assertEquals(listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"), letters)
    }

    @Test
    fun `adds bottom shortcut row with esc ctl alt keys`() {
        assertEquals(5, TextKeyboard.Layout.size)
        val bottomRow = TextKeyboard.Layout.last()
        assertEquals(3, bottomRow.size)

        val escAction = bottomRow[0]
            .behaviors
            .filterIsInstance<KeyDef.Behavior.Press>()
            .single()
            .action as? KeyAction.SymAction
        assertNotNull(escAction)
        assertEquals(FcitxKeyMapping.FcitxKey_Escape, escAction?.sym?.sym)

        val ctrlAction = bottomRow[1]
            .behaviors
            .filterIsInstance<KeyDef.Behavior.Press>()
            .single()
            .action as? KeyAction.ModifierToggleAction
        assertNotNull(ctrlAction)
        assertEquals(KeyAction.Modifier.Ctrl, ctrlAction?.modifier)

        val altAction = bottomRow[2]
            .behaviors
            .filterIsInstance<KeyDef.Behavior.Press>()
            .single()
            .action as? KeyAction.ModifierToggleAction
        assertNotNull(altAction)
        assertEquals(KeyAction.Modifier.Alt, altAction?.modifier)
    }
}
