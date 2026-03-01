/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.core

object AddonConfigDefaults {

    fun setBooleanOption(raw: RawConfig, option: String, value: Boolean): Boolean {
        val cfg = raw.findByName(CFG_NODE) ?: return false
        val target = if (value) TRUE_STRING else FALSE_STRING
        val optionItem = cfg.findByName(option) ?: return false
        if (optionItem.value == target) return false
        optionItem.value = target
        return true
    }

    private const val CFG_NODE = "cfg"
    private const val TRUE_STRING = "True"
    private const val FALSE_STRING = "False"
}
