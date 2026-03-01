/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.input.keyboard

import org.fcitx.fcitx5.android.core.KeyState
import org.fcitx.fcitx5.android.core.KeyStates

class ModifierStateTracker {

    var ctrlEnabled = false
        private set

    var altEnabled = false
        private set

    val hasEnabled: Boolean
        get() = ctrlEnabled || altEnabled

    fun toggle(modifier: KeyAction.Modifier) {
        when (modifier) {
            KeyAction.Modifier.Ctrl -> ctrlEnabled = !ctrlEnabled
            KeyAction.Modifier.Alt -> altEnabled = !altEnabled
        }
    }

    fun reset() {
        ctrlEnabled = false
        altEnabled = false
    }

    fun apply(states: KeyStates): KeyStates {
        var merged = states.states
        if (ctrlEnabled) merged = merged or KeyState.Ctrl.state
        if (altEnabled) merged = merged or KeyState.Alt.state
        return KeyStates(merged)
    }

    fun applyAndConsume(states: KeyStates, clearVirtual: Boolean = false): KeyStates {
        var applied = apply(states)
        if (clearVirtual) {
            applied = KeyStates(applied.states and KeyState.Virtual.state.inv())
        }
        reset()
        return applied
    }
}
