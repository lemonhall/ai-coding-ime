/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

interface StoredProfileDictionary {
    val profileId: String
    val fileName: String

    fun delete()
}

class ProfileDictionaryReimportManager(
    private val catalog: List<ProfileCatalogEntry> = ProfileDictionaryCatalog.entries
) {

    enum class Stage {
        DELETE,
        IMPORT
    }

    data class ReimportFailure(
        val profileId: String,
        val stage: Stage,
        val reason: String
    )

    data class ReimportResult(
        val deletedProfiles: Set<String>,
        val importedProfiles: Set<String>,
        val failures: List<ReimportFailure>,
        val shouldReload: Boolean
    )

    fun reimport(
        existing: List<StoredProfileDictionary>,
        importer: (ProfileCatalogEntry) -> Unit
    ): ReimportResult {
        val deletedProfiles = mutableSetOf<String>()
        val importedProfiles = mutableSetOf<String>()
        val failures = mutableListOf<ReimportFailure>()

        existing.forEach { dictionary ->
            runCatching {
                dictionary.delete()
                deletedProfiles.add(dictionary.profileId)
            }.onFailure {
                failures.add(
                    ReimportFailure(
                        profileId = dictionary.profileId,
                        stage = Stage.DELETE,
                        reason = it.message ?: it.javaClass.simpleName
                    )
                )
            }
        }

        catalog.forEach { entry ->
            runCatching {
                importer(entry)
                importedProfiles.add(entry.profileId)
            }.onFailure {
                failures.add(
                    ReimportFailure(
                        profileId = entry.profileId,
                        stage = Stage.IMPORT,
                        reason = it.message ?: it.javaClass.simpleName
                    )
                )
            }
        }

        return ReimportResult(
            deletedProfiles = deletedProfiles,
            importedProfiles = importedProfiles,
            failures = failures,
            shouldReload = deletedProfiles.isNotEmpty() || importedProfiles.isNotEmpty()
        )
    }
}
