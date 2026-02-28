/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import org.junit.After
import org.junit.Assert
import org.junit.Test

class ProjectDictManagerTest {

    private val dict = """
        getUserInfo\tid\t950
        setConfig\tid\t920
        cfg\tabbr\t800\tconfig/configuration
        数据迁移\tterm\t900\tdatabase migration
        重新部署\tphrase\t700
        回滚到上一个版本\tphrase\t650
    """.trimIndent().replace("\\t", "\t")

    @After
    fun tearDown() {
        ProjectDictNative.setMatcherForTest(null)
        ProjectDictManager.clear()
    }

    @Test
    fun idQueryShouldMatchPinyinSeparatedInput() {
        ProjectDictManager.load(dict, "unit-test")

        val apostropheMatched = ProjectDictManager.query("ge'tu", limit = 5)
        val spaceMatched = ProjectDictManager.query("ge tu", limit = 5)

        Assert.assertTrue(apostropheMatched.any { it.text == "getUserInfo" })
        Assert.assertTrue(spaceMatched.any { it.text == "getUserInfo" })
    }

    @Test
    fun termAndPhraseShouldMatchPinyinPrefix() {
        ProjectDictManager.load(dict, "unit-test")

        val termFull = ProjectDictManager.query("shuju", limit = 5)
        val termInitial = ProjectDictManager.query("sjqy", limit = 5)
        val phrase = ProjectDictManager.query("huigun", limit = 5)

        Assert.assertTrue(termFull.any { it.text == "数据迁移" })
        Assert.assertTrue(termInitial.any { it.text == "数据迁移" })
        Assert.assertTrue(phrase.any { it.text == "回滚到上一个版本" })
    }

    @Test
    fun fuzzyInputShouldRecallPhraseFromNativeMatcher() {
        ProjectDictManager.load(dict, "unit-test")
        ProjectDictNative.setMatcherForTest { input, pinyins ->
            Assert.assertEquals("hyigun", input)
            IntArray(pinyins.size) { idx ->
                if (pinyins[idx].startsWith("hui'gun")) 2 else -1
            }
        }

        val recalled = ProjectDictManager.query("hyigun", limit = 5)

        Assert.assertTrue(recalled.any { it.text == "回滚到上一个版本" })
    }

    @Test
    fun exactMatchShouldRankBeforeFuzzyMatch() {
        ProjectDictManager.load(dict, "unit-test")
        ProjectDictNative.setMatcherForTest { _, pinyins ->
            IntArray(pinyins.size) { idx ->
                if (pinyins[idx].startsWith("shu'ju")) 1 else -1
            }
        }

        val ranked = ProjectDictManager.query("huigun", limit = 5)

        Assert.assertFalse(ranked.isEmpty())
        Assert.assertEquals("回滚到上一个版本", ranked.first().text)
        Assert.assertTrue(ranked.any { it.text == "数据迁移" })
    }

    @Test
    fun nonPinyinInputShouldNotTriggerFuzzyRecall() {
        ProjectDictManager.load(dict, "unit-test")
        var invoked = false
        ProjectDictNative.setMatcherForTest { _, pinyins ->
            invoked = true
            IntArray(pinyins.size) { 0 }
        }

        val results = ProjectDictManager.query("123_", limit = 5)

        Assert.assertFalse(invoked)
        Assert.assertTrue(results.isEmpty())
    }
}
