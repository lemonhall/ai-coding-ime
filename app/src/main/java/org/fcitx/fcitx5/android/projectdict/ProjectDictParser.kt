/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

import timber.log.Timber

/**
 * 项目词库 TSV 解析器
 *
 * 解析 .ime/dict.tsv 格式的词库文件
 * 格式：词条<TAB>类型<TAB>权重<TAB>上下文提示(可选)
 */
object ProjectDictParser {

    /**
     * 解析 TSV 内容为词条列表
     *
     * @param content TSV 文件内容
     * @return 解析成功的词条列表（跳过错误行）
     */
    fun parse(content: String): List<ProjectDictEntry> {
        val entries = mutableListOf<ProjectDictEntry>()
        var lineNumber = 0

        content.lineSequence().forEach { line ->
            lineNumber++
            val trimmed = line.trim()

            // 跳过空行和注释
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                return@forEach
            }

            // 解析词条行：
            // - 优先按 TAB（规范 TSV）
            // - 若无 TAB，则回退到空白分隔（兼容手工输入/复制）
            val fields = parseFields(trimmed)
            if (fields.size < 3) {
                Timber.w("ProjectDict: Line $lineNumber: insufficient fields (need at least 3), skipping")
                return@forEach
            }

            val text = fields[0].trim()
            val typeStr = fields[1].trim()
            val weightStr = fields[2].trim()
            val hint = if (fields.size >= 4) fields[3].trim().takeIf { it.isNotEmpty() } else null

            // 验证字段
            if (text.isEmpty()) {
                Timber.w("ProjectDict: Line $lineNumber: empty text, skipping")
                return@forEach
            }

            val type = ProjectDictEntry.EntryType.fromString(typeStr)
            if (type == null) {
                Timber.w("ProjectDict: Line $lineNumber: invalid type '$typeStr', skipping")
                return@forEach
            }

            val weight = weightStr.toIntOrNull()
            if (weight == null || weight !in 0..1000) {
                Timber.w("ProjectDict: Line $lineNumber: invalid weight '$weightStr' (must be 0-1000), skipping")
                return@forEach
            }

            entries.add(ProjectDictEntry(text, type, weight, hint))
        }

        Timber.i("ProjectDict: Parsed ${entries.size} entries from $lineNumber lines")
        return entries
    }

    private fun parseFields(line: String): List<String> {
        if (line.contains('\t')) {
            return line.split('\t')
        }
        // Fallback: split by whitespace, keep the 4th field as "rest of line" (hint may include spaces)
        return line.split(Regex("\\s+"), limit = 4)
    }
}
