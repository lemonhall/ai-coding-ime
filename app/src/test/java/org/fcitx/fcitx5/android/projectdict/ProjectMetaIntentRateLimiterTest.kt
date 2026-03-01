/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectMetaIntentRateLimiterTest {

    @Test
    fun allowFirstRequest() {
        val limiter = ProjectMetaIntentRateLimiter(windowMs = 5_000L)

        assertTrue(limiter.tryAcquire(nowMs = 10_000L))
    }

    @Test
    fun rejectWithinWindow() {
        val limiter = ProjectMetaIntentRateLimiter(windowMs = 5_000L)
        assertTrue(limiter.tryAcquire(nowMs = 10_000L))

        assertFalse(limiter.tryAcquire(nowMs = 14_999L))
    }

    @Test
    fun allowAfterWindowExpires() {
        val limiter = ProjectMetaIntentRateLimiter(windowMs = 5_000L)
        assertTrue(limiter.tryAcquire(nowMs = 10_000L))
        assertFalse(limiter.tryAcquire(nowMs = 12_000L))

        assertTrue(limiter.tryAcquire(nowMs = 15_000L))
    }
}
