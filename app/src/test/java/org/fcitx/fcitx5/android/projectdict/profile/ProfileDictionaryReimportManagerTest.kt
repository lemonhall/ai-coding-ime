/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

import org.junit.Assert
import org.junit.Test

class ProfileDictionaryReimportManagerTest {

    @Test
    fun reimportShouldDeleteExistingThenImportCatalog() {
        val existing = mutableListOf(
            FakeStoredProfileDictionary("base", "profile.base.dict"),
            FakeStoredProfileDictionary("frontend", "profile.frontend.dict.disable")
        )
        val manager = ProfileDictionaryReimportManager(
            catalog = listOf(
                ProfileCatalogEntry("base", true),
                ProfileCatalogEntry("business.crm", false)
            )
        )
        val imported = mutableListOf<String>()

        val result = manager.reimport(
            existing = existing,
            importer = { entry ->
                imported += "${entry.profileId}:${entry.defaultEnabled}"
            }
        )

        Assert.assertEquals(setOf("base", "frontend"), result.deletedProfiles)
        Assert.assertEquals(setOf("base", "business.crm"), result.importedProfiles)
        Assert.assertEquals(listOf("base:true", "business.crm:false"), imported)
        Assert.assertTrue(result.failures.isEmpty())
        Assert.assertTrue(result.shouldReload)
        Assert.assertTrue(existing.all { it.deleted })
    }

    @Test
    fun reimportShouldKeepGoingWhenDeleteOrImportFails() {
        val existing = mutableListOf(
            FakeStoredProfileDictionary("base", "profile.base.dict", failOnDelete = true),
            FakeStoredProfileDictionary("frontend", "profile.frontend.dict")
        )
        val manager = ProfileDictionaryReimportManager(
            catalog = listOf(
                ProfileCatalogEntry("base", true),
                ProfileCatalogEntry("frontend", false)
            )
        )

        val result = manager.reimport(
            existing = existing,
            importer = { entry ->
                if (entry.profileId == "frontend") {
                    throw IllegalStateException("import failed")
                }
            }
        )

        Assert.assertTrue(result.shouldReload)
        Assert.assertEquals(setOf("frontend"), result.deletedProfiles)
        Assert.assertEquals(setOf("base"), result.importedProfiles)
        Assert.assertEquals(2, result.failures.size)
        Assert.assertEquals("base", result.failures[0].profileId)
        Assert.assertEquals(ProfileDictionaryReimportManager.Stage.DELETE, result.failures[0].stage)
        Assert.assertEquals("frontend", result.failures[1].profileId)
        Assert.assertEquals(ProfileDictionaryReimportManager.Stage.IMPORT, result.failures[1].stage)
    }

    private class FakeStoredProfileDictionary(
        override val profileId: String,
        override val fileName: String,
        private val failOnDelete: Boolean = false
    ) : StoredProfileDictionary {

        var deleted: Boolean = false
            private set

        override fun delete() {
            if (failOnDelete) {
                throw IllegalStateException("delete failed")
            }
            deleted = true
        }
    }
}
