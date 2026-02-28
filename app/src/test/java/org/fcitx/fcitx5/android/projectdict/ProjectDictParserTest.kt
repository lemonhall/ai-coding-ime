/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import org.junit.Assert
import org.junit.Test

class ProjectDictParserTest {

    @Test
    fun parseTabSeparatedEntries() {
        val content = """
            getUserInfo\tid\t950
            cfg\tabbr\t600\tconfig/configuration
        """.trimIndent().replace("\\t", "\t")

        val entries = ProjectDictParser.parse(content)

        Assert.assertEquals(2, entries.size)
        Assert.assertEquals("getUserInfo", entries[0].text)
        Assert.assertEquals(ProjectDictEntry.EntryType.ID, entries[0].type)
        Assert.assertEquals(950, entries[0].weight)
    }

    @Test
    fun parseWhitespaceSeparatedEntries() {
        val content = """
            getUserInfo id 950
            cfg abbr 600 config/configuration
        """.trimIndent()

        val entries = ProjectDictParser.parse(content)

        Assert.assertEquals(2, entries.size)
        Assert.assertEquals("getUserInfo", entries[0].text)
        Assert.assertEquals(ProjectDictEntry.EntryType.ID, entries[0].type)
        Assert.assertEquals(950, entries[0].weight)

        Assert.assertEquals("cfg", entries[1].text)
        Assert.assertEquals(ProjectDictEntry.EntryType.ABBR, entries[1].type)
        Assert.assertEquals(600, entries[1].weight)
        Assert.assertEquals("config/configuration", entries[1].hint)
    }
}
