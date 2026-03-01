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
- `profile.client.desktop-cross-platform.txt`
- `profile.client.game-dev.txt`
- `profile.network.web-backend-api.txt`
- `profile.engineering.devops-sre.txt`
- `profile.engineering.testing.txt`
- `profile.engineering.package-build.txt`
- `profile.engineering.vcs-collaboration.txt`
- `profile.domain.editor-ide-tooling.txt`
- `profile.business.crm.txt`
- `profile.business.erp.txt`
- `profile.business.hrm.txt`

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

## 13. D21 包管理 / 构建系统（`engineering.package-build`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + engineering.package-build`
- 边界对照：`base`（关闭 `engineering.package-build`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `baoguanli` | `包管理` | `base + engineering.package-build` | Top3 出现目标词 |
| `goujianxitong` | `构建系统` | `base + engineering.package-build` | Top3 出现目标词，排序稳定 |
| `zenglianggoujian` | `增量构建` | `base + engineering.package-build` | Top5 出现目标词 |
| `yilaisuoding` | `依赖锁定` | `base + engineering.package-build` | Top5 出现目标词，不被无关词压制 |
| `goujianhuancun` | `构建缓存` | `base + engineering.package-build` | Top5 出现目标词 |
| `gongjiancangku` | `工件仓库` | `base + engineering.package-build` | Top5 出现目标词 |
| `gongyingliananquan` | `供应链安全` | `base + engineering.package-build` | Top5 出现目标词 |
| `baoguanli` | `包管理` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `goujianxitong` | `构建系统` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 14. D23 桌面应用 / 跨平台GUI（`client.desktop-cross-platform`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + client.desktop-cross-platform`
- 边界对照：`base`（关闭 `client.desktop-cross-platform`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `duochuangkou` | `多窗口` | `base + client.desktop-cross-platform` | Top3 出现目标词 |
| `xitongtuopan` | `系统托盘` | `base + client.desktop-cross-platform` | Top3 出现目标词，排序稳定 |
| `gaofenpingshipei` | `高分屏适配` | `base + client.desktop-cross-platform` | Top5 出现目标词，不被无关词压制 |
| `zidonggengxin` | `自动更新` | `base + client.desktop-cross-platform` | Top5 出现目标词 |
| `chajianshichang` | `插件市场` | `base + client.desktop-cross-platform` | Top5 出现目标词 |
| `danshilisuo` | `单实例锁` | `base + client.desktop-cross-platform` | Top5 出现目标词 |
| `tuozhuaishangchuan` | `拖拽上传` | `base + client.desktop-cross-platform` | Top5 出现目标词 |
| `duochuangkou` | `多窗口` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `xitongtuopan` | `系统托盘` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 15. D48 编辑器 / IDE / 开发者工具（`domain.editor-ide-tooling`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + domain.editor-ide-tooling`
- 边界对照：`base`（关闭 `domain.editor-ide-tooling`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `daimabianji` | `代码编辑` | `base + domain.editor-ide-tooling` | Top3 出现目标词 |
| `minglingmianban` | `命令面板` | `base + domain.editor-ide-tooling` | Top5 出现目标词，排序稳定 |
| `yuyanfuwuqi` | `语言服务器` | `base + domain.editor-ide-tooling` | Top5 出现目标词，不被无关词压制 |
| `duandiantiaoshi` | `断点调试` | `base + domain.editor-ide-tooling` | Top5 出现目标词 |
| `chongmingmingfuhao` | `重命名符号` | `base + domain.editor-ide-tooling` | Top5 出现目标词 |
| `chajianshichang` | `插件市场` | `base + domain.editor-ide-tooling` | Top5 出现目标词 |
| `daimabianji` | `代码编辑` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `yuyanfuwuqi` | `语言服务器` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 16. D24 游戏开发（`client.game-dev`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + client.game-dev`
- 边界对照：`base`（关闭 `client.game-dev`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `youxiyinqing` | `游戏引擎` | `base + client.game-dev` | Top3 出现目标词 |
| `guanqiasheji` | `关卡设计` | `base + client.game-dev` | Top5 出现目标词，排序稳定 |
| `zhensuobu` | `帧锁步` | `base + client.game-dev` | Top5 出现目标词，不被无关词压制 |
| `xingweishu` | `行为树` | `base + client.game-dev` | Top5 出现目标词 |
| `neigouxitong` | `内购系统` | `base + client.game-dev` | Top5 出现目标词 |
| `ziyuanregeng` | `资源热更` | `base + client.game-dev` | Top5 出现目标词 |
| `youxiyinqing` | `游戏引擎` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `neigouxitong` | `内购系统` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 17. D53 HRM / 人力资源系统（`business.hrm`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + business.hrm`
- 边界对照：`base`（关闭 `business.hrm`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `renliziyuanxitong` | `人力资源系统` | `base + business.hrm` | Top3 出现目标词 |
| `zhaopinxuqiu` | `招聘需求` | `base + business.hrm` | Top5 出现目标词，排序稳定 |
| `zhuanzhengshenpi` | `转正审批` | `base + business.hrm` | Top5 出现目标词，不被无关词压制 |
| `xinzifa` | `薪资发放` | `base + business.hrm` | Top5 出现目标词 |
| `jixiaoguanli` | `绩效管理` | `base + business.hrm` | Top5 出现目标词 |
| `qingjiashenpi` | `请假审批` | `base + business.hrm` | Top5 出现目标词 |
| `kpi` | `KPI` | `base + business.hrm` | Top5 出现目标词，并可与“关键结果/绩效目标”共现 |
| `renliziyuanxitong` | `人力资源系统` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `xinzifa` | `薪资发放` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 18. D52 CRM / 客户关系管理（`business.crm`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + business.crm`
- 边界对照：`base`（关闭 `business.crm`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `kehuxiansuo` | `客户线索` | `base + business.crm` | Top3 出现目标词 |
| `xiansuozhuanhua` | `线索转化` | `base + business.crm` | Top5 出现目标词，排序稳定 |
| `shangjigenjin` | `商机跟进` | `base + business.crm` | Top5 出现目标词，不被无关词压制 |
| `xiaoshouloudou` | `销售漏斗` | `base + business.crm` | Top5 出现目标词 |
| `baojiadan` | `报价单` | `base + business.crm` | Top5 出现目标词 |
| `gongdanliuzhuan` | `工单流转` | `base + business.crm` | Top5 出现目标词 |
| `kehuquanjing` | `客户全景` | `base + business.crm` | Top5 出现目标词 |
| `kesugongdan` | `客诉工单` | `base + business.crm` | Top3 出现目标词，且排序稳定 |
| `kehuxiansuo` | `客户线索` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `shangjigenjin` | `商机跟进` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 19. D51 ERP / 企业资源计划（`business.erp`）专项回归 Query（2026-02-28）

激活组合说明：
- 正向命中：`base + business.erp`
- 边界对照：`base`（关闭 `business.erp`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `qiyeziyuanjihua` | `企业资源计划` | `base + business.erp` | Top3 出现目标词 |
| `caigoudingdan` | `采购订单` | `base + business.erp` | Top3 出现目标词，排序稳定 |
| `kucunzhouzhuan` | `库存周转` | `base + business.erp` | Top5 出现目标词，不被无关词压制 |
| `shengchanpaicheng` | `生产排程` | `base + business.erp` | Top5 出现目标词 |
| `chengbenhesuan` | `成本核算` | `base + business.erp` | Top5 出现目标词 |
| `yingfuzhangkuan` | `应付账款` | `base + business.erp` | Top5 出现目标词 |
| `zhushujuzhili` | `主数据治理` | `base + business.erp` | Top5 出现目标词，且排序稳定 |
| `qiyeziyuanjihua` | `企业资源计划` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `caigouduizhang` | `采购对账` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 20. D17 软件工程 / 架构模式（`engineering.software-architecture`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + engineering.software-architecture`
- 边界对照：`base`（关闭 `engineering.software-architecture`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `weifuwujiagou` | `微服务架构` | `base + engineering.software-architecture` | Top3 出现目标词 |
| `liubianxingjiagou` | `六边形架构` | `base + engineering.software-architecture` | Top5 出现目标词，排序稳定 |
| `xianjieshangxiawen` | `限界上下文` | `base + engineering.software-architecture` | Top5 出现目标词，不被无关词压制 |
| `shijianqudongjiagou` | `事件驱动架构` | `base + engineering.software-architecture` | Top5 出现目标词 |
| `jiaoshazhemoshi` | `绞杀者模式` | `base + engineering.software-architecture` | Top5 出现目标词 |
| `qiyueceshi` | `契约测试` | `base + engineering.software-architecture` | Top5 出现目标词 |
| `jiagoupingshen` | `架构评审` | `base + engineering.software-architecture` | Top5 出现目标词 |
| `huigunyuanan` | `回滚预案` | `base + engineering.software-architecture` | Top5 出现目标词 |
| `weifuwujiagou` | `微服务架构` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `xianjieshangxiawen` | `限界上下文` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 21. D11 关系型数据库（`data.relational-db`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + data.relational-db`
- 边界对照：`base`（关闭 `data.relational-db`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `guanxixingshujuku` | `关系型数据库` | `base + data.relational-db` | Top3 出现目标词 |
| `zhujiansuoyin` | `主键索引` | `base + data.relational-db` | Top3 出现目标词，排序稳定 |
| `shiwubianjie` | `事务边界` | `base + data.relational-db` | Top5 出现目标词 |
| `kechongfudu` | `可重复读` | `base + data.relational-db` | Top5 出现目标词，不被无关词压制 |
| `fuzhiyanchi` | `复制延迟` | `base + data.relational-db` | Top5 出现目标词 |
| `zaixianddl` | `在线DDL` | `base + data.relational-db` | Top5 出现目标词 |
| `manchaxunrizhi` | `慢查询日志` | `base + data.relational-db` | Top5 出现目标词，排序稳定 |
| `zhujiansuoyin` | `主键索引` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
| `fuzhiyanchi` | `复制延迟` | `base` | 若命中，排序应弱于开启领域 profile 时 |

## 22. D39 云计算 / IaaS（`infra.cloud-iaas`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + infra.cloud-iaas`
- 边界对照：`base`（关闭 `infra.cloud-iaas`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `yunjisuan` | `云计算` | `base + infra.cloud-iaas` | Top3 出现目标词 |
| `jichusheshijifuwu` | `基础设施即服务` | `base + infra.cloud-iaas` | Top5 出现目标词，排序稳定 |
| `xuniji` | `虚拟机` | `base + infra.cloud-iaas` | Top5 出现目标词 |
| `rongzaiqiehuan` | `容灾切换` | `base + infra.cloud-iaas` | Top5 出现目标词，不被无关词压制 |
| `yunpankuaizhao` | `云盘快照` | `base + infra.cloud-iaas` | Top5 出现目标词 |
| `xunisiyouyun` | `虚拟私有云` | `base + infra.cloud-iaas` | Top5 出现目标词 |
| `shenfenyufangwenguanli` | `身份与访问管理` | `base + infra.cloud-iaas` | Top8 出现目标词 |
| `duozuhugeli` | `多租户隔离` | `base + infra.cloud-iaas` | Top8 出现目标词 |
| `xuniji` | `虚拟机` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 23. D40 容器与编排（`infra.containers-orchestration`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + infra.containers-orchestration`
- 边界对照：`base`（关闭 `infra.containers-orchestration`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `rongqibianpai` | `容器编排` | `base + infra.containers-orchestration` | Top3 出现目标词 |
| `gundonggengxin` | `滚动更新` | `base + infra.containers-orchestration` | Top5 出现目标词，排序稳定 |
| `jiqunzidongkuorong` | `集群自动扩容` | `base + infra.containers-orchestration` | Top5 出现目标词 |
| `wanggeyaoce` | `网格遥测` | `base + infra.containers-orchestration` | Top5 出现目标词，不被无关词压制 |
| `shuangxiangjiamirenzheng` | `双向加密认证` | `base + infra.containers-orchestration` | Top5 出现目标词 |
| `rongqijianchadian` | `容器检查点` | `base + infra.containers-orchestration` | Top5 出现目标词 |
| `zidingyikongzhiqi` | `自定义控制器` | `base + infra.containers-orchestration` | Top5 出现目标词 |
| `duozuhugeli` | `多租户隔离` | `base + infra.containers-orchestration` | Top5 出现目标词 |
| `rongqibianpai` | `容器编排` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 24. D41 消息队列 / 事件流（`infra.mq-event-streaming`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + infra.mq-event-streaming`
- 边界对照：`base`（关闭 `infra.mq-event-streaming`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xiaoxiduilie` | `消息队列` | `base + infra.mq-event-streaming` | Top3 出现目标词 |
| `shijianliu` | `事件流` | `base + infra.mq-event-streaming` | Top3 出现目标词，排序稳定 |
| `sixinjiaohuanji` | `死信交换机` | `base + infra.mq-event-streaming` | Top5 出现目标词 |
| `fenquzaijunheng` | `分区再均衡` | `base + infra.mq-event-streaming` | Top5 出现目标词，不被无关词压制 |
| `shiwuhuicha` | `事务回查` | `base + infra.mq-event-streaming` | Top5 出现目标词 |
| `fajianxiangmoshi` | `发件箱模式` | `base + infra.mq-event-streaming` | Top5 出现目标词 |
| `biangengshujubuhuo` | `变更数据捕获` | `base + infra.mq-event-streaming` | Top5 出现目标词 |
| `bianpai` | `编排引擎` | `base + infra.mq-event-streaming` | Top5 出现目标词 |
| `xiaoxiduilie` | `消息队列` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 25. D42 可观测性 / 监控（`infra.observability-monitoring`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + infra.observability-monitoring`
- 边界对照：`base`（关闭 `infra.observability-monitoring`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `keguancexing` | `可观测性` | `base + infra.observability-monitoring` | Top3 出现目标词 |
| `yaocecaiji` | `遥测采集` | `base + infra.observability-monitoring` | Top5 出现目标词，排序稳定 |
| `baifenweiyanchi` | `百分位延迟` | `base + infra.observability-monitoring` | Top5 出现目标词 |
| `jiegouhuarizhi` | `结构化日志` | `base + infra.observability-monitoring` | Top5 出现目标词，不被无关词压制 |
| `lianluzhuizong` | `链路追踪` | `base + infra.observability-monitoring` | Top5 出现目标词 |
| `gaojingyizhi` | `告警抑制` | `base + infra.observability-monitoring` | Top5 出现目标词 |
| `cuowuyusuan` | `错误预算` | `base + infra.observability-monitoring` | Top5 出现目标词 |
| `zhenshiyonghujiankong` | `真实用户监控` | `base + infra.observability-monitoring` | Top5 出现目标词 |
| `keguancexing` | `可观测性` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 26. D43 IaC / 基础设施即代码（`infra.iac`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + infra.iac`
- 边界对照：`base`（关闭 `infra.iac`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jichusheshijidaima` | `基础设施即代码` | `base + infra.iac` | Top3 出现目标词 |
| `shengmingshibushu` | `声明式部署` | `base + infra.iac` | Top5 出现目标词，排序稳定 |
| `zhuangtaisuoding` | `状态锁定` | `base + infra.iac` | Top5 出现目标词 |
| `piaoyijiance` | `漂移检测` | `base + infra.iac` | Top5 出现目标词，不被无关词压制 |
| `mokuaibanbensuoding` | `模块版本锁定` | `base + infra.iac` | Top5 出现目标词 |
| `qiangzhibiaoqiancelue` | `强制标签策略` | `base + infra.iac` | Top5 出现目标词 |
| `xunisiyouyun` | `虚拟私有云` | `base + infra.iac` | Top5 出现目标词 |
| `duojiqunfabu` | `多集群发布` | `base + infra.iac` | Top5 出现目标词 |
| `jichusheshijidaima` | `基础设施即代码` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 27. D01 计算机体系结构 / CPU设计（`hardware.cpu-architecture`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + hardware.cpu-architecture`
- 边界对照：`base`（关闭 `hardware.cpu-architecture`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jisuanjitixijiegou` | `计算机体系结构` | `base + hardware.cpu-architecture` | Top3 出现目标词 |
| `zhilingjijiagou` | `指令集架构` | `base + hardware.cpu-architecture` | Top5 出现目标词，排序稳定 |
| `weitixijiegou` | `微体系结构` | `base + hardware.cpu-architecture` | Top5 出现目标词 |
| `pianshangxitong` | `片上系统` | `base + hardware.cpu-architecture` | Top5 出现目标词，不被无关词压制 |
| `meizhouqizhilingshu` | `每周期指令数` | `base + hardware.cpu-architecture` | Top5 出现目标词 |
| `zhilingjijiagou` | `指令集架构` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 28. D02 嵌入式系统 / IoT（`hardware.embedded-iot`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + hardware.embedded-iot`
- 边界对照：`base`（关闭 `hardware.embedded-iot`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `digonghaochuanganqi` | `低功耗传感器` | `base + hardware.embedded-iot` | Top3 出现目标词 |
| `digonghaowangguan` | `低功耗网关` | `base + hardware.embedded-iot` | Top5 出现目标词，排序稳定 |
| `bianyuanjisuanjiedian` | `边缘计算节点` | `base + hardware.embedded-iot` | Top5 出现目标词 |
| `wuxianchuanganqi` | `无线传感器` | `base + hardware.embedded-iot` | Top5 出现目标词，不被无关词压制 |
| `digonghaoguanlixitong` | `低功耗管理系统` | `base + hardware.embedded-iot` | Top5 出现目标词 |
| `digonghaochuanganqi` | `低功耗传感器` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 29. D03 操作系统内核（`hardware.os-kernel`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + hardware.os-kernel`
- 边界对照：`base`（关闭 `hardware.os-kernel`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `neihe` | `内核` | `base + hardware.os-kernel` | Top3 出现目标词 |
| `xitongdiaoyong` | `系统调用` | `base + hardware.os-kernel` | Top5 出现目标词，排序稳定 |
| `zhongduanxiangliang` | `中断向量` | `base + hardware.os-kernel` | Top5 出现目标词 |
| `shangxiawenqiehuan` | `上下文切换` | `base + hardware.os-kernel` | Top5 出现目标词，不被无关词压制 |
| `quanjumiaoshufubiao` | `全局描述符表` | `base + hardware.os-kernel` | Top5 出现目标词 |
| `xitongdiaoyong` | `系统调用` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 30. D04 驱动与硬件抽象（`hardware.driver-hal`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + hardware.driver-hal`
- 边界对照：`base`（关闭 `hardware.driver-hal`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `qudongchouxiang` | `驱动抽象` | `base + hardware.driver-hal` | Top3 出现目标词 |
| `yingjianchouxiang` | `硬件抽象` | `base + hardware.driver-hal` | Top5 出现目标词，排序稳定 |
| `pingtaishebei` | `平台设备` | `base + hardware.driver-hal` | Top5 出现目标词 |
| `zifushebei` | `字符设备` | `base + hardware.driver-hal` | Top5 出现目标词，不被无关词压制 |
| `shebeibangding` | `设备绑定` | `base + hardware.driver-hal` | Top5 出现目标词 |
| `qudongchouxiang` | `驱动抽象` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 31. D05 FPGA / 硬件描述语言（`hardware.fpga-hdl`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + hardware.fpga-hdl`
- 边界对照：`base`（关闭 `hardware.fpga-hdl`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xianchangkebianchengmenzhenlie` | `现场可编程门阵列` | `base + hardware.fpga-hdl` | Top3 出现目标词 |
| `yingjianmiaoshuyuyan` | `硬件描述语言` | `base + hardware.fpga-hdl` | Top5 出现目标词，排序稳定 |
| `jicunqichuanshuji` | `寄存器传输级` | `base + hardware.fpga-hdl` | Top5 出现目标词 |
| `bujubuxian` | `布局布线` | `base + hardware.fpga-hdl` | Top5 出现目标词，不被无关词压制 |
| `shizhongyujiaocha` | `时钟域交叉` | `base + hardware.fpga-hdl` | Top5 出现目标词 |
| `yingjianmiaoshuyuyan` | `硬件描述语言` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 32. D06 计算机网络 / 协议栈（`network.protocols`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + network.protocols`
- 边界对照：`base`（关闭 `network.protocols`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xieyizhan` | `协议栈` | `base + network.protocols` | Top3 出现目标词 |
| `qicengmoxing` | `七层模型` | `base + network.protocols` | Top5 出现目标词，排序稳定 |
| `wangluoceng` | `网络层` | `base + network.protocols` | Top5 出现目标词 |
| `chuanshuceng` | `传输层` | `base + network.protocols` | Top5 出现目标词，不被无关词压制 |
| `fenpianchongzu` | `分片重组` | `base + network.protocols` | Top5 出现目标词 |
| `xieyizhan` | `协议栈` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 33. D09 网络安全 / 密码学（`network.security-crypto`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + network.security-crypto`
- 边界对照：`base`（关闭 `network.security-crypto`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `wangluoanquan` | `网络安全` | `base + network.security-crypto` | Top3 出现目标词 |
| `mimaxue` | `密码学` | `base + network.security-crypto` | Top5 出现目标词，排序稳定 |
| `duichenjiami` | `对称加密` | `base + network.security-crypto` | Top5 出现目标词 |
| `feiduichenjiami` | `非对称加密` | `base + network.security-crypto` | Top5 出现目标词，不被无关词压制 |
| `duandaoduanjiami` | `端到端加密` | `base + network.security-crypto` | Top5 出现目标词 |
| `wangluoanquan` | `网络安全` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 34. D10 分布式系统（`network.distributed-systems`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + network.distributed-systems`
- 边界对照：`base`（关闭 `network.distributed-systems`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `fenbushixitong` | `分布式系统` | `base + network.distributed-systems` | Top3 出现目标词 |
| `xianxingyizhixing` | `线性一致性` | `base + network.distributed-systems` | Top5 出现目标词，排序稳定 |
| `zuizhongyizhixing` | `最终一致性` | `base + network.distributed-systems` | Top5 出现目标词 |
| `zhucongfuzhi` | `主从复制` | `base + network.distributed-systems` | Top5 出现目标词，不被无关词压制 |
| `fuzhiyanchi` | `复制延迟` | `base + network.distributed-systems` | Top5 出现目标词 |
| `fenbushixitong` | `分布式系统` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 35. D12 NoSQL / NewSQL（`data.nosql-newsql`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + data.nosql-newsql`
- 边界对照：`base`（关闭 `data.nosql-newsql`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `feiguanxishujuku` | `非关系数据库` | `base + data.nosql-newsql` | Top3 出现目标词 |
| `jianzhishujuku` | `键值数据库` | `base + data.nosql-newsql` | Top5 出现目标词，排序稳定 |
| `wendangshujuku` | `文档数据库` | `base + data.nosql-newsql` | Top5 出现目标词 |
| `liezushujuku` | `列族数据库` | `base + data.nosql-newsql` | Top5 出现目标词，不被无关词压制 |
| `yunyuanshengshujuku` | `云原生数据库` | `base + data.nosql-newsql` | Top5 出现目标词 |
| `feiguanxishujuku` | `非关系数据库` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 36. D13 数据工程 / ETL / 大数据（`data.engineering-etl`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + data.engineering-etl`
- 边界对照：`base`（关闭 `data.engineering-etl`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `shujugongcheng` | `数据工程` | `base + data.engineering-etl` | Top3 出现目标词 |
| `shujuguandao` | `数据管道` | `base + data.engineering-etl` | Top5 出现目标词，排序稳定 |
| `shujucaiji` | `数据采集` | `base + data.engineering-etl` | Top5 出现目标词 |
| `biangengbuhu` | `变更捕获` | `base + data.engineering-etl` | Top5 出现目标词，不被无关词压制 |
| `shujutongbu` | `数据同步` | `base + data.engineering-etl` | Top5 出现目标词 |
| `shujugongcheng` | `数据工程` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 37. D26 经典机器学习（`ai.classic-ml`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.classic-ml`
- 边界对照：`base`（关闭 `ai.classic-ml`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jianduxuexi` | `监督学习` | `base + ai.classic-ml` | Top3 出现目标词 |
| `luojihuigui` | `逻辑回归` | `base + ai.classic-ml` | Top3 出现目标词 |
| `tezhenggongcheng` | `特征工程` | `base + ai.classic-ml` | Top5 出现目标词 |
| `jiaochayanzheng` | `交叉验证` | `base + ai.classic-ml` | Top5 出现目标词 |
| `zhuchengfenfenxi` | `主成分分析` | `base + ai.classic-ml` | Top5 出现目标词 |
| `hunxiaojuzhen` | `混淆矩阵` | `base + ai.classic-ml` | Top5 出现目标词 |
| `jianduxuexi` | `监督学习` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 38. D27 深度学习 / 神经网络（`ai.deep-learning`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.deep-learning`
- 边界对照：`base`（关闭 `ai.deep-learning`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `shenduxuexi` | `深度学习` | `base + ai.deep-learning` | Top3 出现目标词 |
| `juanjishenjingwangluo` | `卷积神经网络` | `base + ai.deep-learning` | Top3 出现目标词 |
| `hunhejingduxunlian` | `混合精度训练` | `base + ai.deep-learning` | Top5 出现目标词 |
| `zishiyingjuguji` | `自适应矩估计` | `base + ai.deep-learning` | Top5 出现目标词 |
| `duikangxunlian` | `对抗训练` | `base + ai.deep-learning` | Top5 出现目标词 |
| `lianghuaganzhixunlian` | `量化感知训练` | `base + ai.deep-learning` | Top5 出现目标词 |
| `shenduxuexi` | `深度学习` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 39. D28 NLP / 大语言模型（`ai.nlp-llm`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.nlp-llm`
- 边界对照：`base`（关闭 `ai.nlp-llm`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `ziranyuyanchuli` | `自然语言处理` | `base + ai.nlp-llm` | Top3 出现目标词 |
| `zhilingweitiao` | `指令微调` | `base + ai.nlp-llm` | Top5 出现目标词 |
| `jiansuozengqiangshengcheng` | `检索增强生成` | `base + ai.nlp-llm` | Top5 出现目标词 |
| `renleifankuiqianghuaxuexi` | `人类反馈强化学习` | `base + ai.nlp-llm` | Top5 出现目标词 |
| `tishizhuru` | `提示注入` | `base + ai.nlp-llm` | Top5 出现目标词 |
| `changshangxiawenpingce` | `长上下文评测` | `base + ai.nlp-llm` | Top5 出现目标词 |
| `zhilingweitiao` | `指令微调` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 40. D29 计算机视觉（`ai.computer-vision`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.computer-vision`
- 边界对照：`base`（关闭 `ai.computer-vision`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `mubiaojiance` | `目标检测` | `base + ai.computer-vision` | Top3 出现目标词 |
| `yuyifenge` | `语义分割` | `base + ai.computer-vision` | Top5 出现目标词 |
| `guangliuguji` | `光流估计` | `base + ai.computer-vision` | Top5 出现目标词 |
| `juanjishenjingwangluo` | `卷积神经网络` | `base + ai.computer-vision` | Top5 出现目标词 |
| `pingjunjingdujunzhi` | `平均精度均值` | `base + ai.computer-vision` | Top5 出现目标词 |
| `lianghuaganzhixunlian` | `量化感知训练` | `base + ai.computer-vision` | Top5 出现目标词 |
| `mubiaojiance` | `目标检测` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 41. D30 推荐系统（`ai.recommender-systems`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.recommender-systems`
- 边界对照：`base`（关闭 `ai.recommender-systems`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xietongguolvzhaohui` | `协同过滤召回` | `base + ai.recommender-systems` | Top3 出现目标词 |
| `cupaidafenmoxing` | `粗排打分模型` | `base + ai.recommender-systems` | Top5 出现目标词 |
| `jingpaizhuyiliwangluo` | `精排注意力网络` | `base + ai.recommender-systems` | Top5 出现目标词 |
| `zaixianshiyanpingtai` | `在线实验平台` | `base + ai.recommender-systems` | Top5 出现目标词 |
| `lengqidongtansuochi` | `冷启动探索池` | `base + ai.recommender-systems` | Top5 出现目标词 |
| `fanzuobizhaohui` | `反作弊召回` | `base + ai.recommender-systems` | Top5 出现目标词 |
| `xietongguolvzhaohui` | `协同过滤召回` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 42. D31 MLOps / 模型工程（`ai.mlops`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.mlops`
- 边界对照：`base`（关闭 `ai.mlops`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `moxingshengmingzhouqi` | `模型生命周期` | `base + ai.mlops` | Top3 出现目标词 |
| `hunhejingduxunlian` | `混合精度训练` | `base + ai.mlops` | Top5 出现目标词 |
| `tezhengpiaoyijiance` | `特征漂移检测` | `base + ai.mlops` | Top5 出现目标词 |
| `jinsiquefabu` | `金丝雀发布` | `base + ai.mlops` | Top5 出现目标词 |
| `gaojingjuhe` | `告警聚合` | `base + ai.mlops` | Top5 出现目标词 |
| `heguijiancha` | `合规检查` | `base + ai.mlops` | Top5 出现目标词 |
| `moxingshengmingzhouqi` | `模型生命周期` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
