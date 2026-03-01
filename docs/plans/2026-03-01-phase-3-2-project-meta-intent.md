# Phase 3.2 Project Meta Intent Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 通过开放 Intent 从 SSH 终端 app 向 IME app 传递 `.ime/meta.json` 内容，IME 侧根据 `dict_profiles/tags` 激活对应 profile 词库并完成一次热加载；非法输入静默丢弃。

**Architecture:** IME 新增一个导出的 `BroadcastReceiver` 作为入口，接收 `meta_json` 后执行“5 秒限流 -> JSON 解析 -> profile 过滤 -> applySelection -> reloadPinyinDict”流水线。词库启停与重载完全复用现有 `ProfileDictionaryService` 与 `reloadPinyinDict` 路径，不再传输 `dict.tsv`。

**Tech Stack:** Android BroadcastReceiver、Kotlin、kotlinx.serialization-json、FcitxDaemon/FcitxConnection、现有 ProjectDict Profile 子系统。

## 1. 已确认决策

1. 安装包名迁移为私有包名：`com.lsl.lemonhall.fcitx5`。
2. 仅传 `.ime/meta.json`，不再跨 app 传 `dict.tsv`。
3. `dict_profiles` 缺失时，回退使用 `tags`。
4. `.ime/meta.json` 缺失时，发送并应用 `base-only`（只激活 `base`）。
5. 不做调用方签名校验。
6. 新增防御：5 秒窗口限流，窗口内后续 Intent 直接忽略。

## 2. 通信协议（v1）

### 2.1 Intent 常量
- Action: `com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META`
- Extra Key: `meta_json`

说明：Action 使用稳定常量，不跟随 debug `applicationIdSuffix` 变化。

### 2.2 Payload
`meta_json` 为完整 JSON 字符串，推荐结构：

```json
{
  "version": 1,
  "project": "ai-coding-ime",
  "dict_profiles": ["app.android", "engineering.testing"],
  "tags": ["app.android", "engineering.testing"]
}
```

处理规则：
1. 优先使用 `dict_profiles`（数组）。
2. `dict_profiles` 不存在或为空时，回退 `tags`（数组）。
3. 最终 profile 集合仅保留 catalog 中存在的 id。
4. `base` 强制保留（由 `ProfileDictionaryActivationManager` 兜底）。

## 3. 包名迁移方案

### 3.1 最小改动策略
只改 `applicationId`，不改 Kotlin `package` 与 `namespace`。

- 修改文件：`app/build.gradle.kts`
- 目标值：`applicationId = "com.lsl.lemonhall.fcitx5"`

### 3.2 影响面
1. 新旧包名可与上游 Fcitx 并存安装。
2. 系统视为新 app，旧数据不会自动迁移。
3. 需要重新在系统输入法设置里启用新 app。
4. SSH 终端 app 的 `setPackage(...)` 需同步目标包名。

## 4. IME 侧实现设计

### 4.1 新增组件
1. `ProjectMetaIntentReceiver`（导出 BroadcastReceiver）
2. `ProjectMetaParser`（解析 `meta_json` -> `Set<String>`）
3. `ProjectMetaIntentRateLimiter`（5 秒限流）
4. `ProjectMetaIntentProcessor`（可选，便于单测）

### 4.2 Manifest 接入
在 `AndroidManifest.xml` 中注册 receiver：
- `android:exported="true"`
- `intent-filter` 仅声明 `com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META`

### 4.3 Receiver 处理流水线
在 `onReceive` 内使用 `goAsync` + 协程后台执行：

1. 限流检查：
- 若距离上次“接受处理”的时间 < 5000ms，直接 `return`。
- 使用 `SystemClock.elapsedRealtime()` + `AtomicLong` 做无锁/低锁判断。

2. 读取 payload：
- `meta_json` 为空或全空白 -> `return`。
- 防御性长度上限（建议 64KB，超限直接 `return`）。

3. 解析 profile：
- JSON 非法 -> `return`（静默）。
- 提取 `dict_profiles` / `tags`，清洗空字符串、去重。

4. 激活与重载：
- `targetProfiles = parsedProfiles`。
- `activation = ProfileDictionaryService.applySelection(targetProfiles)`。
- 若 `activation.shouldReload` 为 `true`：
  - `val conn = FcitxDaemon.connect("ProjectMetaIntentReceiver")`
  - `conn.runOnReady { reloadPinyinDict() }`
  - `finally { FcitxDaemon.disconnect("ProjectMetaIntentReceiver") }`

5. 错误处理：
- 任何异常吞掉并记录 `Timber.d/w`。
- 不弹 Toast，不打断用户输入。

