/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AddonConfigDefaultsTest {

    @Test
    fun `sets enabled to false when currently true`() {
        val raw = RawConfig(
            arrayOf(
                RawConfig(
                    "cfg",
                    arrayOf(
                        RawConfig("Enabled", "True"),
                        RawConfig("Hotkey", "Control+period")
                    )
                )
            )
        )

        val changed = AddonConfigDefaults.setBooleanOption(raw, option = "Enabled", value = false)

        assertTrue(changed)
        assertEquals("False", raw["cfg"]["Enabled"].value)
    }

    @Test
    fun `does nothing when cfg is missing`() {
        val raw = RawConfig(arrayOf(RawConfig("desc", arrayOf())))

        val changed = AddonConfigDefaults.setBooleanOption(raw, option = "Enabled", value = false)

        assertFalse(changed)
    }

    @Test
    fun `does nothing when option is missing`() {
        val raw = RawConfig(arrayOf(RawConfig("cfg", arrayOf(RawConfig("Hotkey", "Control+period")))))

        val changed = AddonConfigDefaults.setBooleanOption(raw, option = "Enabled", value = false)

        assertFalse(changed)
    }
}
