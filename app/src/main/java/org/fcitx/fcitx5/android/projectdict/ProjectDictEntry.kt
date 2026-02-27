/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 MaJing IME Contributors
 */
package org.fcitx.fcitx5.android.projectdict

/**
 * 项目词库词条数据类
 *
 * @property text 词条文本（如 "getUserInfo", "数据迁移"）
 * @property type 词条类型
 * @property weight 权重（0-1000），越大越优先
 * @property hint 上下文提示（可选，如 "database migration"）
 */
data class ProjectDictEntry(
    val text: String,
    val type: EntryType,
    val weight: Int,
    val hint: String? = null
) {
    /**
     * 词条类型枚举
     */
    enum class EntryType {
        /** 标识符（函数名、变量名、类名等） */
        ID,
        /** 领域术语（技术概念、业务术语） */
        TERM,
        /** 缩写 */
        ABBR,
        /** 常用短语 */
        PHRASE;

        companion object {
            /**
             * 从字符串解析类型，不区分大小写
             */
            fun fromString(s: String): EntryType? = when (s.lowercase()) {
                "id" -> ID
                "term" -> TERM
                "abbr" -> ABBR
                "phrase" -> PHRASE
                else -> null
            }
        }
    }
}
