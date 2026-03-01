/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryCatalog
import timber.log.Timber

object ProjectMetaParser {

    private val catalogProfiles: Set<String> = ProfileDictionaryCatalog.entries
        .map { it.profileId }
        .toSet()

    fun parseProfiles(
        metaJson: String,
        availableProfiles: Set<String> = catalogProfiles
    ): Set<String> {
        val root = runCatching { Json.parseToJsonElement(metaJson) }
            .onFailure { Timber.w(it, "ProjectMeta: Failed to parse meta_json") }
            .getOrNull() as? JsonObject ?: return emptySet()

        val dictProfiles = root.readStringList("dict_profiles")
        val source = if (dictProfiles.isNotEmpty()) {
            dictProfiles
        } else {
            root.readStringList("tags")
        }

        return source.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .filter { availableProfiles.contains(it) }
            .toSet()
    }

    private fun JsonObject.readStringList(key: String): List<String> {
        val values = this[key] as? JsonArray ?: return emptyList()
        return values.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
    }
}
