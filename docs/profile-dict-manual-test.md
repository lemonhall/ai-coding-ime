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

## 7. D22 移动端开发（`app.android`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + app.android`
- 边界对照：`base`（关闭 `app.android`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `qiantaifuwu` | `前台服务` | `base + app.android` | Top3 出现目标词 |
| `lengqidongyouhua` | `冷启动优化` | `base + app.android` | Top3 出现目标词，排序稳定 |
| `quanxiantanchuang` | `权限弹窗` | `base + app.android` | Top5 出现目标词，不被无关词压制 |
| `neicunxielou` | `内存泄漏` | `base + app.android` | Top5 出现目标词 |
| `huidufabu` | `灰度发布` | `base + app.android` | Top5 出现目标词 |
| `zhediepingshipei` | `折叠屏适配` | `base + app.android` | Top5 出现目标词 |
| `qiantaifuwu` | `前台服务` | `base` | 目标词较 `base + app.android` 场景明显后移（不应强前移） |
| `yilaizhuru` | `依赖注入` | `base` | 若命中，排序应弱于 `base + app.android` 场景 |

## 8. D08 Web前端（`frontend`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + frontend`
- 边界对照：`base`（关闭 `frontend`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `zujianfuyong` | `组件复用` | `base + frontend` | Top3 出现目标词 |
| `shoupingyouhua` | `首屏优化` | `base + frontend` | Top3 出现目标词，排序稳定 |
| `xiangyingshibuju` | `响应式布局` | `base + frontend` | Top5 出现目标词 |
| `fuwuduanxuanran` | `服务端渲染` | `base + frontend` | Top5 出现目标词 |
| `xunigundong` | `虚拟滚动` | `base + frontend` | Top5 出现目标词，不被无关词压制 |
| `cuowubianjie` | `错误边界` | `base + frontend` | Top5 出现目标词 |
| `zujianfuyong` | `组件复用` | `base` | 目标词较 `base + frontend` 场景明显后移（不应强前移） |
| `fuwugongzuoxiancheng` | `服务工作线程` | `base` | 若命中，排序应弱于 `base + frontend` 场景 |

## 9. D07 Web后端 / API设计（`network.web-backend-api`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + network.web-backend-api`
- 边界对照：`base`（关闭 `network.web-backend-api`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jiekouwangguan` | `接口网关` | `base + network.web-backend-api` | Top3 出现目标词 |
| `midengjian` | `幂等键` | `base + network.web-backend-api` | Top5 出现目标词，排序稳定 |
| `xianliujiangji` | `限流降级` | `base + network.web-backend-api` | Top5 出现目标词，不被无关词压制 |
| `lianluzhuizong` | `链路追踪` | `base + network.web-backend-api` | Top5 出现目标词 |
| `quanxianjianquan` | `权限鉴权` | `base + network.web-backend-api` | Top5 出现目标词 |
| `rongzaiqiehuan` | `容灾切换` | `base + network.web-backend-api` | Top5 出现目标词 |
| `jiekouwangguan` | `接口网关` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `jiekouxianliu` | `接口限流` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 10. D18 DevOps / CI/CD / SRE（`engineering.devops-sre`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + engineering.devops-sre`
- 边界对照：`base`（关闭 `engineering.devops-sre`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `chixujicheng` | `持续集成` | `base + engineering.devops-sre` | Top3 出现目标词 |
| `liushuixianbianpai` | `流水线编排` | `base + engineering.devops-sre` | Top5 出现目标词，排序稳定 |
| `lianluzhuizong` | `链路追踪` | `base + engineering.devops-sre` | Top5 出现目标词，不被无关词压制 |
| `rongzaiqiehuan` | `容灾切换` | `base + engineering.devops-sre` | Top5 出现目标词 |
| `hundungongcheng` | `混沌工程` | `base + engineering.devops-sre` | Top5 出现目标词 |
| `jichusheshijidaima` | `基础设施即代码` | `base + engineering.devops-sre` | Top5 出现目标词 |
| `chixujicheng` | `持续集成` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `fuwujiankong` | `服务监控` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 11. D20 测试工程（`engineering.testing`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + engineering.testing`
- 边界对照：`base`（关闭 `engineering.testing`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `danyuanceshi` | `单元测试` | `base + engineering.testing` | Top3 出现目标词 |
| `huiguiceshi` | `回归测试` | `base + engineering.testing` | Top3 出现目标词，排序稳定 |
| `jiekouceshi` | `接口测试` | `base + engineering.testing` | Top5 出现目标词，不被无关词压制 |
| `quexiangenzong` | `缺陷跟踪` | `base + engineering.testing` | Top5 出现目标词 |
| `daimafugailv` | `代码覆盖率` | `base + engineering.testing` | Top5 出现目标词 |
| `yalicieshi` | `压力测试` | `base + engineering.testing` | Top5 出现目标词 |
| `danyuanceshi` | `单元测试` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `quexiangenzong` | `缺陷跟踪` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 12. D19 版本控制 / 协作（`engineering.vcs-collaboration`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + engineering.vcs-collaboration`
- 边界对照：`base`（关闭 `engineering.vcs-collaboration`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `banbenkongzhi` | `版本控制` | `base + engineering.vcs-collaboration` | Top3 出现目标词 |
| `fenzhicelue` | `分支策略` | `base + engineering.vcs-collaboration` | Top3 出现目标词，排序稳定 |
| `laquqingqiu` | `拉取请求` | `base + engineering.vcs-collaboration` | Top5 出现目标词，不被无关词压制 |
| `hebingchongtu` | `合并冲突` | `base + engineering.vcs-collaboration` | Top5 出现目标词 |
| `daimapingshen` | `代码评审` | `base + engineering.vcs-collaboration` | Top5 出现目标词 |
| `rebasebianji` | `rebase变基` | `base + engineering.vcs-collaboration` | Top5 出现目标词 |
| `tijiaomenjin` | `提交门禁` | `base + engineering.vcs-collaboration` | Top5 出现目标词 |
| `hebingqingqiu` | `合并请求` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `banbenkongzhi` | `版本控制` | `base` | 若命中，排序应弱于开启领域 profile 时 |
