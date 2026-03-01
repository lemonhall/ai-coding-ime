/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectMetaParserTest {

    private val catalog = setOf("base", "app.android", "engineering.testing")

    @Test
    fun useDictProfilesFirst() {
        val metaJson = """
            {
              "dict_profiles": ["app.android", "engineering.testing", "unknown", "app.android", " "],
              "tags": ["base"]
            }
        """.trimIndent()

        val result = ProjectMetaParser.parseProfiles(metaJson, catalog)

        assertEquals(setOf("app.android", "engineering.testing"), result)
    }

    @Test
    fun fallbackToTagsWhenDictProfilesMissing() {
        val metaJson = """
            {
              "tags": ["engineering.testing", "unknown", " "]
            }
        """.trimIndent()

        val result = ProjectMetaParser.parseProfiles(metaJson, catalog)

        assertEquals(setOf("engineering.testing"), result)
    }

    @Test
    fun fallbackToTagsWhenDictProfilesEmpty() {
        val metaJson = """
            {
              "dict_profiles": [],
              "tags": ["app.android"]
            }
        """.trimIndent()

        val result = ProjectMetaParser.parseProfiles(metaJson, catalog)

        assertEquals(setOf("app.android"), result)
    }

    @Test
    fun ignoreNonStringValues() {
        val metaJson = """
            {
              "dict_profiles": [1, true, null, "app.android"]
            }
        """.trimIndent()

        val result = ProjectMetaParser.parseProfiles(metaJson, catalog)

        assertEquals(setOf("app.android"), result)
    }

    @Test
    fun returnEmptyForInvalidJson() {
        val result = ProjectMetaParser.parseProfiles("{invalid", catalog)

        assertEquals(emptySet<String>(), result)
    }
}
