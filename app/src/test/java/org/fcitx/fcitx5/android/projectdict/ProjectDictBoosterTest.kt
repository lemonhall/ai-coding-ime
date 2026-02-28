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
}
