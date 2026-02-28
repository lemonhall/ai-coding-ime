/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

import org.junit.Assert
import org.junit.Test

class ProfileDictionaryActivationManagerTest {

    @Test
    fun applyShouldEnforceBaseAndToggleByDiff() {
        val base = FakeProfileDictionary(profileId = "base", enabled = false)
        val frontend = FakeProfileDictionary(profileId = "frontend", enabled = true)
        val react = FakeProfileDictionary(profileId = "frontend.react", enabled = false)
        val manager = ProfileDictionaryActivationManager()

        val result = manager.apply(
            dictionaries = listOf(base, frontend, react),
            requestedEnabledProfiles = setOf("frontend.react")
        )

        Assert.assertTrue(result.shouldReload)
        Assert.assertTrue(result.failures.isEmpty())
        Assert.assertEquals(setOf("base", "frontend.react"), result.targetProfiles)
        Assert.assertEquals(setOf("base", "frontend", "frontend.react"), result.changedProfiles)
        Assert.assertTrue(base.isEnabled)
        Assert.assertFalse(frontend.isEnabled)
        Assert.assertTrue(react.isEnabled)
    }

    @Test
    fun noDiffShouldSkipReload() {
        val base = FakeProfileDictionary(profileId = "base", enabled = true)
        val frontend = FakeProfileDictionary(profileId = "frontend", enabled = false)
        val manager = ProfileDictionaryActivationManager()

        val result = manager.apply(
            dictionaries = listOf(base, frontend),
            requestedEnabledProfiles = setOf("base")
        )

        Assert.assertFalse(result.shouldReload)
        Assert.assertTrue(result.changedProfiles.isEmpty())
        Assert.assertEquals(0, base.enableCalls)
        Assert.assertEquals(0, frontend.disableCalls)
    }

    @Test
    fun failureShouldNotBlockOtherChanges() {
        val base = FakeProfileDictionary(profileId = "base", enabled = true)
        val frontend = FakeProfileDictionary(
            profileId = "frontend",
            enabled = true,
            failOnDisable = true
        )
        val react = FakeProfileDictionary(profileId = "frontend.react", enabled = false)
        val manager = ProfileDictionaryActivationManager()

        val result = manager.apply(
            dictionaries = listOf(base, frontend, react),
            requestedEnabledProfiles = setOf("base", "frontend.react")
        )

        Assert.assertTrue(result.shouldReload)
        Assert.assertEquals(setOf("frontend", "frontend.react"), result.changedProfiles)
        Assert.assertEquals(1, result.failures.size)
        Assert.assertEquals("frontend", result.failures.first().profileId)
        Assert.assertTrue(frontend.isEnabled)
        Assert.assertTrue(react.isEnabled)
    }

    private class FakeProfileDictionary(
        override val profileId: String,
        enabled: Boolean,
        private val failOnEnable: Boolean = false,
        private val failOnDisable: Boolean = false
    ) : MutableProfileDictionary {

        override var isEnabled: Boolean = enabled
            private set
        override val fileName: String = "profile.$profileId.dict"
        var enableCalls: Int = 0
            private set
        var disableCalls: Int = 0
            private set

        override fun enable() {
            enableCalls += 1
            if (failOnEnable) throw IllegalStateException("enable failed")
            isEnabled = true
        }

        override fun disable() {
            disableCalls += 1
            if (failOnDisable) throw IllegalStateException("disable failed")
            isEnabled = false
        }
    }
}
