/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.core.reloadPinyinDict
import org.fcitx.fcitx5.android.daemon.FcitxDaemon
import org.fcitx.fcitx5.android.projectdict.profile.ProfileDictionaryService
import timber.log.Timber

class ProjectMetaIntentReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineName(RECEIVER_NAME)
    )
    private val rateLimiter = ProjectMetaIntentRateLimiter()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ProjectMetaProtocol.ACTION_APPLY_PROJECT_META) {
            return
        }
        if (!rateLimiter.tryAcquire()) {
            Timber.d("ProjectMeta: Intent dropped by rate limiter")
            return
        }
        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                handleMetaIntent(intent)
            } catch (t: Throwable) {
                Timber.w(t, "ProjectMeta: Failed to apply project meta")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleMetaIntent(intent: Intent) {
        val metaJson = intent.getStringExtra(ProjectMetaProtocol.EXTRA_META_JSON)
            ?.takeIf { it.isNotBlank() }
            ?: return
        if (metaJson.length > ProjectMetaProtocol.MAX_META_JSON_LENGTH) {
            Timber.w("ProjectMeta: meta_json too large, ignore")
            return
        }

        val targetProfiles = ProjectMetaParser.parseProfiles(metaJson)
        val activation = ProfileDictionaryService.applySelection(targetProfiles)
        if (!activation.shouldReload) {
            return
        }

        val connection = FcitxDaemon.connect(RECEIVER_NAME)
        try {
            connection.runOnReady {
                reloadPinyinDict()
            }
        } catch (t: Throwable) {
            Timber.w(t, "ProjectMeta: reloadPinyinDict failed")
        } finally {
            FcitxDaemon.disconnect(RECEIVER_NAME)
        }
    }

    companion object {
        private const val RECEIVER_NAME = "ProjectMetaIntentReceiver"
    }
}
