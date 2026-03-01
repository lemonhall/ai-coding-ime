/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard

import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModifierStateTrackerTest {

    @Test
    fun `toggles ctrl and alt states and applies to key states`() {
        val tracker = ModifierStateTracker()

        assertFalse(tracker.ctrlEnabled)
        assertFalse(tracker.altEnabled)

        tracker.toggle(KeyAction.Modifier.Ctrl)
        val withCtrl = tracker.apply(KeyStates.Virtual)
        assertTrue(tracker.ctrlEnabled)
        assertTrue(withCtrl.ctrl)
        assertFalse(withCtrl.alt)
        assertTrue(withCtrl.virtual)

        tracker.toggle(KeyAction.Modifier.Alt)
        val withCtrlAlt = tracker.apply(KeyStates.Virtual)
        assertTrue(tracker.ctrlEnabled)
        assertTrue(tracker.altEnabled)
        assertTrue(withCtrlAlt.ctrl)
        assertTrue(withCtrlAlt.alt)
        assertTrue(withCtrlAlt.virtual)

        tracker.toggle(KeyAction.Modifier.Ctrl)
        val withAltOnly = tracker.apply(KeyStates.Virtual)
        assertFalse(tracker.ctrlEnabled)
        assertTrue(tracker.altEnabled)
        assertFalse(withAltOnly.ctrl)
        assertTrue(withAltOnly.alt)
        assertTrue(withAltOnly.virtual)
    }

    @Test
    fun `reset clears both modifiers`() {
        val tracker = ModifierStateTracker()

        tracker.toggle(KeyAction.Modifier.Ctrl)
        tracker.toggle(KeyAction.Modifier.Alt)
        tracker.reset()

        val plain = tracker.apply(KeyStates(KeyState.Virtual))
        assertFalse(tracker.ctrlEnabled)
        assertFalse(tracker.altEnabled)
        assertFalse(plain.ctrl)
        assertFalse(plain.alt)
        assertTrue(plain.virtual)
    }

    @Test
    fun `apply and consume clears active modifiers after one key`() {
        val tracker = ModifierStateTracker()

        tracker.toggle(KeyAction.Modifier.Ctrl)
        val first = tracker.applyAndConsume(KeyStates.Virtual)
        assertTrue(first.ctrl)
        assertFalse(first.alt)
        assertTrue(first.virtual)
        assertFalse(tracker.ctrlEnabled)
        assertFalse(tracker.altEnabled)

        tracker.toggle(KeyAction.Modifier.Ctrl)
        tracker.toggle(KeyAction.Modifier.Alt)
        val second = tracker.applyAndConsume(KeyStates.Virtual)
        assertTrue(second.ctrl)
        assertTrue(second.alt)
        assertTrue(second.virtual)
        assertFalse(tracker.ctrlEnabled)
        assertFalse(tracker.altEnabled)
    }

    @Test
    fun `apply and consume can clear virtual bit for shortcut key events`() {
        val tracker = ModifierStateTracker()

        tracker.toggle(KeyAction.Modifier.Ctrl)
        val physical = tracker.applyAndConsume(KeyStates.Virtual, clearVirtual = true)

        assertTrue(physical.ctrl)
        assertFalse(physical.virtual)
        assertFalse(tracker.ctrlEnabled)
    }
}
