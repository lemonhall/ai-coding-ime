/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

class ProjectMetaIntentRateLimiter(
    private val windowMs: Long = ProjectMetaProtocol.RATE_LIMIT_WINDOW_MS,
    initialAcceptedAtMs: Long = -windowMs
) {
    private val lastAcceptedAtMs = AtomicLong(initialAcceptedAtMs)

    init {
        require(windowMs > 0) { "windowMs must be > 0" }
    }

    fun tryAcquire(nowMs: Long = SystemClock.elapsedRealtime()): Boolean {
        while (true) {
            val last = lastAcceptedAtMs.get()
            if (nowMs - last < windowMs) {
                return false
            }
            if (lastAcceptedAtMs.compareAndSet(last, nowMs)) {
                return true
            }
        }
    }
}
