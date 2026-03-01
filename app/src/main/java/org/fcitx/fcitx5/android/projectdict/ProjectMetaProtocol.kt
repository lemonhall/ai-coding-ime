/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

object ProjectMetaProtocol {
    const val ACTION_APPLY_PROJECT_META = "com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META"
    const val EXTRA_META_JSON = "meta_json"
    const val MAX_META_JSON_LENGTH = 64 * 1024
    const val RATE_LIMIT_WINDOW_MS = 5_000L
}
