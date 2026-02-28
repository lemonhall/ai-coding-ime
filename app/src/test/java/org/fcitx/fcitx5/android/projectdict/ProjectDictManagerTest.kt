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
}
