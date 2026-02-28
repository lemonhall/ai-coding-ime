# 上游 Profile 词库人工测试指引（Phase 3.5 / Slice 1）

## 1. 测试目标

验证 Slice 1 的三个核心交付是否可用：
- UI 可进入并可启停 profile 词典
- profile 词典在启动后即 ready，可应用、可重载生效
- 示例词典能稳定复现“温和强调”效果

## 2. 前置条件

1. 使用包含 `Phase 3.5 / Slice 1` 代码的 APK。
2. 手机已启用 `Fcitx5 for Android`。
3. 可进入输入测试页面（Termux/记事本/聊天输入框均可）。
4. APK 已按设计预置 profile 词典资产（无需手工导入）。

## 3. 测试数据

建议首轮验证这 3 份预置词典：
- `profile.base.txt`
- `profile.frontend.txt`
- `profile.frontend.react.txt`

可选扩展验证：
- `profile.backend.java.txt`
- `profile.backend.go.txt`
- `profile.backend.rust.txt`
- `profile.app.android.txt`
- `profile.business.crm.txt`
- `profile.business.erp.txt`

## 4. 测试场景

### 场景 1：进入 profile UI 页面

操作：
1. 打开输入法设置。
2. 进入 `Project Dictionary -> Context Profiles`（或实现中最终命名的 profile 入口）。

期望：
- 页面可正常打开，无崩溃。
- 页面显示 profile 列表/空状态指引。
- 页面存在“应用并重载”入口（按钮或等效动作）。

---

### 场景 2：冷启动后检查预置词典已就绪

操作：
1. 彻底关闭 App 后重新启动（冷启动）。
2. 进入拼音词典管理页或 profile 专属 UI。

期望：
- 无需任何导入操作，profile 词典已存在并可见。
- 至少可看到 `base/frontend/react` 三组词典。

---

### 场景 3：profile UI 显示预置词典状态

操作：
1. 回到 profile UI 页面。

期望：
- 列表能识别并显示预置 profile（至少 base/frontend/react）。
- 每个 profile 有独立启停开关。

---

### 场景 4：仅启用 base

操作：
1. 在 profile UI 中设置：仅 `base` 开启，其他全部关闭。
2. 点击“应用并重载”。
3. 输入：`bianyi`、`gouzi`。

期望：
- `bianyi` 可召回“编译缓存”（base 词）。
- `gouzi` 不应优先召回“自定义钩子”（react 词）。

---

### 场景 5：启用 frontend + react

操作：
1. 开启 `frontend`、`react`（及 base）。
2. 点击“应用并重载”。
3. 输入：`zujian`、`gouzi`。

期望：
- `zujian` 可看到“组件复用”或“函数组件”明显前移。
- `gouzi` 可召回“自定义钩子”。

---

### 场景 6：关闭 react，仅保留 frontend

操作：
1. 关闭 `react`，保留 `frontend` + `base`。
2. 应用并重载。
3. 输入：`gouzi`、`zujian`。

期望：
- `gouzi` 的 react 特定词前移效果消失或显著减弱。
- `zujian` 的 frontend 通用词仍可前移。

---

### 场景 7：切换 no-op

操作：
1. 当前已是 `base + frontend`。
2. 不改任何开关，直接再次点击“应用并重载”。

期望：
- 不应重复执行重载（或有 no-op 提示）。
- 页面无卡顿、无异常。

---

### 场景 8：快速切换稳定性

操作：
1. 在 5 秒内连续切换 3 次组合：
   - `base`
   - `base + frontend`
   - `base + frontend + react`
2. 每次都点击应用。

期望：
- 无崩溃。
- 最终状态与最后一次设置一致。
- 候选行为与最终状态一致。

---

### 场景 9：空词典/缺词典容错

操作：
1. 在开发调试环境下，手动移除部分预置 profile 词典文件（模拟资产缺失）。
2. 打开 profile UI。

期望：
- 页面显示空状态引导（如“预置词典缺失，请检查安装包资产”）。
- 应用操作不会导致崩溃。

---

### 场景 10：与现有 ProjectDict 共存

操作：
1. 保持 profile 模式启用。
2. 再加载一份现有 `.ime/dict.tsv` 项目词库（ProjectDict）。
3. 输入一个 ProjectDict 高权重词与一个 profile 词。

期望：
- 两条链路都可工作，无互相覆盖导致的异常。
- 若实现了模式开关，行为符合开关定义。

---

### 场景 11：重导入内置词库

操作：
1. 在 `Context Profiles` 页面点击“重导入内置词库”。
2. 等待提示后输入：`kesugongdan`。

期望：
- 页面提示重导入成功并完成一次重载。
- 若本地 profile 被历史版本污染，重导入后应恢复到内置 txt 定义。
- `kesugongdan` 可稳定召回“客诉工单”（开启 `business.crm` 后验证）。

## 5. 验收结论模板

- 通过场景数：`x/11`
- 阻塞问题：
  - `P0:`
  - `P1:`
- 建议修正：
- 最终结论：`可进入 Slice 2 / 不可进入 Slice 2`

## 6. 备注

- 本文档聚焦 `Phase 3.5 / Slice 1`，不覆盖 SSH 联动安全链路（Phase 3.2/3.3）。
- 若 UI 最终命名与本文不同，以实现名称为准，但能力要求不变。
