/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination
import timber.log.Timber

/**
 * 项目词库管理器（单例）
 *
 * 职责：
 * - 加载和解析词库内容
 * - 维护当前活跃的项目词库
 * - 提供查询接口
 * - 支持热替换
 */
object ProjectDictManager {

    private data class PinyinTokenSet(
        val fullOptions: List<String>
    )

    private data class FuzzyCandidate(
        val entry: ProjectDictEntry,
        val fullPinyin: String
    )

    private data class FuzzyMatch(
        val entry: ProjectDictEntry,
        val correctionCost: Int
    ) {
        val score: Int = entry.weight - FUZZY_BASE_PENALTY - correctionCost * FUZZY_COST_PENALTY
    }

    private var entries: List<ProjectDictEntry> = emptyList()
    private var projectName: String? = null
    private val pinyinCache = mutableMapOf<String, List<PinyinTokenSet>>()

    private val pinyinFormat = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
        vCharType = HanyuPinyinVCharType.WITH_V
    }

    /**
     * 加载词库内容
     *
     * @param content TSV 文件内容
     * @param project 项目名称（可选）
     */
    fun load(content: String, project: String? = null): Int {
        val parsed = ProjectDictParser.parse(content)
        entries = parsed
        projectName = project
        pinyinCache.clear()
        Timber.i("ProjectDict: Loaded ${entries.size} entries for project '$project'")
        return entries.size
    }

    /**
     * 清空词库
     */
    fun clear() {
        entries = emptyList()
        projectName = null
        pinyinCache.clear()
        Timber.i("ProjectDict: Cleared")
    }

    /**
     * 查询匹配的词条
     *
     * @param input 用户输入（拼音或英文前缀）
     * @param limit 返回结果数量上限
     * @return 匹配的词条列表，按权重降序排列
     */
    fun query(input: String, limit: Int = 10): List<ProjectDictEntry> {
        if (input.isEmpty() || entries.isEmpty()) {
            return emptyList()
        }

        // Pinyin preedit may contain separators like apostrophe/space: "ge'tu", "ge tu".
        val normalizedIdentifierInput = normalizeIdentifierInput(input)
        val normalizedPinyinInput = normalizePinyinInput(input)
        val strictMatches = mutableListOf<ProjectDictEntry>()
        val fuzzyCandidatePool = mutableListOf<FuzzyCandidate>()

        for (entry in entries) {
            val matched = when (entry.type) {
                ProjectDictEntry.EntryType.ID -> {
                    // 标识符：英文前缀匹配，不区分大小写
                    normalizedIdentifierInput.isNotEmpty() &&
                            normalizeIdentifierInput(entry.text).startsWith(normalizedIdentifierInput)
                }
                ProjectDictEntry.EntryType.TERM -> {
                    // 术语：中文前缀匹配 + 拼音前缀匹配（全拼/首字母）
                    entry.text.startsWith(input) ||
                            matchChineseByPinyin(entry.text, normalizedIdentifierInput)
                }
                ProjectDictEntry.EntryType.ABBR -> {
                    // 缩写：精确匹配或前缀匹配
                    val normalizedEntry = normalizeIdentifierInput(entry.text)
                    normalizedIdentifierInput.isNotEmpty() &&
                            (normalizedEntry == normalizedIdentifierInput ||
                                    normalizedEntry.startsWith(normalizedIdentifierInput))
                }
                ProjectDictEntry.EntryType.PHRASE -> {
                    // 短语：中文前缀匹配 + 拼音前缀匹配（全拼/首字母）
                    entry.text.startsWith(input) ||
                            matchChineseByPinyin(entry.text, normalizedIdentifierInput)
                }
            }

            if (matched) {
                strictMatches.add(entry)
            } else if (shouldTryFuzzy(entry.type, normalizedPinyinInput)) {
                toRepresentativeFullPinyin(entry.text)?.let { pinyin ->
                    fuzzyCandidatePool.add(FuzzyCandidate(entry, pinyin))
                }
            }
        }

        val strictSorted = strictMatches.sortedByDescending { it.weight }
        if (strictSorted.size >= limit || normalizedPinyinInput.isEmpty()) {
            return strictSorted.take(limit)
        }

        val fuzzyCandidates = fuzzyCandidatePool
            .sortedByDescending { it.entry.weight }
            .take(MAX_FUZZY_CANDIDATES)
        if (fuzzyCandidates.isEmpty()) {
            return strictSorted.take(limit)
        }

        val fuzzyCosts = ProjectDictNative.matchPinyinFuzzyBatch(
            normalizedPinyinInput,
            fuzzyCandidates.map { it.fullPinyin }
        )
        val fuzzyMatches = fuzzyCandidates.mapIndexedNotNull { index, candidate ->
            val cost = fuzzyCosts.getOrNull(index) ?: ProjectDictNative.NO_MATCH
            if (cost < 0) {
                null
            } else {
                FuzzyMatch(candidate.entry, cost)
            }
        }.sortedWith(
            compareByDescending<FuzzyMatch> { it.score }
                .thenByDescending { it.entry.weight }
        ).map { it.entry }

        return (strictSorted + fuzzyMatches).take(limit)
    }

    /**
     * 获取当前词库信息
     */
    fun getInfo(): String {
        return "Project: ${projectName ?: "none"}, Entries: ${entries.size}"
    }

    /**
     * 是否已加载词库
     */
    fun isLoaded(): Boolean = entries.isNotEmpty()

    private fun normalizeIdentifierInput(raw: String): String {
        return buildString(raw.length) {
            raw.forEach { ch ->
                when {
                    ch in 'A'..'Z' -> append(ch.lowercaseChar())
                    ch in 'a'..'z' || ch in '0'..'9' || ch == '_' -> append(ch)
                }
            }
        }
    }

    private fun normalizePinyinInput(raw: String): String {
        return buildString(raw.length) {
            raw.forEach { ch ->
                when {
                    ch in 'A'..'Z' -> append(ch.lowercaseChar())
                    ch in 'a'..'z' -> append(ch)
                }
            }
        }
    }

    private fun shouldTryFuzzy(
        type: ProjectDictEntry.EntryType,
        normalizedPinyinInput: String
    ): Boolean {
        if (normalizedPinyinInput.isEmpty() || normalizedPinyinInput.length > MAX_FUZZY_INPUT_LENGTH) {
            return false
        }
        return type == ProjectDictEntry.EntryType.TERM || type == ProjectDictEntry.EntryType.PHRASE
    }

    private fun toRepresentativeFullPinyin(text: String): String? {
        val tokens = pinyinCache.getOrPut(text) { buildPinyinTokenSets(text) }
        if (tokens.isEmpty()) {
            return null
        }
        val syllables = ArrayList<String>(tokens.size)
        for (token in tokens) {
            val option = token.fullOptions.firstOrNull() ?: return null
            if (!isPinyinSyllable(option)) {
                return null
            }
            syllables.add(option)
        }
        return syllables.joinToString("'")
    }

    private fun isPinyinSyllable(raw: String): Boolean {
        if (raw.isEmpty()) {
            return false
        }
        return raw.all { it in 'a'..'z' }
    }

    private fun matchChineseByPinyin(text: String, normalizedInput: String): Boolean {
        if (normalizedInput.isEmpty()) {
            return false
        }
        val tokens = pinyinCache.getOrPut(text) { buildPinyinTokenSets(text) }
        if (tokens.isEmpty()) {
            return false
        }
        return matchesPinyinFullPrefix(tokens, normalizedInput) ||
                matchesPinyinInitialPrefix(tokens, normalizedInput)
    }

    private fun buildPinyinTokenSets(text: String): List<PinyinTokenSet> {
        if (text.isBlank()) {
            return emptyList()
        }
        val tokens = mutableListOf<PinyinTokenSet>()
        val ascii = StringBuilder()

        fun flushAscii() {
            if (ascii.isNotEmpty()) {
                tokens.add(PinyinTokenSet(listOf(ascii.toString())))
                ascii.clear()
            }
        }

        text.forEach { ch ->
            when {
                ch in 'A'..'Z' -> ascii.append(ch.lowercaseChar())
                ch in 'a'..'z' || ch in '0'..'9' || ch == '_' -> ascii.append(ch)
                else -> {
                    val options = toPinyinOptions(ch)
                    if (options.isNotEmpty()) {
                        flushAscii()
                        tokens.add(PinyinTokenSet(options))
                    } else {
                        flushAscii()
                    }
                }
            }
        }
        flushAscii()
        return tokens
    }

    private fun toPinyinOptions(ch: Char): List<String> {
        return try {
            PinyinHelper.toHanyuPinyinStringArray(ch, pinyinFormat)
                ?.map { normalizeIdentifierInput(it) }
                ?.filter { it.isNotEmpty() }
                ?.distinct()
                ?: emptyList()
        } catch (_: BadHanyuPinyinOutputFormatCombination) {
            emptyList()
        }
    }

    private fun matchesPinyinFullPrefix(tokens: List<PinyinTokenSet>, input: String): Boolean {
        if (input.isEmpty()) {
            return false
        }
        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = HashSet<Pair<Int, Int>>()
        queue.add(0 to 0)

        while (queue.isNotEmpty()) {
            val (tokenIdx, inputIdx) = queue.removeFirst()
            val state = tokenIdx to inputIdx
            if (!visited.add(state)) {
                continue
            }
            if (inputIdx >= input.length) {
                return true
            }
            if (tokenIdx >= tokens.size) {
                continue
            }

            val token = tokens[tokenIdx]
            token.fullOptions.forEach { option ->
                var consumed = 0
                while (
                    consumed < option.length &&
                    inputIdx + consumed < input.length &&
                    option[consumed] == input[inputIdx + consumed]
                ) {
                    consumed++
                }
                if (consumed == 0) {
                    return@forEach
                }
                val newInputIdx = inputIdx + consumed
                if (newInputIdx >= input.length) {
                    return true
                }
                if (consumed == option.length) {
                    queue.add(tokenIdx + 1 to newInputIdx)
                }
            }
        }
        return false
    }

    private fun matchesPinyinInitialPrefix(tokens: List<PinyinTokenSet>, input: String): Boolean {
        if (input.isEmpty()) {
            return false
        }
        var idx = 0
        for (token in tokens) {
            if (idx >= input.length) {
                return true
            }
            val target = input[idx]
            val matched = token.fullOptions.any { option ->
                option.isNotEmpty() && option[0] == target
            }
            if (!matched) {
                return false
            }
            idx++
        }
        return idx >= input.length
    }

    private const val FUZZY_BASE_PENALTY = 120
    private const val FUZZY_COST_PENALTY = 30
    private const val MAX_FUZZY_INPUT_LENGTH = 64
    private const val MAX_FUZZY_CANDIDATES = 256
}
