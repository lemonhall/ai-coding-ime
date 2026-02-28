/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

class ProfileDictionaryActivationManager(
    private val baseProfileId: String = BASE_PROFILE_ID
) {

    data class ActivationFailure(
        val profileId: String,
        val fileName: String,
        val reason: String
    )

    data class ApplyResult(
        val targetProfiles: Set<String>,
        val activeProfiles: Set<String>,
        val changedProfiles: Set<String>,
        val failures: List<ActivationFailure>,
        val shouldReload: Boolean
    )

    fun apply(
        dictionaries: List<MutableProfileDictionary>,
        requestedEnabledProfiles: Set<String>
    ): ApplyResult {
        if (dictionaries.isEmpty()) {
            return ApplyResult(
                targetProfiles = emptySet(),
                activeProfiles = emptySet(),
                changedProfiles = emptySet(),
                failures = emptyList(),
                shouldReload = false
            )
        }

        val availableProfiles = dictionaries.map { it.profileId }.toSet()
        val targetProfiles = requestedEnabledProfiles
            .intersect(availableProfiles)
            .toMutableSet()
            .apply {
                if (availableProfiles.contains(baseProfileId)) {
                    add(baseProfileId)
                }
            }
            .toSet()

        val changedProfiles = mutableSetOf<String>()
        val failures = mutableListOf<ActivationFailure>()
        var hasPlannedChanges = false

        dictionaries.forEach { dictionary ->
            val shouldEnable = targetProfiles.contains(dictionary.profileId)
            if (dictionary.isEnabled == shouldEnable) {
                return@forEach
            }
            hasPlannedChanges = true
            changedProfiles.add(dictionary.profileId)
            runCatching {
                if (shouldEnable) {
                    dictionary.enable()
                } else {
                    dictionary.disable()
                }
            }.onFailure {
                failures.add(
                    ActivationFailure(
                        profileId = dictionary.profileId,
                        fileName = dictionary.fileName,
                        reason = it.message ?: it.javaClass.simpleName
                    )
                )
            }
        }

        return ApplyResult(
            targetProfiles = targetProfiles,
            activeProfiles = dictionaries.filter { it.isEnabled }.map { it.profileId }.toSet(),
            changedProfiles = changedProfiles,
            failures = failures,
            shouldReload = hasPlannedChanges
        )
    }

    companion object {
        const val BASE_PROFILE_ID = "base"
    }
}
