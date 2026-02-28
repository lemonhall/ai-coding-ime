# v2 上游词库分层激活（Context-Aware Profiles）设计文档

## 文档关系
- 上位点火文档：[`docs/init.md`](../init.md)
- 对应阶段：`Phase 3.5 上游词库分层激活（Context-Aware Profiles）`
- 当前状态：`Slice 1 Implemented (2026-02-28)`，自动 context 激活待施工（Slice 2）
- 范围说明：本文是 Slice 1 设计基线；当前领域矩阵扩展请以 `docs/profile-dict-domain-checklist.md`（`D01-D74`）为准

## 1. 背景与问题
当前 ProjectDict 通过候选前插方式增强项目词召回，命中能力强，但在日常输入中容易显得“过于强势”。本阶段目标不是继续增强前插策略，而是回到上游已有能力：使用拼音词典本身的排序机制（`cost`）来做更自然、更温和的词汇引导。

核心观察：
- 上游已具备词典启停与重载机制（`.dict` / `.dict.disable` + `reloadPinyinDict`）。
- 重复加同词不会按“次数”叠加提权，主要应通过 `cost` 与词典分层控制。
- 全量加载所有领域词会提高干扰；应按项目上下文只激活一小部分词典。

因此，定义“全局词库仓库 + 按 context 激活 profile 子集”的新路径。

## 2. 目标与非目标
### 2.1 Goal
- 在不改 `lib/*`、`plugin/*` 的前提下，实现“按项目上下文自动激活词库 profile”。
- 保持输入自然度：减少无关词干扰，降低 ProjectDict 前插依赖。
- 词汇强调通过词典 `cost` 完成，避免“重复加词”这种不可控策略。

### 2.2 Non-Goal
- 本阶段不改 libime/fcitx5-chinese-addons 排序算法。
- 本阶段不做云端词库同步。
- 本阶段不废弃现有 ProjectDict 功能（仅增加可选的温和路径）。

## 3. 范围
### 3.1 In Scope
- Slice 1 提供可见 UI：可进入页面手动启停 profile 词典并触发重载。
- 词库 profile 命名规范与目录约定。
- 上下文到 profile 的映射规则。
- 词典差量启停与单次重载流程。
- 提供内置 profile 示例词典资产（App 启动即 ready，无需手工导入）。
- 提供独立人工测试指引，覆盖 UI、启停、重载与效果验证。
- 失败回退、日志、去抖与上限策略。
- 对应测试与验收标准。

### 3.2 Out of Scope
- SSH 终端联动安全链路完整落地（依赖 Phase 3.2/3.3）。
- 复杂策略编辑器（本阶段仅做最小可用入口，不做可视化规则编排）。
- GitHub 承载/云端同步词典能力（后续阶段再做）。

## 4. 术语
- `Profile`：按技术/业务场景划分的词库分组（如 `frontend`、`react`、`android`、`crm`）。
- `Profile Dictionary`：某个 profile 对应的 libime `.dict` 文件。
- `Active Set`：当前启用的 profile 集合。
- `Context`：项目元信息（例如 `.ime/meta.json` 中的 tags / dict_profiles）。

## 5. 复用现有能力（不新增底层能力）
- 词典启停：通过文件后缀切换 `.dict <-> .dict.disable`（已有 `LibIMEDictionary.enable/disable`）。
- 词典重载：已有 `reloadPinyinDict` 调用链。
- 词典加载：引擎已支持 built-in + extra dictionaries，且可按启停状态加载。

本设计只在 Kotlin/app 层编排“何时启用哪些词典”，不改底层匹配逻辑。

## 6. 架构设计
### 6.1 组件
1. `ProfileCatalog`
- 维护 profile 与词典文件名映射。
- 维护 profile 默认优先级、互斥关系（可选）与建议激活上限。

2. `ContextResolver`
- 输入：项目 context（meta tags / dict_profiles / 手动选择）。
- 输出：目标 profile 集合（如 `{base, frontend, react, crm}`）。

3. `DictionaryActivationManager`
- 读取当前启用词典集合。
- 计算与目标集合差异（enable/disable diff）。
- 批量执行启停操作并触发一次 `reloadPinyinDict`。

4. `ActivationPolicy`
- 约束策略：`base` 永远启用、最大激活数量、切换去抖、冷却窗口。

### 6.2 数据流
```text
Project Context(meta/tags/manual)
  -> ContextResolver
  -> target profiles
  -> DictionaryActivationManager(diff current vs target)
  -> enable/disable profile dict files
  -> single reloadPinyinDict()
  -> fcitx upstream ranking with cost
```

## 7. 数据与命名约定
### 7.1 词典文件命名
建议放在 `data/pinyin/dictionaries/`，命名为：
- `profile.base.dict`
- `profile.frontend.dict`
- `profile.frontend.react.dict`
- `profile.backend.java.dict`
- `profile.backend.go.dict`
- `profile.backend.rust.dict`
- `profile.app.android.dict`
- `profile.business.crm.dict`
- `profile.business.erp.dict`

禁用态使用现有后缀：`*.dict.disable`。

### 7.2 Slice 1 预置资产约定（启动即 ready）
- 词典源文件维护在：`docs/samples/profile-dictionaries/*.txt`。
- 运行时目标资产路径：`app/src/main/assets/usr/share/fcitx5/pinyin/dictionaries/`。
- 默认状态建议：
  - `profile.base` 默认启用；
  - 其他 profile 默认禁用（以 `.dict.disable` 形式存在或等效启停状态）。
- 验收口径：首次安装并启动后，profile 词典无需导入即可在 UI 中可见并可启停。

