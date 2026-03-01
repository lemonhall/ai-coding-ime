# v4 Phase 3.2 Meta Intent Integration Plan

## 文档关系
- 上位文档：`docs/init.md`
- 配套 PRD（对端 SSH app）：`docs/prd/2026-03-01-ssh-to-ime-meta-bridge-prd.md`
- 目标阶段：`Phase 3.2`（SSH 联动）+ `Phase 3.3`（当前采用开放通道防御式方案）
- 当前状态：`PLANNED`

## 1. Goal
在 IME app 内新增一条开放 Intent 接收链路，接收 SSH app 传递的 `.ime/meta.json` 字符串，完成 profile 激活与单次热加载，且在异常输入下保持静默与稳定。

## 2. Scope / Non-Goal
### 2.1 In Scope
1. 安装包名改为私有：`com.lsl.lemonhall.fcitx5`。
2. 新增 `ACTION_APPLY_PROJECT_META` 接收入口。
3. 解析 `meta_json` 的 `dict_profiles`，缺失时回退 `tags`。
4. 调用 `ProfileDictionaryService.applySelection(...)` 并按需 `reloadPinyinDict()`。
5. 新增 5 秒限流（窗口内忽略后续 Intent）。
6. 非法输入静默丢弃（仅日志）。

### 2.2 Out of Scope
1. 不再通过跨 app 传输 `dict.tsv`。
2. 本轮不做调用方签名校验。
3. 本轮不做云端同步。
4. 本轮不做可视化调试 UI。

## 3. 协议固定（必须与对端 PRD 一致）
1. Action：`com.lsl.lemonhall.fcitx5.action.APPLY_PROJECT_META`
2. Extra Key：`meta_json`
3. Payload：完整 JSON 字符串（优先 `dict_profiles`，回退 `tags`）
4. 对端发送时必须 `setPackage("com.lsl.lemonhall.fcitx5")`

## 4. 需求追溯（Req -> 实现）
- `REQ-IME-001`：接收开放 Intent。
- `REQ-IME-002`：解析并过滤 profile 集合。
- `REQ-IME-003`：5 秒限流。
- `REQ-IME-004`：仅在 `shouldReload=true` 时触发单次重载。
- `REQ-IME-005`：静默失败策略（不打断输入）。
- `REQ-IME-006`：包名与目标路由与上游可并存。

## 5. 设计细节

### 5.1 包名迁移
- 修改 `app/build.gradle.kts`：
  - `applicationId = "com.lsl.lemonhall.fcitx5"`
- 保持 `namespace` 与 Kotlin 包路径不变，降低重构风险。

### 5.2 接收组件
新增：`ProjectMetaIntentReceiver`（`app/src/main/java/org/fcitx/fcitx5/android/projectdict/`）。

职责：
1. 收到广播后先过限流器。
2. 取 `meta_json`（空/超限直接返回）。
3. 解析 profile。
4. `applySelection`。
5. 若 `shouldReload`，临时连接 `FcitxDaemon` 执行 `reloadPinyinDict()`。

### 5.3 限流策略（5 秒）
新增：`ProjectMetaIntentRateLimiter`。

规则：
1. 全局窗口 5000ms。
2. 窗口内新 Intent 一律丢弃，不排队。
3. 时钟使用 `SystemClock.elapsedRealtime()`。
4. 并发安全：`AtomicLong` CAS。

### 5.4 JSON 解析策略
新增：`ProjectMetaParser`。

规则：
1. 允许字段缺失，缺失即回退。
2. profile 仅接受字符串，空白字符串剔除。
3. 去重后再与 catalog 求交集。
4. 不识别字段忽略。
5. 解析失败直接返回空结果并由上层静默处理。

### 5.5 激活与重载策略
1. `targetProfiles = parsedProfiles`。
2. 由 `ProfileDictionaryActivationManager` 自动补 `base`。
3. `activation.shouldReload == true` 才执行重载。
4. 重载失败仅日志，不影响后续输入。

## 6. 文件改动计划

### Create
1. `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaProtocol.kt`
2. `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaParser.kt`
3. `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentRateLimiter.kt`
4. `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentReceiver.kt`
5. `app/src/test/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaParserTest.kt`
6. `app/src/test/java/org/fcitx/fcitx5/android/projectdict/ProjectMetaIntentRateLimiterTest.kt`

### Modify
1. `app/build.gradle.kts`
2. `app/src/main/AndroidManifest.xml`
3. `docs/init.md`
4. `docs/projectdict-manual-test.md`

## 7. 执行任务（可直接开工）

### Task 1: 包名迁移
1. 修改 `applicationId`。
2. 构建 debug。
3. 设备验证可与上游并存。

### Task 2: 协议与解析器
1. 定义 action/extra 常量。
2. 实现 parser（dict_profiles -> tags）。
3. 增加 parser 单测。

### Task 3: 限流器
1. 实现 `tryAcquire()`。
2. 增加首次放行/窗口拒绝/窗口恢复单测。

### Task 4: Receiver 接入
1. 注册 exported receiver + intent-filter。
2. 接入限流、读取 payload、调用 parser。
3. applySelection + 条件重载。
4. 异常全静默。

### Task 5: 文档同步
1. `docs/init.md` 改为 `meta_json` 协议。
2. 更新 Phase 3.3 当前策略描述。
3. 在手工测试文档新增限流场景。

## 8. 验证计划

### 8.1 单测
```bash
./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*" --console=plain
```

### 8.2 构建
```bash
./scripts/gradle-dev.sh --fast --assemble
```

### 8.3 手工验证
1. 发送合法 meta，确认 profile 切换生效。
2. 连续 1 秒内发送 3 次，确认仅首条生效。
3. 发送非法 JSON，确认无崩溃无提示。
4. 发送 base-only，确认回退成功。

## 9. DoD（硬性）
1. 合法 meta 在 1 次广播后完成 profile 切换。
2. 5 秒窗口内重复广播不触发重复处理。
3. 非法 payload 静默丢弃。
4. 与上游 Fcitx 共存安装不冲突。
5. ProjectDict 相关 JVM 单测全绿。

## 10. 风险与后续
1. 开放 Intent 可能被滥用；本轮以“限流+静默+上限”防御。
2. 若出现滥用，再升级为权限或签名方案。
3. 后续可补“最近一次应用状态”调试页，提升可观测性。
