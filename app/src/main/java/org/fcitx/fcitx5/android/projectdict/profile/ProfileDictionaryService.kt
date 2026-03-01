/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict.profile

import android.content.res.AssetManager
import org.fcitx.fcitx5.android.data.pinyin.PinyinDictManager
import org.fcitx.fcitx5.android.data.pinyin.dict.LibIMEDictionary
import timber.log.Timber

data class ProfileCatalogEntry(
    val profileId: String,
    val defaultEnabled: Boolean
)

data class ProfileDictionaryState(
    val profileId: String,
    val fileName: String,
    val isEnabled: Boolean
)

data class ProfileBootstrapFailure(
    val profileId: String,
    val reason: String
)

object ProfileDictionaryCatalog {
    val entries: List<ProfileCatalogEntry> = listOf(
        ProfileCatalogEntry(profileId = "base", defaultEnabled = true),
        ProfileCatalogEntry(profileId = "frontend", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "frontend.react", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "network.web-backend-api", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.devops-sre", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.testing", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.package-build", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.vcs-collaboration", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.software-architecture", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "data.relational-db", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "backend.java", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "backend.go", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "backend.rust", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "app.android", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "client.desktop-cross-platform", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "client.game-dev", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "client.graphics-rendering", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.editor-ide-tooling", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.blockchain-web3", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.audio-video-streaming", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.gis", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.robotics-ros", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.data-visualization", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "domain.lowcode-dsl-config", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "infra.cloud-iaas", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "infra.containers-orchestration", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "infra.mq-event-streaming", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "infra.observability-monitoring", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "infra.iac", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "hardware.cpu-architecture", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "hardware.embedded-iot", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "hardware.os-kernel", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "hardware.driver-hal", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "hardware.fpga-hdl", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "network.protocols", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "network.security-crypto", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "network.distributed-systems", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "data.nosql-newsql", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "data.engineering-etl", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "data.search-ir", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.compiler-interpreter", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "engineering.plt", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.classic-ml", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.deep-learning", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.nlp-llm", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.computer-vision", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.recommender-systems", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.mlops", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "ai.reinforcement-learning", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.scientific-computing", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.physics-simulation-fem", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.signal-processing-dsp", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.computational-geometry-cad", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.bioinformatics", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "science.quantum-computing", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.crm", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.erp", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.finance-payments", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.hrm", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.supply-chain-logistics-wms", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.ecommerce-platform", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "business.oa-workflow", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "product.management", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "product.growth-operations", defaultEnabled = false),
        ProfileCatalogEntry(profileId = "product.adtech-martech", defaultEnabled = false)
    )

    fun assetFileName(profileId: String): String = "$PROFILE_PREFIX.$profileId.txt"

    fun parseProfileId(dictName: String): String? {
        if (!dictName.startsWith("$PROFILE_PREFIX.")) {
            return null
        }
        val id = dictName.removePrefix("$PROFILE_PREFIX.")
        return id.takeIf { it.isNotBlank() }
    }

    private const val PROFILE_PREFIX = "profile"
}

object ProfileDictionaryService {

    private val activationManager = ProfileDictionaryActivationManager()
    private val reimportManager = ProfileDictionaryReimportManager()
    private val catalogOrder = ProfileDictionaryCatalog.entries
        .mapIndexed { index, entry -> entry.profileId to index }
        .toMap()

    fun ensureBundledProfilesReady(assetManager: AssetManager): List<ProfileBootstrapFailure> {
        val failures = mutableListOf<ProfileBootstrapFailure>()
        val existingProfiles = listStates().map { it.profileId }.toMutableSet()

        ProfileDictionaryCatalog.entries.forEach { entry ->
            if (existingProfiles.contains(entry.profileId)) {
                return@forEach
            }
            runCatching {
                importProfileFromAsset(assetManager, entry)
                existingProfiles.add(entry.profileId)
                Timber.i("ProjectDictProfile: Bootstrapped profile '%s'", entry.profileId)
            }.onFailure {
                val message = it.message ?: it.javaClass.simpleName
                failures.add(ProfileBootstrapFailure(entry.profileId, message))
                Timber.e(it, "ProjectDictProfile: Failed to bootstrap profile '%s'", entry.profileId)
            }
        }
        return failures
    }

    fun listStates(): List<ProfileDictionaryState> {
        return listMutableDictionaries()
            .map {
                ProfileDictionaryState(
                    profileId = it.profileId,
                    fileName = it.fileName,
                    isEnabled = it.isEnabled
                )
            }
            .sortedWith(
                compareBy<ProfileDictionaryState> { catalogOrder[it.profileId] ?: Int.MAX_VALUE }
                    .thenBy { it.profileId }
            )
    }

    fun applySelection(requestedEnabledProfiles: Set<String>): ProfileDictionaryActivationManager.ApplyResult {
        return activationManager.apply(
            dictionaries = listMutableDictionaries(),
            requestedEnabledProfiles = requestedEnabledProfiles
        )
    }

    fun forceReimportBundledProfiles(assetManager: AssetManager): ProfileDictionaryReimportManager.ReimportResult {
        return reimportManager.reimport(
            existing = listStoredDictionaries(),
            importer = { entry ->
                importProfileFromAsset(assetManager, entry)
            }
        )
    }

    private fun listMutableDictionaries(): List<MutableProfileDictionary> {
        return PinyinDictManager.listDictionaries()
            .mapNotNull { it as? LibIMEDictionary }
            .mapNotNull { dict ->
                ProfileDictionaryCatalog.parseProfileId(dict.name)?.let { profileId ->
                    LibIMEProfileDictionaryAdapter(profileId = profileId, delegate = dict)
                }
            }
    }

    private fun listStoredDictionaries(): List<StoredProfileDictionary> {
        return PinyinDictManager.listDictionaries()
            .mapNotNull { it as? LibIMEDictionary }
            .mapNotNull { dict ->
                ProfileDictionaryCatalog.parseProfileId(dict.name)?.let { profileId ->
                    LibIMEStoredProfileDictionary(profileId = profileId, delegate = dict)
                }
            }
    }

    private fun importProfileFromAsset(assetManager: AssetManager, entry: ProfileCatalogEntry) {
        val assetPath = "$ASSET_PROFILE_DIR/${ProfileDictionaryCatalog.assetFileName(entry.profileId)}"
        assetManager.open(assetPath).use { input ->
            val imported = PinyinDictManager.importFromInputStream(
                input,
                ProfileDictionaryCatalog.assetFileName(entry.profileId)
            ).getOrThrow()
            if (!entry.defaultEnabled) {
                imported.disable()
            }
        }
        Timber.i("ProjectDictProfile: Imported profile '%s' from assets", entry.profileId)
    }

    private const val ASSET_PROFILE_DIR = "projectdict/profile-dictionaries"
}

private class LibIMEProfileDictionaryAdapter(
    override val profileId: String,
    private val delegate: LibIMEDictionary
) : MutableProfileDictionary {

    override val fileName: String
        get() = delegate.file.name

    override val isEnabled: Boolean
        get() = delegate.isEnabled

    override fun enable() {
        delegate.enable()
    }

    override fun disable() {
        delegate.disable()
    }
}

private class LibIMEStoredProfileDictionary(
    override val profileId: String,
    private val delegate: LibIMEDictionary
) : StoredProfileDictionary {

    override val fileName: String
        get() = delegate.file.name

    override fun delete() {
        if (!delegate.file.delete()) {
            throw IllegalStateException("Failed to delete ${delegate.file.absolutePath}")
        }
    }
}
