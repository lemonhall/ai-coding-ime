# SSH Terminal -> IME Meta Bridge PRD (Zero-Context Edition)

## 0. Document Purpose
本 PRD 面向“完全不了解当前 IME 项目上下文”的实现者。目标是让 SSH 终端 app 团队只靠本文即可完成联动功能开发，并与 IME 端协议严格一致。

## 1. Product Goal
当用户在 SSH 终端 app 中切换到不同远端项目目录时，终端 app 自动读取该目录下 `.ime/meta.json`（若存在），并将 JSON 内容发送给 IME app。IME app 基于其中的 `dict_profiles/tags` 激活对应词库 profile，并完成热加载。

### 1.1 Success Criteria
1. 用户无须手动在 IME 设置页切换 profile。
2. 项目切换后，5~10 秒内输入候选应体现新项目词库上下文。
3. 不存在 `.ime/meta.json` 时，可自动回退 `base-only`。

## 2. Integration Contract (Must Not Drift)

### 2.1 Target IME Package
- Release package: `com.lsl.lemonhall.fcitx5`
- Debug package（可选支持）: `com.lsl.lemonhall.fcitx5.debug`

建议：终端 app 配置目标包名白名单（release/debug），按已安装包选择发送目标。

### 2.2 Intent Protocol
- Action: `com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META`
- Extra key: `meta_json`
- Type: `String`

发送示例：

```kotlin
val intent = Intent("com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META")
    .setPackage("com.lsl.lemonhall.fcitx5")
    .putExtra("meta_json", metaJson)
context.sendBroadcast(intent)
```

### 2.3 Payload JSON Schema (Sender perspective)

```json
{
  "version": 1,
  "project": "optional-project-name",
  "dict_profiles": ["app.android", "engineering.testing"],
  "tags": ["app.android", "engineering.testing"]
}
```

规则：
1. 优先填写 `dict_profiles`。
2. 可同时携带 `tags`（兼容回退）。
3. 终端 app 不需要校验 profile 是否存在于 IME catalog（IME 会过滤）。

## 3. Functional Requirements

### REQ-SSH-001: Session Context Tracking
系统必须能感知“当前 SSH 会话项目上下文变化”。至少要检测：
1. SSH 会话连接到新主机。
2. 同会话内工作目录变化（`cd`）。
3. 前台活跃会话切换。

### REQ-SSH-002: Meta Discovery
在上下文变化后，终端 app 必须检查远端当前目录下 `.ime/meta.json`。

建议流程：
1. 获取当前远端目录绝对路径（如 `pwd -P`）。
2. 探测文件存在性（`test -f .ime/meta.json`）。
3. 若存在，读取内容并限制最大长度（建议 64KB）。

### REQ-SSH-003: Base Fallback
若 `.ime/meta.json` 不存在、不可读、超限或读取超时，终端 app 必须可发送 fallback payload：

```json
{"version":1,"dict_profiles":["base"]}
```

### REQ-SSH-004: Broadcast Dispatch
发送广播时必须：
1. 使用固定 action。
2. 使用 `setPackage(targetImePackage)`。
3. 写入 `meta_json` 字符串 extra。

### REQ-SSH-005: Sender-Side Throttle & Dedup
为避免噪声，终端 app 应该在发送前做两层保护：
1. 去重：若目标 package + payload hash 与上次相同，则不重复发送。
2. 去抖：同一会话短时间内连续目录变化时，建议 300~800ms 延迟合并后只发一次。

### REQ-SSH-006: Non-Blocking UX
所有探测与读取行为必须在后台线程执行，不得阻塞终端输入/渲染主线程。

### REQ-SSH-007: Silent Failure
若 IME 未安装、广播失败、读取失败，不应弹致命错误。可在 debug 日志记录。

## 4. Non-Functional Requirements

### NFR-001 Latency
从目录变化到广播发出，P95 建议 < 1500ms（不含远端网络抖动）。

### NFR-002 Reliability
网络抖动场景下，不应导致终端 app 崩溃；失败后下一次目录变化仍可恢复发送。

### NFR-003 Resource Control
1. 不缓存超大 meta 内容。
2. 探测命令设置超时（建议 1~2s）。
3. 限制重试次数（建议 0~1 次）。

## 5. Suggested Architecture (Reference)

## 5.1 Module Split
建议在 SSH app 内新增 `ime-bridge` 子模块（或同级 package）：

1. `SessionContextMonitor`
- 监听活跃会话、目录变化、主机变化。

