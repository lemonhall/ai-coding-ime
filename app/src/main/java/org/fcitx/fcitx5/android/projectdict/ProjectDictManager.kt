/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import timber.log.Timber
import java.util.Locale

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

    private var entries: List<ProjectDictEntry> = emptyList()
    private var projectName: String? = null

    /**
     * 加载词库内容
     *
     * @param content TSV 文件内容
     * @param project 项目名称（可选）
     */
    fun load(content: String, project: String? = null) {
        val parsed = ProjectDictParser.parse(content)
        entries = parsed
        projectName = project
        Timber.i("ProjectDict: Loaded ${entries.size} entries for project '$project'")
    }

    /**
     * 清空词库
     */
    fun clear() {
        entries = emptyList()
        projectName = null
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

        val inputLower = input.lowercase(Locale.getDefault())
        val results = mutableListOf<ProjectDictEntry>()

        for (entry in entries) {
            val matched = when (entry.type) {
                ProjectDictEntry.EntryType.ID -> {
                    // 标识符：英文前缀匹配，不区分大小写
                    entry.text.lowercase(Locale.getDefault()).startsWith(inputLower)
                }
                ProjectDictEntry.EntryType.TERM -> {
                    // 术语：中文前缀匹配或拼音前缀匹配（暂时简化为中文前缀）
                    entry.text.startsWith(input)
                }
                ProjectDictEntry.EntryType.ABBR -> {
                    // 缩写：精确匹配或前缀匹配
                    entry.text.equals(input, ignoreCase = true) ||
                            entry.text.lowercase(Locale.getDefault()).startsWith(inputLower)
                }
                ProjectDictEntry.EntryType.PHRASE -> {
                    // 短语：中文前缀匹配
                    entry.text.startsWith(input)
                }
            }

            if (matched) {
                results.add(entry)
            }
        }

        // 按权重降序排列，取前 limit 个
        return results.sortedByDescending { it.weight }.take(limit)
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
}
