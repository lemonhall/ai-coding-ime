/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.reloadPinyinDict
import org.fcitx.fcitx5.android.projectdict.profile.ProfileBootstrapFailure
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryActivationManager
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryReimportManager
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryService
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryState
import org.fcitx.fcitx5.android.ui.common.PaddingPreferenceFragment
import org.fcitx.fcitx5.android.ui.main.modified.MySwitchPreference
import org.fcitx.fcitx5.android.utils.addCategory
import org.fcitx.fcitx5.android.utils.setup
import timber.log.Timber

class ProjectDictProfilesFragment : PaddingPreferenceFragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val switches = linkedMapOf<String, MySwitchPreference>()
    private var bootstrapFailures: List<ProfileBootstrapFailure> = emptyList()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        renderLoading()
        loadProfiles()
    }

    private fun renderLoading() {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            addPreference(Preference(context).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_title),
                    summary = getString(R.string.please_wait)
                )
            })
        }
    }

    private fun loadProfiles() {
        val assets = requireContext().assets
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    viewModel.fcitx.runOnReady { }
                    val failures = ProfileDictionaryService.ensureBundledProfilesReady(assets)
                    val states = ProfileDictionaryService.listStates()
                    LoadResult(states = states, bootstrapFailures = failures)
                }.getOrElse {
                    Timber.e(it, "ProjectDictProfile: Failed to load profiles")
                    LoadResult(
                        states = emptyList(),
                        bootstrapFailures = listOf(
                            ProfileBootstrapFailure(
                                profileId = "runtime",
                                reason = it.message ?: it.javaClass.simpleName
                            )
                        )
                    )
                }
            }
            bootstrapFailures = result.bootstrapFailures
            renderProfiles(result.states)
        }
    }

    private fun renderProfiles(states: List<ProfileDictionaryState>) {
        switches.clear()
        val context = requireContext()
        val hasProfiles = states.isNotEmpty()
        val activeProfiles = states.filter { it.isEnabled }.map { it.profileId }

        preferenceScreen = preferenceManager.createPreferenceScreen(context).apply {
            addPreference(Preference(context).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_overview),
                    summary = buildOverviewSummary(states, activeProfiles)
                )
            })

            addPreference(Preference(context).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_keep_base_only),
                    summary = getString(R.string.project_dict_profiles_keep_base_only_summary)
                )
                isEnabled = hasProfiles
                setOnPreferenceClickListener {
                    keepBaseOnly()
                    true
                }
            })

            addPreference(Preference(context).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_apply_reload),
                    summary = getString(R.string.project_dict_profiles_apply_reload_summary)
                )
                isEnabled = hasProfiles
                setOnPreferenceClickListener {
                    applyAndReload()
                    true
                }
            })

            addPreference(Preference(context).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_reimport_builtin),
                    summary = getString(R.string.project_dict_profiles_reimport_builtin_summary)
                )
                setOnPreferenceClickListener {
                    reimportBundledProfiles()
                    true
                }
            })

            addCategory(R.string.project_dict_profiles_list) {
                fillProfileList(this, states)
            }

            if (bootstrapFailures.isNotEmpty()) {
                addPreference(Preference(context).apply {
                    setup(
                        title = getString(R.string.project_dict_profiles_bootstrap_failed_title),
                        summary = buildBootstrapFailureSummary(bootstrapFailures)
                    )
                })
            }
        }
    }

    private fun fillProfileList(category: PreferenceCategory, states: List<ProfileDictionaryState>) {
        if (states.isEmpty()) {
            category.addPreference(Preference(requireContext()).apply {
                setup(
                    title = getString(R.string.project_dict_profiles_empty_title),
                    summary = getString(R.string.project_dict_profiles_empty_summary)
                )
            })
            return
        }
        states.forEach { state ->
            category.addPreference(MySwitchPreference(requireContext()).apply {
                key = "project_dict_profile_${state.profileId}"
                title = state.profileId
                summary = state.fileName
                isChecked = state.isEnabled
            }.also {
                switches[state.profileId] = it
            })
        }
    }

    private fun keepBaseOnly() {
        switches.forEach { (profileId, pref) ->
            pref.isChecked = profileId == ProfileDictionaryActivationManager.BASE_PROFILE_ID
        }
    }

    private fun applyAndReload() {
        val selectedProfiles = switches
            .filterValues { it.isChecked }
            .map { it.key }
            .toSet()

        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                val activation = ProfileDictionaryService.applySelection(selectedProfiles)
                var reloadError: String? = null
                if (activation.shouldReload) {
                    runCatching {
                        viewModel.fcitx.runOnReady {
                            reloadPinyinDict()
                        }
                    }.onFailure {
                        reloadError = it.message ?: it.javaClass.simpleName
                        Timber.e(it, "ProjectDictProfile: Reload failed")
                    }
                }
                val states = ProfileDictionaryService.listStates()
                val durationMs = System.currentTimeMillis() - startTime
                ApplyResult(
                    states = states,
                    activation = activation,
                    durationMs = durationMs,
                    reloadError = reloadError
                )
            }

            renderProfiles(result.states)
            when {
                result.reloadError != null || result.activation.failures.isNotEmpty() -> {
                    showToast(
                        getString(
                            R.string.project_dict_profiles_apply_failed,
                            result.activation.failures.joinToString(limit = 2) {
                                "${it.profileId}:${it.reason}"
                            }.ifBlank { "-" },
                            result.reloadError ?: "-"
                        )
                    )
                }
                !result.activation.shouldReload -> {
                    showToast(R.string.project_dict_profiles_apply_noop)
                }
                else -> {
                    showToast(
                        getString(
                            R.string.project_dict_profiles_apply_success,
                            result.activation.activeProfiles.size,
                            result.durationMs
                        )
                    )
                }
            }
        }
    }

    private fun reimportBundledProfiles() {
        val assets = requireContext().assets
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                val startTime = System.currentTimeMillis()
                val reimport = ProfileDictionaryService.forceReimportBundledProfiles(assets)
                var reloadError: String? = null
                if (reimport.shouldReload) {
                    runCatching {
                        viewModel.fcitx.runOnReady {
                            reloadPinyinDict()
                        }
                    }.onFailure {
                        reloadError = it.message ?: it.javaClass.simpleName
                        Timber.e(it, "ProjectDictProfile: Reload failed after reimport")
                    }
                }
                val states = ProfileDictionaryService.listStates()
                val durationMs = System.currentTimeMillis() - startTime
                ReimportResult(
                    states = states,
                    reimport = reimport,
                    durationMs = durationMs,
                    reloadError = reloadError
                )
            }

            bootstrapFailures = emptyList()
            renderProfiles(result.states)
            when {
                result.reloadError != null || result.reimport.failures.isNotEmpty() -> {
                    showToast(
                        getString(
                            R.string.project_dict_profiles_reimport_failed,
                            summarizeReimportFailures(result.reimport.failures),
                            result.reloadError ?: "-"
                        )
                    )
                }
                else -> {
                    showToast(
                        getString(
                            R.string.project_dict_profiles_reimport_success,
                            result.reimport.importedProfiles.size,
                            result.durationMs
                        )
                    )
                }
            }
        }
    }

    private fun buildOverviewSummary(
        states: List<ProfileDictionaryState>,
        activeProfiles: List<String>
    ): String {
        if (states.isEmpty()) {
            return getString(R.string.project_dict_profiles_empty_summary)
        }
        return getString(
            R.string.project_dict_profiles_overview_summary,
            activeProfiles.size,
            states.size,
            activeProfiles.joinToString().ifBlank { getString(R.string.none) }
        )
    }

    private fun buildBootstrapFailureSummary(failures: List<ProfileBootstrapFailure>): String {
        return failures.joinToString(limit = 3) { "${it.profileId}:${it.reason}" }.let {
            if (failures.size > 3) "$it ..." else it
        }
    }

    private fun summarizeReimportFailures(
        failures: List<ProfileDictionaryReimportManager.ReimportFailure>
    ): String {
        return failures.joinToString(limit = 3) {
            "${it.profileId}:${it.stage.name.lowercase()}:${it.reason}"
        }.let { if (failures.size > 3) "$it ..." else it }.ifBlank { "-" }
    }

    private fun showToast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private data class LoadResult(
        val states: List<ProfileDictionaryState>,
        val bootstrapFailures: List<ProfileBootstrapFailure>
    )

    private data class ApplyResult(
        val states: List<ProfileDictionaryState>,
        val activation: ProfileDictionaryActivationManager.ApplyResult,
        val durationMs: Long,
        val reloadError: String?
    )

    private data class ReimportResult(
        val states: List<ProfileDictionaryState>,
        val reimport: ProfileDictionaryReimportManager.ReimportResult,
        val durationMs: Long,
        val reloadError: String?
    )
}
