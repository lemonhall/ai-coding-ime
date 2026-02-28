/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

/**
 * ProjectDict JNI facade for pinyin fuzzy/correction matching.
 */
object ProjectDictNative {

    const val NO_MATCH = -1

    @Volatile
    private var matcherForTest: ((String, Array<String>) -> IntArray)? = null

    fun matchPinyinFuzzyBatch(input: String, candidatePinyins: List<String>): IntArray {
        if (input.isEmpty() || candidatePinyins.isEmpty()) {
            return IntArray(0)
        }
        val pinyinsArray = candidatePinyins.toTypedArray()
        matcherForTest?.let { matcher ->
            val mocked = matcher(input, pinyinsArray)
            return sanitizeResult(mocked, pinyinsArray.size)
        }

        return runCatching { matchPinyinFuzzyBatchNative(input, pinyinsArray) }
            .map { sanitizeResult(it, pinyinsArray.size) }
            .getOrElse { IntArray(pinyinsArray.size) { NO_MATCH } }
    }

    internal fun setMatcherForTest(matcher: ((String, Array<String>) -> IntArray)?) {
        matcherForTest = matcher
    }

    private fun sanitizeResult(result: IntArray?, expectedSize: Int): IntArray {
        if (result == null || result.size != expectedSize) {
            return IntArray(expectedSize) { NO_MATCH }
        }
        return result
    }

    @JvmStatic
    private external fun matchPinyinFuzzyBatchNative(
        input: String,
        candidatePinyins: Array<String>
    ): IntArray?
}
