/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import org.fcitx.fcitx5.android.core.FcitxEvent
import org.junit.Assert
import org.junit.Test

class ProjectDictBoosterTest {

    @Test
    fun extractProjectCommitTextFromBulkCandidate() {
        Assert.assertEquals("getUserInfo", ProjectDictBooster.extractProjectCommitText("getUserInfo [P]"))
        Assert.assertEquals("cfg", ProjectDictBooster.extractProjectCommitText("cfg [P:config/configuration]"))
        Assert.assertEquals(null, ProjectDictBooster.extractProjectCommitText("个图"))
    }

    @Test
    fun identifyProjectCandidateInPagedMode() {
        val project = FcitxEvent.Candidate(label = "", text = "getUserInfo", comment = "[P]")
        val normal = FcitxEvent.Candidate(label = "", text = "个图", comment = "")

        Assert.assertTrue(ProjectDictBooster.isProjectPagedCandidate(project))
        Assert.assertFalse(ProjectDictBooster.isProjectPagedCandidate(normal))
        Assert.assertEquals("getUserInfo", ProjectDictBooster.extractProjectCommitText(project))
        Assert.assertEquals(null, ProjectDictBooster.extractProjectCommitText(normal))
    }

    @Test
    fun mapBulkDisplayIndexToEngineIndexSkipsProjectCandidates() {
        val mixed = arrayOf(
            "API_BASE_URL [P]",
            "安排",
            "apt"
        )

        Assert.assertEquals(0, ProjectDictBooster.mapBulkDisplayIndexToEngineIndex(0, mixed))
        Assert.assertEquals(0, ProjectDictBooster.mapBulkDisplayIndexToEngineIndex(1, mixed))
        Assert.assertEquals(1, ProjectDictBooster.mapBulkDisplayIndexToEngineIndex(2, mixed))
        Assert.assertEquals(2, ProjectDictBooster.mapBulkDisplayIndexToEngineIndex(3, mixed))
    }

    @Test
    fun mapPagedDisplayIndexToEngineIndexSkipsProjectCandidates() {
        val mixed = arrayOf(
            FcitxEvent.Candidate(label = "", text = "API_BASE_URL", comment = "[P]"),
            FcitxEvent.Candidate(label = "", text = "安排", comment = ""),
            FcitxEvent.Candidate(label = "", text = "apt", comment = "")
        )

        Assert.assertEquals(0, ProjectDictBooster.mapPagedDisplayIndexToEngineIndex(0, mixed))
        Assert.assertEquals(0, ProjectDictBooster.mapPagedDisplayIndexToEngineIndex(1, mixed))
        Assert.assertEquals(1, ProjectDictBooster.mapPagedDisplayIndexToEngineIndex(2, mixed))
        Assert.assertEquals(2, ProjectDictBooster.mapPagedDisplayIndexToEngineIndex(3, mixed))
    }
}
