/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

interface MutableProfileDictionary {
    val profileId: String
    val fileName: String
    val isEnabled: Boolean

    fun enable()
    fun disable()
}
