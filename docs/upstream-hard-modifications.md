# 上游硬修改清单

> 目的：记录所有“与上游默认行为不同、且会影响用户可感知行为”的本仓硬修改，便于升级、回归、回滚。

## 判定标准

- 修改了上游默认行为（不是纯新增功能）。
- 用户在首次安装或默认路径下会直接感知到差异。
- 需要在上游同步时重点回归。

## 当前清单（持续追加）

| ID | 状态 | 日期 | 模块 | 变更摘要 | 触发条件 | 主要文件 | 验证命令 | 回滚点 |
|---|---|---|---|---|---|---|---|---|
| HM-001 | ACTIVE | 2026-03-01 | 首启输入法顺序 | 首次启动默认将 `pinyin` 提到启用输入法第 1 位（此前常见为 `keyboard-us` 在前） | `firstRun=true` 且启用列表包含 `pinyin` | `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` `app/src/main/java/org/fcitx/fcitx5/android/core/InputMethodOrder.kt` | `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.core.InputMethodOrderTest"` | 移除 `onFirstRun()` 中 `InputMethodOrder.prioritize(...)` 调用 |
| HM-002 | ACTIVE | 2026-03-01 | 首启标点默认值 | 首次启动默认将拼音标点从“全角开启”改为“半角”（`punctuation.cfg.Enabled=False`） | `firstRun=true` 且读取到 `punctuation` addon 配置项 `Enabled` | `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` `app/src/main/java/org/fcitx/fcitx5/android/core/AddonConfigDefaults.kt` | `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.core.AddonConfigDefaultsTest"` | 移除 `onFirstRun()` 中 `AddonConfigDefaults.setBooleanOption(..., value = false)` 分支 |

## 新增条目模板

复制一行并填写：

| HM-XXX | ACTIVE/REMOVED | YYYY-MM-DD | 模块 | 变更摘要 | 触发条件 | 主要文件 | 验证命令 | 回滚点 |
|---|---|---|---|---|---|---|---|---|