2. `RemoteMetaProbe`
- 对远端执行 `.ime/meta.json` 探测与读取。

3. `MetaPayloadBuilder`
- 将探测结果转为最终 JSON 字符串（含 fallback）。

4. `ImeBroadcastDispatcher`
- 负责构建 Intent 并发送。

5. `DispatchGuard`
- 负责 sender-side 去重与去抖。

## 5.2 Recommended File Layout (Android/Kotlin)

```text
app/src/main/java/<your/package>/imebridge/
  ImeBridgeController.kt
  SessionContextMonitor.kt
  RemoteMetaProbe.kt
  MetaPayloadBuilder.kt
  ImeBroadcastDispatcher.kt
  DispatchGuard.kt
  model/
    SessionContext.kt
    ProbeResult.kt
    DispatchEvent.kt
```

如果你们仓库不是 Android/Kotlin，可保持同名职责映射到本地技术栈。

## 6. Detailed Behavior

### 6.1 Trigger Points
触发桥接流程的时机：
1. 用户执行命令后回到 shell prompt，检测到 `cwd` 变化。
2. 用户切换到另一个已连接会话。
3. SSH 重连后首次进入可执行状态。

### 6.2 End-to-End Flow
1. `SessionContextMonitor` 输出新 `SessionContext(host, user, cwd, sessionId)`。
2. `DispatchGuard` 检查是否重复上下文。
3. `RemoteMetaProbe` 读取 `.ime/meta.json`。
4. `MetaPayloadBuilder` 产出 `payload`（成功内容或 base fallback）。
5. `DispatchGuard` 检查 payload hash 是否重复。
6. `ImeBroadcastDispatcher` 发送显式广播。

### 6.3 Multi-Session Rule
当多会话并存时，仅对“当前前台活跃会话”发送；后台会话变化不触发发送。

### 6.4 Timeout / Error Rule
1. 探测超时：发 base fallback。
2. 读取异常：发 base fallback。
3. 广播失败：仅日志，不重试风暴。

## 7. Privacy & Safety
1. 仅发送 `.ime/meta.json` 内容，不发送项目源码或 `dict.tsv`。
2. 不持久化远端 meta 原文到磁盘（除非用户开启 debug）。
3. 日志默认不打印完整 payload，仅打印 hash/长度。

## 8. Telemetry (Optional but Recommended)
建议埋点字段：
1. `ime_bridge_trigger`（cause: cwd_change/session_switch/reconnect）
2. `ime_bridge_probe_result`（exists/timeout/error/size）
3. `ime_bridge_dispatch`（target_pkg/success/failure/hash_prefix）
4. `ime_bridge_fallback`（reason）

## 9. Test Plan

### 9.1 Unit Tests
1. `MetaPayloadBuilderTest`
- 正常 meta 透传。
- 空/非法/超限时 fallback。

2. `DispatchGuardTest`
- 同 payload 去重。
- 去抖窗口合并。

3. `ImeBroadcastDispatcherTest`
- action/key/package 正确。

### 9.2 Integration Tests
1. 模拟会话 `cwd` 连续切换：`A -> B -> B -> C`。
2. 仅 `A/C` 触发实际发送，重复 `B` 不重复发送。
3. 远端读取失败时发送 base fallback。

### 9.3 Manual QA
1. 项目目录存在 `.ime/meta.json`：切换后 IME 候选变化。
2. 目录无 `.ime/meta.json`：IME 回退 base。
3. 快速连续 `cd`：终端不卡顿，IME 无广播风暴。

## 10. Rollout Plan
1. Phase 1: 内测开关（默认关闭）。
2. Phase 2: 白名单用户开启，采集稳定性指标。
3. Phase 3: 全量开启。

建议配置项：
- `imeBridgeEnabled`（bool）
- `imeTargetPackage`（string，支持 debug/release）
- `probeTimeoutMs`（int，默认 1500）
- `debounceMs`（int，默认 500）

## 11. Out of Scope
1. 不负责 IME 侧 profile catalog 维护。
2. 不负责 IME 侧重载策略。
3. 不实现签名鉴权协商。

## 12. Handoff Checklist (for Another Codex)
1. 已知目标 IME 包名与 action。
2. 了解 `.ime/meta.json` 文件路径和 fallback 规则。
3. 知道要做会话监听、远端探测、广播发送三段式架构。
4. 具备最小测试清单。
5. 明确失败行为必须静默且不影响终端主流程。