## 5. 5 秒限流细节（新增要求）

### 5.1 行为定义
- 以 receiver 为入口做全局限流。
- 任意来源 Intent 在 5 秒窗口内只允许第一条进入处理链。
- 窗口内后续请求一律忽略，不排队，不延后执行。

### 5.2 建议实现

```kotlin
object ProjectMetaIntentRateLimiter {
    private const val WINDOW_MS = 5_000L
    private val lastAcceptedAtMs = AtomicLong(0L)

    fun tryAcquire(nowMs: Long = SystemClock.elapsedRealtime()): Boolean {
        while (true) {
            val last = lastAcceptedAtMs.get()
            if (nowMs - last < WINDOW_MS) return false
            if (lastAcceptedAtMs.compareAndSet(last, nowMs)) return true
        }
    }
}
```

### 5.3 预期效果
- 防止调用方频繁触发词库启停与重载。
- 配合 `applySelection().shouldReload`，减少无效重载。

## 6. SSH 终端 app 侧约定

1. 仅在项目上下文变化时发送。
2. 发送前尽量做内容去重（同目录同内容不重复发）。
3. 发送方式：

```kotlin
Intent("com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META")
    .setPackage("com.lsl.lemonhall.fcitx5")
    .putExtra("meta_json", metaJsonString)
```

4. `.ime/meta.json` 不存在时发送 base-only：

```json
{"version":1,"dict_profiles":["base"]}
```

## 7. 静默失败策略

以下情况全部静默丢弃：
1. 5 秒限流命中。
2. `meta_json` 缺失/空字符串。
3. `meta_json` 超过长度上限。
4. JSON 解析失败。
5. 解析后 profile 为空且无可用项（最终仍会保留 base）。
6. reload 过程中异常。

## 8. 验收标准（DoD）

1. 合法 meta 可正确切换 profile，并在需要时只触发一次 reload。
2. 非法 meta 不引发崩溃、不弹窗、不改变当前状态。
3. 5 秒窗口内重复 Intent 不会触发重复处理。
4. `.ime/meta.json` 缺失场景可稳定回退 `base-only`。
5. 与上游 Fcitx 可并存安装，不相互抢占目标通信。

## 9. 实施任务拆分（供后续执行）

### Task 1: 包名迁移

**Files:**
- Modify: `app/build.gradle.kts`

**Steps:**
1. 将 `defaultConfig.applicationId` 改为 `com.lsl.lemonhall.fcitx5`。
2. 构建 debug APK 验证可编译。
3. 在设备安装后确认与上游 app 可并存。

### Task 2: 协议常量与解析器

**Files:**
- Create: `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaProtocol.kt`
- Create: `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaParser.kt`
- Test: `app/src/test/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaParserTest.kt`

**Steps:**
1. 定义 action/extra 常量。
2. 实现 `meta_json` 解析与 profile 提取（dict_profiles -> tags 回退）。
3. 补充非法 JSON、空数组、未知 profile、去重等单测。

### Task 3: 限流器

**Files:**
- Create: `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentRateLimiter.kt`
- Test: `app/src/test/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentRateLimiterTest.kt`

**Steps:**
1. 实现 5 秒窗口 `tryAcquire`。
2. 单测覆盖首次放行、窗口内拒绝、窗口后恢复放行。

### Task 4: Receiver 接入

**Files:**
- Create: `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentReceiver.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Steps:**
1. 新增导出 receiver + intent-filter。
2. 串联限流、payload 读取、解析、applySelection。
3. 在需要时触发 reload，并确保 connect/disconnect 成对。
4. 全异常静默处理。

### Task 5: 文档同步

**Files:**
- Modify: `docs/init.md`
- Modify: `docs/projectdict-manual-test.md`

**Steps:**
1. 将 Phase 3.2 协议更新为 `meta_json` 通道。
2. 将 Phase 3.3 的“签名校验”描述改为当前策略（开放 Intent + 防御限流）。
3. 增加 5 秒限流手工回归步骤。

### Task 6: 验证命令

**Run:**

```bash
./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*" --console=plain
./scripts/gradle-dev.sh --fast --assemble
```

**Expected:**
1. ProjectDict 相关 JVM 单测通过。
2. Debug APK 成功产出。

## 10. 风险与取舍

1. 开放 Intent 无调用方身份校验，存在被第三方 app 高频触发的风险。
2. 当前通过 5 秒限流 + payload 上限 + 静默丢弃控制资源风险。
3. 若后续出现滥用，再增量升级为权限保护或签名校验，不影响当前协议主体。
