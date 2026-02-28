/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.ui.main

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.projectdict.ProjectDictManager
import org.fcitx.fcitx5.android.ui.common.PaddingPreferenceFragment
import org.fcitx.fcitx5.android.utils.addPreference
import org.fcitx.fcitx5.android.utils.setup
import timber.log.Timber

class ProjectDictFragment : PaddingPreferenceFragment() {

    private var infoPreference: Preference? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadDictionaryFromUri(uri)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireContext()).apply {
            addPreference(R.string.project_dict_load_from_file) {
                pickFile()
            }
            addPreference(R.string.project_dict_load_from_clipboard) {
                loadDictionaryFromClipboard()
            }
            addPreference(R.string.project_dict_clear) {
                clearDictionary()
            }
            infoPreference = Preference(requireContext()).apply {
                setup(
                    title = getString(R.string.project_dict_info),
                    summary = getDictionaryInfo()
                )
            }.also { addPreference(it) }
        }
    }

    private fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }

    private fun loadDictionaryFromUri(uri: android.net.Uri) {
        try {
            val content = requireContext().contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            } ?: run {
                showToast(R.string.project_dict_load_error)
                return
            }

            val projectName = uri.lastPathSegment ?: "unknown"
            val entryCount = ProjectDictManager.load(content, projectName)
            if (entryCount == 0) {
                showToast(R.string.project_dict_load_error)
                refreshInfo()
                Timber.w("ProjectDict: No valid entries found in file: $projectName")
                return
            }

            showToast(getString(R.string.project_dict_load_success, entryCount))
            refreshInfo()
            Timber.i("ProjectDict: Loaded from file: $projectName")
        } catch (e: Exception) {
            Timber.e(e, "ProjectDict: Failed to load from file")
            showToast(R.string.project_dict_load_error)
        }
    }

    private fun loadDictionaryFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboard.primaryClip

        if (clipData == null || clipData.itemCount == 0) {
            showToast(R.string.project_dict_load_error)
            return
        }

        val content = clipData.getItemAt(0).text?.toString()
        if (content.isNullOrEmpty()) {
            showToast(R.string.project_dict_load_error)
            return
        }

        try {
            val entryCount = ProjectDictManager.load(content, "clipboard")
            if (entryCount == 0) {
                showToast(R.string.project_dict_load_error)
                refreshInfo()
                Timber.w("ProjectDict: No valid entries found in clipboard content")
                return
            }

            showToast(getString(R.string.project_dict_load_success, entryCount))
            refreshInfo()
            Timber.i("ProjectDict: Loaded from clipboard")
        } catch (e: Exception) {
            Timber.e(e, "ProjectDict: Failed to load from clipboard")
            showToast(R.string.project_dict_load_error)
        }
    }

    private fun clearDictionary() {
        ProjectDictManager.clear()
        showToast(R.string.project_dict_cleared)
        refreshInfo()
        Timber.i("ProjectDict: Cleared")
    }

    private fun getDictionaryInfo(): String {
        return if (ProjectDictManager.isLoaded()) {
            ProjectDictManager.getInfo()
        } else {
            getString(R.string.project_dict_not_loaded)
        }
    }

    private fun refreshInfo() {
        infoPreference?.summary = getDictionaryInfo()
    }

    private fun showToast(resId: Int) {
        Toast.makeText(requireContext(), resId, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
