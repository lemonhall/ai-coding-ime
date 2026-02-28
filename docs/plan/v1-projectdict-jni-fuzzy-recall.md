# v1 ProjectDict JNI 容错召回施工计划

## 文档关系
- 上位点火文档：[`docs/init.md`](../init.md)
- 对应阶段：`Phase 3.4 容错召回增强：复用 JNI/libime 能力`
- 手工回归参考：[`docs/projectdict-manual-test.md`](../projectdict-manual-test.md)

## 1. Goal
在不改 `lib/*` 与 `plugin/*` 源码的前提下，复用 app 层 JNI + libime 现有纠错能力，让 ProjectDict 对误触拼音输入也可召回（如 `hyi` / `huu` / `hyigun` / `huugun`）。

## 2. Scope
### In Scope
- 在 app JNI 层新增“拼音容错匹配”桥接接口。
- 在 Kotlin `ProjectDictManager` 接入该接口，作为严格匹配后的补充召回通道。
- 为容错召回建立排序降权策略，确保精确命中优先。
- 补充单元测试与最小手工回归清单。

### Out of Scope
- 不修改 `libime` / `fcitx5-chinese-addons` / `plugin/*` 源码。
- 不改变现有 ProjectDict 的加载协议（`.ime/dict.tsv`）与 UI 交互路径。
- 不在本阶段做跨词语义纠错、上下文语义重排、云端纠错。

## 3. 方案总览
1. Kotlin 侧把 `input` 与候选词条的拼音键（按现有 token 缓存）批量传入 JNI。
2. JNI 侧调用 libime 的纠错配置（`Qwerty + Fuzzy`）判断“输入前缀是否可映射到目标拼音前缀”。
3. JNI 返回每个词条的匹配状态与纠错代价（或纠错标志）。
4. Kotlin 合并排序：`精确匹配 > 纠错匹配`，同组内按 `weight` 降序。

## 4. 文件级改动计划
- `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectDictNative.kt`
  - 新增 JNI 门面，提供批量匹配接口。
- `app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectDictManager.kt`
  - 接入 JNI 匹配结果，扩展 `query()` 召回逻辑与排序逻辑。
- `app/src/main/cpp/native-lib.cpp`
  - 新增 JNIEXPORT 方法，调用 libime 的 correction/fuzzy 能力。
- `app/src/test/java/org/fcitx/fcitx5/android/projectdict/ProjectDictManagerTest.kt`
  - 新增误触输入召回用例与优先级/误召回保护用例。

## 5. 实施步骤（不开工版）
1. 定义 JNI 数据契约（输入、批量候选、返回结构）。
2. 先写 Kotlin 侧测试（红）：误触输入应召回，且不压过精确命中。
3. 新增 C++ JNI 桥接实现（最小可用，先支持全拼路径）。
4. 接入 `ProjectDictManager.query()`（绿）：严格匹配 + JNI 补召回 + 降权排序。
5. 补充边界测试：空输入、非拼音字符、超长输入、词条过多。
6. 跑回归并记录证据（测试命令与人工场景）。

## 6. 验收标准
- `hyi`、`huu`、`hyigun`、`huugun` 能召回目标项目词 `[P]`（词库包含“回滚到上一个版本”时）。
- 精确输入（`huigun`）的召回排序必须优于纠错输入。
- 未加载词库时行为与当前版本一致。
- 性能可控：常见词库规模（<=1000 条）下，不出现肉眼可见候选卡顿。

## 7. 验证与证据
### 自动化
- `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.ProjectDictManagerTest"`

### 手工
- 依据 [`docs/projectdict-manual-test.md`](../projectdict-manual-test.md) 新增“误触召回”场景并执行。

## 8. 风险与缓解
- 误召回过多：通过“纠错代价降权 + 候选上限”控制。
- JNI 往返开销：采用批量接口，避免 per-entry JNI 调用。
- 规则偏差：严格对齐 libime 已有 correction/fuzzy 配置，不自造规则。

## 9. 里程碑
- M1：数据契约与测试红用例通过编译。
- M2：JNI 桥接完成并通过核心误触召回测试。
- M3：排序稳定、回归通过、文档与测试证据齐全。

## 10. 状态
- 当前状态：`Planned (未开工)`
- 入口文档：[`docs/init.md`](../init.md)
