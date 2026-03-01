/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 */
package org.fcitx.fcitx5.android.core

object InputMethodOrder {

    fun prioritize(inputMethods: List<String>, preferred: String): List<String> {
        val preferredIndex = inputMethods.indexOf(preferred)
        if (preferredIndex <= 0) return inputMethods

        return buildList(inputMethods.size) {
            add(preferred)
            inputMethods.forEachIndexed { index, inputMethod ->
                if (index != preferredIndex) {
                    add(inputMethod)
                }
            }
        }
    }
}