### 7.3 Context 协议建议（兼容扩展）
在 `.ime/meta.json` 增加可选字段（不破坏现有字段）：
```json
{
  "version": 1,
  "project": "example",
  "dict_profiles": ["frontend", "react", "crm"]
}
```

若 `dict_profiles` 缺失：
- 回退到 `tags`（若存在）。
- 再回退到 `base`。

## 8. 激活算法（核心）
```kotlin
target = setOf("base") + resolveProfiles(context)
target = applyPolicy(target) // 上限、黑白名单、互斥

current = loadEnabledProfiles()
if (target == current) return NoOp

toEnable = target - current
toDisable = current - target

batchEnable(toEnable)
batchDisable(toDisable)
reloadPinyinDictOnce()
persistLastApplied(target, contextHash)
```

策略细节：
- 去抖：短时间多次 context 变化时，仅保留最后一次（建议 1-2 秒）。
- 冷却：两次成功重载至少间隔 N 秒（建议 3 秒）。
- 上限：自动模式最多激活 `base + 3` 个 profile，避免词库过宽。

## 9. 排序策略（cost 使用规范）
- 通过 `cost` 做轻量强调，不使用重复条目提权。
- 建议初始区间（可调）：
  - `base`：`0.00 ~ 0.08`
  - 领域 profile：`0.08 ~ 0.20`
  - 强提示词：`0.20 ~ 0.35`（谨慎使用）
- 不建议大幅度正值，否则会重新出现“过强干预”。

## 10. 与现有 ProjectDict 的关系
- ProjectDict 保留，但新增“上游 profile 模式”后，默认推荐该模式。
- 候选前插路径建议可配置开关：`off / soft / on`。
- 当 profile 模式稳定后，可把前插策略降为应急能力（而非默认）。

## 11. 错误处理与可观测性
### 11.1 失败处理
- 任一词典启停失败：记录错误并继续处理剩余词典，最后统一重载一次。
- 重载失败：保留当前文件状态并上报日志，下一次 context 变化时重试收敛。

### 11.2 日志与指标
- 日志字段：`contextHash`、`targetProfiles`、`toEnable`、`toDisable`、`reloadDurationMs`、`result`。
- 关键指标：
  - `profile_switch_count`
  - `profile_switch_noop_count`
  - `profile_reload_fail_count`
  - `profile_active_size`

## 12. 测试与验收
### 12.1 单元测试
- `ContextResolverTest`：context 到 profile 解析正确，回退路径正确。
- `DictionaryActivationManagerTest`：diff 正确、启停顺序正确、单次重载保证。
- `ActivationPolicyTest`：上限、去抖、冷却策略正确。

### 12.2 集成/手工回归
- 启动后直接可见预置 profile 词典（无手工导入步骤）。
- 在不同 context 下切换 profile，验证候选干扰显著下降。
- 快速切换项目（连续 context 变化）时，确保不会频繁重载卡顿。
- context 缺失/非法时，稳定回退到 `base`。
- 人工测试脚本见：`docs/profile-dict-manual-test.md`。

### 12.3 验收标准（DoD）
- A1：自动模式下，active profile 数不超过策略上限。
- A2：一次 context 切换最多触发一次重载（去抖后）。
- A3：无 context 时仅 `base` 生效，输入行为稳定。
- A4：可复现地通过 `cost` 调整实现“温和前移”，无重复候选副作用。

## 13. 分步落地建议（供下个会话施工）
1. `Slice 1`（必做，可验证）
   - UI：新增 profile 词典启停入口（最小可用），支持手动启停与“应用并重载”。
   - Core：差量启停 + 单次重载（不接自动 context）。
   - Assets：将首批示例词典预置到运行时资产路径，保证首次启动即 ready。
   - QA：新增手工测试文档（`docs/profile-dict-manual-test.md`）并可按文档跑通。
2. `Slice 2`：接入 `meta.json.dict_profiles` 自动激活。
3. `Slice 3`：接入 SSH 终端 context 通道（复用 Phase 3.2/3.3 安全约束）。
4. `Slice 4`：新增模式开关，默认优先 profile 模式，前插改为可选。

## 14. Slice 1 UI 约束（补充）
- 页面位置（建议）：`设置 -> Project Dictionary -> Context Profiles`。
- 页面能力（必须）：
  - 展示 profile 列表（名称、词典文件、启用状态）。
  - 支持单项开关与批量“仅保留 base”。
  - 支持“应用并重载”按钮，且一次点击最多触发一次重载。
- 可视反馈（必须）：
  - 应用成功提示：显示启用数量与耗时。
  - 应用失败提示：显示失败词典及错误原因摘要。
- 兼容要求：
  - 不破坏现有 `PinyinDictionaryFragment` 行为；可复用组件但需提供更聚焦入口。
  - 若预置词典缺失/损坏，页面可进入且给出空状态指引。

## 15. 风险与缓解
- 风险：profile 粒度太细导致维护成本高。
  - 缓解：先保留 8-12 个高价值 profile，不追求全覆盖。
- 风险：词典切换过频导致体验抖动。
  - 缓解：去抖 + 冷却 + no-op 快速返回。
- 风险：cost 调整不当导致局部词霸榜。
  - 缓解：约束 cost 区间并做 A/B 词典回归。

## 16. 当前结论
该方案技术可行，且与现有代码路径兼容。它能在“保留上游能力”的前提下，把词汇强调从“强插候选”转为“温和排序引导”，符合当前目标。

## 17. 后续扩展（非 Slice 1）
- 词典云端承载与同步：后续可评估使用 GitHub 仓库作为 profile 词典分发源。
- 同步策略建议：签名校验 + 版本戳 + 增量下载 + 回滚缓存。
