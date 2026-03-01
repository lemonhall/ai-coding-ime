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

## 43. D14 搜索引擎 / 信息检索（`data.search-ir`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + data.search-ir`
- 边界对照：`base`（关闭 `data.search-ir`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `sousuoyinqing` | `搜索引擎` | `base + data.search-ir` | Top3 出现目标词 |
| `daopaisuoyin` | `倒排索引` | `base + data.search-ir` | Top3 出现目标词，且优先于泛化“索引”候选 |
| `bm25` | `BM25模型` | `base + data.search-ir` | Top3 出现目标词 |
| `xiangliangjiansuo` | `向量检索` | `base + data.search-ir` | Top5 出现目标词，排序稳定 |
| `ndcg` | `NDCG指标` | `base + data.search-ir` | Top5 出现目标词 |
| `daopaisuoyin` | `倒排索引` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 44. D15 编译器 / 解释器（`engineering.compiler-interpreter`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + engineering.compiler-interpreter`
- 边界对照：`base`（关闭 `engineering.compiler-interpreter`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `bianyiqi` | `编译器` | `base + engineering.compiler-interpreter` | Top3 出现目标词 |
| `cifafenxi` | `词法分析` | `base + engineering.compiler-interpreter` | Top5 出现目标词，排序稳定 |
| `jingtaidanfuzhi` | `静态单赋值` | `base + engineering.compiler-interpreter` | Top5 出现目标词 |
| `zijietimajieshiqi` | `字节码解释器` | `base + engineering.compiler-interpreter` | Top5 出现目标词 |
| `lajihuishoupingzhang` | `垃圾回收屏障` | `base + engineering.compiler-interpreter` | Top5 出现目标词 |
| `cifafenxi` | `词法分析` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 45. D16 编程语言理论（PLT）（`engineering.plt`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + engineering.plt`
- 边界对照：`base`（关闭 `engineering.plt`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `bianchengyuyanlilun` | `编程语言理论` | `base + engineering.plt` | Top3 出现目标词 |
| `leixingtuiduan` | `类型推断` | `base + engineering.plt` | Top5 出现目标词，排序稳定 |
| `lambda` | `Lambda演算` | `base + engineering.plt` | Top5 出现目标词 |
| `debuluyinsuoyin` | `德布鲁因索引` | `base + engineering.plt` | Top5 出现目标词 |
| `chouxiangjieshi` | `抽象解释` | `base + engineering.plt` | Top5 出现目标词 |
| `leixingtuiduan` | `类型推断` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 46. D25 图形学 / 渲染（`client.graphics-rendering`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + client.graphics-rendering`
- 边界对照：`base`（关闭 `client.graphics-rendering`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xuanranguanxian` | `渲染管线` | `base + client.graphics-rendering` | Top3 出现目标词 |
| `wulijichuxuanran` | `物理基础渲染` | `base + client.graphics-rendering` | Top5 出现目标词，排序稳定 |
| `pingmukongjianhuanjingguangzhebi` | `屏幕空间环境光遮蔽` | `base + client.graphics-rendering` | Top5 出现目标词 |
| `shuangsiyuanshumengpi` | `双四元数蒙皮` | `base + client.graphics-rendering` | Top5 出现目标词 |
| `guohuizhirelitu` | `过绘制热力图` | `base + client.graphics-rendering` | Top5 出现目标词 |
| `xuanranguanxian` | `渲染管线` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 47. D32 强化学习（`ai.reinforcement-learning`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + ai.reinforcement-learning`
- 边界对照：`base`（关闭 `ai.reinforcement-learning`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `qianghuaxuexi` | `强化学习` | `base + ai.reinforcement-learning` | Top3 出现目标词 |
| `maerkefujueceguocheng` | `马尔可夫决策过程` | `base + ai.reinforcement-learning` | Top5 出现目标词，排序稳定 |
| `shuangyanshishenduquedingxingceletidu` | `双延迟深度确定性策略梯度` | `base + ai.reinforcement-learning` | Top5 出现目标词 |
| `qmix` | `QMIX算法` | `base + ai.reinforcement-learning` | Top5 出现目标词 |
| `renleifankuiqianghuaxuexi` | `人类反馈强化学习` | `base + ai.reinforcement-learning` | Top5 出现目标词 |
| `qianghuaxuexi` | `强化学习` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 48. D33 数值计算 / 科学计算（`science.scientific-computing`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.scientific-computing`
- 边界对照：`base`（关闭 `science.scientific-computing`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `shuzhijifen` | `数值积分` | `base + science.scientific-computing` | Top3 出现目标词 |
| `gongetidufa` | `共轭梯度法` | `base + science.scientific-computing` | Top5 出现目标词，排序稳定 |
| `youxianyuanfa` | `有限元法` | `base + science.scientific-computing` | Top5 出现目标词 |
| `mcmccaiyang` | `MCMC采样` | `base + science.scientific-computing` | Top5 出现目标词 |
| `cfltiaojian` | `CFL条件` | `base + science.scientific-computing` | Top5 出现目标词 |
| `shuzhijifen` | `数值积分` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 49. D34 物理仿真 / 有限元（`science.physics-simulation-fem`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.physics-simulation-fem`
- 边界对照：`base`（关闭 `science.physics-simulation-fem`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `youxianyuanfa` | `有限元法` | `base + science.physics-simulation-fem` | Top3 出现目标词 |
| `wanggeshoulianxing` | `网格收敛性` | `base + science.physics-simulation-fem` | Top5 出现目标词，排序稳定 |
| `jiechufenxi` | `接触分析` | `base + science.physics-simulation-fem` | Top5 出现目标词 |
| `liugouhe` | `流固耦合` | `base + science.physics-simulation-fem` | Top5 出现目标词 |
| `duowulichangouhe` | `多物理场耦合` | `base + science.physics-simulation-fem` | Top5 出现目标词 |
| `duanlieyundu` | `断裂韧度` | `base + science.physics-simulation-fem` | Top5 出现目标词 |
| `jiechufenxi` | `接触分析` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 50. D35 信号处理 / DSP（`science.signal-processing-dsp`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.signal-processing-dsp`
- 边界对照：`base`（关闭 `science.signal-processing-dsp`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `fft` | `快速傅里叶变换` | `base + science.signal-processing-dsp` | Top3 出现目标词 |
| `fir` | `有限冲激响应滤波器` | `base + science.signal-processing-dsp` | Top5 出现目标词 |
| `iir` | `无限冲激响应滤波器` | `base + science.signal-processing-dsp` | Top5 出现目标词，排序稳定 |
| `lms` | `最小均方算法` | `base + science.signal-processing-dsp` | Top5 出现目标词 |
| `kalman` | `卡尔曼滤波` | `base + science.signal-processing-dsp` | Top5 出现目标词 |
| `wavelet` | `小波变换` | `base + science.signal-processing-dsp` | Top5 出现目标词 |
| `fft` | `快速傅里叶变换` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 51. D36 计算几何 / CAD（`science.computational-geometry-cad`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.computational-geometry-cad`
- 边界对照：`base`（关闭 `science.computational-geometry-cad`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `tubao` | `凸包算法` | `base + science.computational-geometry-cad` | Top3 出现目标词 |
| `delaoneisanjiaopoufen` | `德劳内三角剖分` | `base + science.computational-geometry-cad` | Top5 出现目标词 |
| `caotuqiujieqi` | `草图求解器` | `base + science.computational-geometry-cad` | Top5 出现目标词 |
| `nurbs` | `NURBS曲面` | `base + science.computational-geometry-cad` | Top5 出现目标词 |
| `ganshejiancha` | `干涉检查` | `base + science.computational-geometry-cad` | Top5 出现目标词 |
| `stepjiaohuan` | `STEP交换` | `base + science.computational-geometry-cad` | Top5 出现目标词 |
| `nurbs` | `NURBS曲面` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 52. D37 生物信息学（`science.bioinformatics`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.bioinformatics`
- 边界对照：`base`（关闭 `science.bioinformatics`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `shengwuxinxixue` | `生物信息学` | `base + science.bioinformatics` | Top3 出现目标词 |
| `danxibaozhuanluzuxue` | `单细胞转录组学` | `base + science.bioinformatics` | Top5 出现目标词 |
| `quanjiyinzuguanlianfenxi` | `全基因组关联分析` | `base + science.bioinformatics` | Top5 出现目标词，排序稳定 |
| `baomidingniaopiaolingdao` | `胞嘧啶鸟嘌呤岛` | `base + science.bioinformatics` | Top5 出现目标词 |
| `xunhuanzhongliutuoyanghetanghesuan` | `循环肿瘤脱氧核糖核酸` | `base + science.bioinformatics` | Top5 出现目标词 |
| `tujiyinzubidui` | `图基因组比对` | `base + science.bioinformatics` | Top5 出现目标词 |
| `shengwuxinxixue` | `生物信息学` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 53. D38 量子计算（`science.quantum-computing`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + science.quantum-computing`
- 边界对照：`base`（关闭 `science.quantum-computing`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `liangzijisuan` | `量子计算` | `base + science.quantum-computing` | Top3 出现目标词 |
| `liangzijiuchuo` | `量子纠错` | `base + science.quantum-computing` | Top5 出现目标词 |
| `xiaoersuanfa` | `肖尔算法` | `base + science.quantum-computing` | Top5 出现目标词 |
| `biaomianma` | `表面码` | `base + science.quantum-computing` | Top5 出现目标词 |
| `chaodaoliangzibite` | `超导量子比特` | `base + science.quantum-computing` | Top5 出现目标词 |
| `liangzixianluqiege` | `量子线路切割` | `base + science.quantum-computing` | Top5 出现目标词 |
| `liangzijisuan` | `量子计算` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 54. D44 区块链 / Web3（`domain.blockchain-web3`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.blockchain-web3`
- 边界对照：`base`（关闭 `domain.blockchain-web3`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `qukuailian` | `区块链` | `base + domain.blockchain-web3` | Top3 出现目标词 |
| `gongzuoliangzhengming` | `工作量证明` | `base + domain.blockchain-web3` | Top5 出现目标词 |
| `zhinengheyue` | `智能合约` | `base + domain.blockchain-web3` | Top5 出现目标词，排序稳定 |
| `liudongxingwakuang` | `流动性挖矿` | `base + domain.blockchain-web3` | Top5 出现目标词 |
| `kualianqiao` | `跨链桥` | `base + domain.blockchain-web3` | Top5 出现目标词 |
| `keyanzhengpingzheng` | `可验证凭证` | `base + domain.blockchain-web3` | Top5 出现目标词 |
| `qukuailian` | `区块链` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 55. D45 音视频处理 / 流媒体（`domain.audio-video-streaming`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.audio-video-streaming`
- 边界对照：`base`（关闭 `domain.audio-video-streaming`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `yinpinbianma` | `音频编码` | `base + domain.audio-video-streaming` | Top3 出现目标词 |
| `yinhuatongbu` | `音画同步` | `base + domain.audio-video-streaming` | Top5 出现目标词，不被泛词“同步”抢位 |
| `shishizhuanma` | `实时转码` | `base + domain.audio-video-streaming` | Top5 出现目标词，且与“离线转码”区分明确 |
| `diyanchizhibo` | `低延迟直播` | `base + domain.audio-video-streaming` | Top5 出现目标词 |
| `neirongfenfawangluo` | `内容分发网络` | `base + domain.audio-video-streaming` | Top5 出现目标词，相关 CDN 词条可见 |
| `yinpinbianma` | `音频编码` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 56. D46 GIS / 地理信息（`domain.gis`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.gis`
- 边界对照：`base`（关闭 `domain.gis`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `kongjianfenxi` | `空间分析` | `base + domain.gis` | Top3 出现目标词 |
| `gaosikelvge` | `高斯克吕格` | `base + domain.gis` | Top5 出现目标词 |
| `kelijinchazhi` | `克里金插值` | `base + domain.gis` | Top5 出现目标词 |
| `zhengsheyingxiang` | `正射影像` | `base + domain.gis` | Top5 出现目标词 |
| `wangluoditufuwu` | `网络地图服务` | `base + domain.gis` | Top5 出现目标词 |
| `kongjianfenxi` | `空间分析` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 57. D47 机器人 / ROS（`domain.robotics-ros`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.robotics-ros`
- 边界对照：`base`（关闭 `domain.robotics-ros`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jiqirencaozuoxitongjiedian` | `机器人操作系统节点` | `base + domain.robotics-ros` | Top3 出现目标词 |
| `zuobiaobianhuanpeizhi` | `坐标变换配置` | `base + domain.robotics-ros` | Top5 出现目标词 |
| `tongbudingweijiantuliucheng` | `同步定位建图流程` | `base + domain.robotics-ros` | Top5 出现目标词 |
| `jiguangleidajiekou` | `激光雷达接口` | `base + domain.robotics-ros` | Top5 出现目标词 |
| `jixiebikongzhifangan` | `机械臂控制方案` | `base + domain.robotics-ros` | Top5 出现目标词 |
| `jiqirencaozuoxitongjiedian` | `机器人操作系统节点` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 58. D49 数据可视化（`domain.data-visualization`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.data-visualization`
- 边界对照：`base`（关闭 `domain.data-visualization`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `relitu` | `热力图` | `base + domain.data-visualization` | Top3 出现目标词 |
| `pingxingzuobiaotu` | `平行坐标图` | `base + domain.data-visualization` | Top5 出现目标词 |
| `fenjishetuse` | `分级设色图` | `base + domain.data-visualization` | Top5 出现目标词 |
| `liandonggaoliang` | `联动高亮` | `base + domain.data-visualization` | Top5 出现目标词 |
| `yichangdianjiancetu` | `异常点检测图` | `base + domain.data-visualization` | Top5 出现目标词 |
| `relitu` | `热力图` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 59. D50 低代码 / DSL / 配置语言（`domain.lowcode-dsl-config`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + domain.lowcode-dsl-config`
- 边界对照：`base`（关闭 `domain.lowcode-dsl-config`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `didaimapingtai` | `低代码平台` | `base + domain.lowcode-dsl-config` | Top3 出现目标词 |
| `chouxiangyufashu` | `抽象语法树` | `base + domain.lowcode-dsl-config` | Top5 出现目标词 |
| `peizhihebing` | `配置合并` | `base + domain.lowcode-dsl-config` | Top5 出现目标词 |
| `guizeshejiqi` | `规则设计器` | `base + domain.lowcode-dsl-config` | Top5 出现目标词 |
| `zifuchuanchazhi` | `字符串插值` | `base + domain.lowcode-dsl-config` | Top5 出现目标词 |
| `didaimapingtai` | `低代码平台` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 60. D54 财务 / 支付 / 结算（`business.finance-payments`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + business.finance-payments`
- 边界对照：`base`（关闭 `business.finance-payments`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `jiesuangize` | `结算规则` | `base + business.finance-payments` | Top3 出现目标词 |
| `qingsuanhuizhi` | `清算回执` | `base + business.finance-payments` | Top5 出现目标词 |
| `yingshouhexiao` | `应收核销` | `base + business.finance-payments` | Top5 出现目标词 |
| `fapiaogouxuan` | `发票勾选` | `base + business.finance-payments` | Top5 出现目标词 |
| `jufushensu` | `拒付申诉` | `base + business.finance-payments` | Top5 出现目标词 |
| `jiesuangize` | `结算规则` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 61. D55 供应链 / 物流 / WMS（`business.supply-chain-logistics-wms`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + business.supply-chain-logistics-wms`
- 边界对照：`base`（关闭 `business.supply-chain-logistics-wms`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `gongyinglian` | `供应链` | `base + business.supply-chain-logistics-wms` | Top3 出现目标词 |
| `cangkukuwei` | `仓库库位` | `base + business.supply-chain-logistics-wms` | Top5 出现目标词 |
| `rukuguozhang` | `入库过账` | `base + business.supply-chain-logistics-wms` | Top5 出现目标词 |
| `zhaiguojianxuan` | `摘果拣选` | `base + business.supply-chain-logistics-wms` | Top5 出现目标词 |
| `kechengnuokucun` | `可承诺库存` | `base + business.supply-chain-logistics-wms` | Top5 出现目标词 |
| `nixiangwuliu` | `逆向物流` | `base + business.supply-chain-logistics-wms` | Top5 出现目标词 |
| `gongyinglian` | `供应链` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 62. D56 电商平台（`business.ecommerce-platform`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + business.ecommerce-platform`
- 边界对照：`base`（关闭 `business.ecommerce-platform`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `dianshangpingtai` | `电商平台` | `base + business.ecommerce-platform` | Top3 出现目标词 |
| `shangjiaruzhu` | `商家入驻` | `base + business.ecommerce-platform` | Top3 出现目标词 |
| `youhuiquan` | `优惠券` | `base + business.ecommerce-platform` | Top5 出现目标词 |
| `zhibodaihuo` | `直播带货` | `base + business.ecommerce-platform` | Top5 出现目标词 |
| `dingdanqueren` | `订单确认` | `base + business.ecommerce-platform` | Top5 出现目标词 |
| `wuliuguiji` | `物流轨迹` | `base + business.ecommerce-platform` | Top5 出现目标词 |
| `dianshangpingtai` | `电商平台` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 63. D57 OA / 协同办公 / 工作流（`business.oa-workflow`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + business.oa-workflow`
- 边界对照：`base`（关闭 `business.oa-workflow`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xietongbangong` | `协同办公` | `base + business.oa-workflow` | Top3 出现目标词 |
| `daibanzhongxin` | `待办中心` | `base + business.oa-workflow` | Top5 出现目标词 |
| `liuchengjianmo` | `流程建模` | `base + business.oa-workflow` | Top5 出现目标词 |
| `shenpijilu` | `审批记录` | `base + business.oa-workflow` | Top5 出现目标词 |
| `biaodansheji` | `表单设计` | `base + business.oa-workflow` | Top5 出现目标词 |
| `feiyongbaoxiao` | `费用报销` | `base + business.oa-workflow` | Top5 出现目标词 |
| `xietongbangong` | `协同办公` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 64. D58 产品管理（`product.management`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.management`
- 边界对照：`base`（关闭 `product.management`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `chanpinguanli` | `产品管理` | `base + product.management` | Top3 出现目标词 |
| `chanpinluxiantu` | `产品路线图` | `base + product.management` | Top5 出现目标词 |
| `xuqiuyouxianji` | `需求优先级` | `base + product.management` | Top5 出现目标词 |
| `yonghuyanjiu` | `用户研究` | `base + product.management` | Top5 出现目标词 |
| `jingpinfenxi` | `竞品分析` | `base + product.management` | Top5 出现目标词 |
| `beijixingzhibiao` | `北极星指标` | `base + product.management` | Top5 出现目标词 |
| `chanpinguanli` | `产品管理` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 65. D59 增长 / 用户运营（`product.growth-operations`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.growth-operations`
- 边界对照：`base`（关闭 `product.growth-operations`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `zengzhangfeilun` | `增长飞轮` | `base + product.growth-operations` | Top3 出现目标词 |
| `laxincuhuo` | `拉新促活` | `base + product.growth-operations` | Top5 出现目标词 |
| `huokechengben` | `获客成本` | `base + product.growth-operations` | Top5 出现目标词 |
| `liucunlv` | `留存率` | `base + product.growth-operations` | Top5 出现目标词 |
| `yonghufenqun` | `用户分群` | `base + product.growth-operations` | Top5 出现目标词 |
| `zidonghuachuda` | `自动化触达` | `base + product.growth-operations` | Top5 出现目标词 |
| `zengzhangfeilun` | `增长飞轮` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 66. D60 广告与营销技术（AdTech/MarTech）（`product.adtech-martech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.adtech-martech`
- 边界对照：`base`（关闭 `product.adtech-martech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `chengxuhuaguanggao` | `程序化广告` | `base + product.adtech-martech` | Top3 出现目标词 |
| `chongdingxiangguanggao` | `重定向广告` | `base + product.adtech-martech` | Top5 出现目标词 |
| `shujuguanlipingtai` | `数据管理平台` | `base + product.adtech-martech` | Top5 出现目标词 |
| `guanggaozhichuhuibao` | `广告支出回报` | `base + product.adtech-martech` | Top5 出现目标词 |
| `duochudianguiyin` | `多触点归因` | `base + product.adtech-martech` | Top5 出现目标词 |
| `liuliangfanzuobi` | `流量反作弊` | `base + product.adtech-martech` | Top5 出现目标词 |
| `chengxuhuaguanggao` | `程序化广告` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 67. D61 内容 / 社区运营（`product.content-community-ops`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.content-community-ops`
- 边界对照：`base`（关闭 `product.content-community-ops`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `neirongyunying` | `内容运营` | `base + product.content-community-ops` | Top3 出现目标词 |
| `huatiyunying` | `话题运营` | `base + product.content-community-ops` | Top5 出现目标词 |
| `fengxianneirong` | `风险内容` | `base + product.content-community-ops` | Top5 出现目标词 |
| `jubaochuli` | `举报处理` | `base + product.content-community-ops` | Top5 出现目标词 |
| `shengmingzhouqiguanli` | `生命周期管理` | `base + product.content-community-ops` | Top5 出现目标词 |
| `shequyunying` | `社区运营` | `base + product.content-community-ops` | Top5 出现目标词 |
| `neirongyunying` | `内容运营` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 68. D62 SEO / SEM / ASO（`product.seo-sem-aso`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.seo-sem-aso`
- 边界对照：`base`（关闭 `product.seo-sem-aso`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `sousuoyinqingyouhua` | `搜索引擎优化` | `base + product.seo-sem-aso` | Top3 出现目标词 |
| `guifanbiaoqian` | `规范标签` | `base + product.seo-sem-aso` | Top5 出现目标词 |
| `foudinguanjianci` | `否定关键词` | `base + product.seo-sem-aso` | Top5 出现目标词 |
| `shujuqudongguiyin` | `数据驱动归因` | `base + product.seo-sem-aso` | Top5 出现目标词 |
| `yingyongjietuyouhua` | `应用截图优化` | `base + product.seo-sem-aso` | Top5 出现目标词 |
| `pingfenyupinglun` | `评分与评论` | `base + product.seo-sem-aso` | Top5 出现目标词 |
| `sousuoyinqingyouhua` | `搜索引擎优化` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 69. D63 数据分析 / BI（`product.analytics-bi`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.analytics-bi`
- 边界对照：`base`（关闭 `product.analytics-bi`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `zhibiaokoujing` | `指标口径` | `base + product.analytics-bi` | Top3 出现目标词 |
| `shucangxiaofeiceng` | `数仓消费层` | `base + product.analytics-bi` | Top5 出现目标词 |
| `baobiaozhongxin` | `报表中心` | `base + product.analytics-bi` | Top5 出现目标词 |
| `loudoufenxi` | `漏斗分析` | `base + product.analytics-bi` | Top5 出现目标词 |
| `guiyinchuangkou` | `归因窗口` | `base + product.analytics-bi` | Top5 出现目标词 |
| `maidiancaiji` | `埋点采集` | `base + product.analytics-bi` | Top5 出现目标词 |
| `zhibiaokoujing` | `指标口径` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 70. D64 A/B测试 / 实验平台（`product.ab-testing`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + product.ab-testing`
- 边界对照：`base`（关闭 `product.ab-testing`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `suijifenliu` | `随机分流` | `base + product.ab-testing` | Top3 出现目标词 |
| `yangbenliangguji` | `样本量估计` | `base + product.ab-testing` | Top5 出现目标词 |
| `xianzhuxingshuiping` | `显著性水平` | `base + product.ab-testing` | Top5 出现目标词 |
| `hulanzhibiao` | `护栏指标` | `base + product.ab-testing` | Top5 出现目标词 |
| `shiyianjiankong` | `实验监控` | `base + product.ab-testing` | Top5 出现目标词 |
| `huiguncelue` | `回滚策略` | `base + product.ab-testing` | Top5 出现目标词 |
| `suijifenliu` | `随机分流` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 71. D65 金融科技（FinTech）（`industry.fintech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.fintech`
- 边界对照：`base`（关闭 `industry.fintech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `juhezhifu` | `聚合支付` | `base + industry.fintech` | Top3 出现目标词 |
| `qingsuanchongpao` | `清算重跑` | `base + industry.fintech` | Top5 出现目标词 |
| `qianbaoshouquan` | `钱包授权` | `base + industry.fintech` | Top5 出现目标词 |
| `fanxiqianjiance` | `反洗钱监测` | `base + industry.fintech` | Top5 出现目标词 |
| `jufushensu` | `拒付申诉` | `base + industry.fintech` | Top5 出现目标词 |
| `huilvsuoding` | `汇率锁定` | `base + industry.fintech` | Top5 出现目标词 |
| `juhezhifu` | `聚合支付` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 72. D66 保险科技（InsurTech）（`industry.insurtech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.insurtech`
- 边界对照：`base`（关闭 `industry.insurtech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `hebao` | `核保` | `base + industry.insurtech` | Top3 出现目标词 |
| `hebaoyinqing` | `核保引擎` | `base + industry.insurtech` | Top5 出现目标词 |
| `lipeifanqizha` | `理赔反欺诈` | `base + industry.insurtech` | Top5 出现目标词 |
| `zaibaoxianfenchu` | `再保险分出` | `base + industry.insurtech` | Top5 出现目标词 |
| `jingsuanzhunbeijin` | `精算准备金` | `base + industry.insurtech` | Top5 出现目标词 |
| `chelianwangdingjia` | `车联网定价` | `base + industry.insurtech` | Top5 出现目标词 |
| `hebao` | `核保` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 73. D67 医疗健康（HealthTech）（`industry.healthtech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.healthtech`
- 边界对照：`base`（关闭 `industry.healthtech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `dianzibingli` | `电子病历` | `base + industry.healthtech` | Top3 出现目标词 |
| `linchuanglujing` | `临床路径` | `base + industry.healthtech` | Top5 出现目标词 |
| `yibaozhinengshenhe` | `医保智能审核` | `base + industry.healthtech` | Top5 出现目标词 |
| `manbingguanli` | `慢病管理` | `base + industry.healthtech` | Top5 出现目标词 |
| `yongyaohelixingshenhe` | `用药合理性审核` | `base + industry.healthtech` | Top5 出现目标词 |
| `yingxiangxinxixitong` | `影像信息系统` | `base + industry.healthtech` | Top5 出现目标词 |
| `dianzibingli` | `电子病历` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 74. D68 教育科技（EdTech）（`industry.edtech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.edtech`
- 边界对照：`base`（关闭 `industry.edtech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `xuexiguanlixitong` | `学习管理系统` | `base + industry.edtech` | Top3 出现目标词 |
| `xuexijindukanban` | `学习进度看板` | `base + industry.edtech` | Top5 出现目标词 |
| `zhinengpaike` | `智能排课` | `base + industry.edtech` | Top5 出现目标词 |
| `tikuguanli` | `题库管理` | `base + industry.edtech` | Top5 出现目标词 |
| `qiepinggaojing` | `切屏告警` | `base + industry.edtech` | Top5 出现目标词 |
| `xueqingzhenduan` | `学情诊断` | `base + industry.edtech` | Top5 出现目标词 |
| `xuexiguanlixitong` | `学习管理系统` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 75. D69 房产 / PropTech（`industry.proptech`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.proptech`
- 边界对照：`base`（关闭 `industry.proptech`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `fangyuancaiji` | `房源采集` | `base + industry.proptech` | Top3 出现目标词 |
| `jiaoyicuohe` | `交易撮合` | `base + industry.proptech` | Top5 出现目标词 |
| `zaixianqianyue` | `在线签约` | `base + industry.proptech` | Top5 出现目标词 |
| `zijinjianguan` | `资金监管` | `base + industry.proptech` | Top5 出现目标词 |
| `wuyejiaofei` | `物业缴费` | `base + industry.proptech` | Top5 出现目标词 |
| `yuanqurelitu` | `园区热力图` | `base + industry.proptech` | Top5 出现目标词 |
| `fangyuancaiji` | `房源采集` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 76. D70 出行 / 地图 / LBS（`industry.mobility-lbs`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.mobility-lbs`
- 边界对照：`base`（关闭 `industry.mobility-lbs`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `weizhifuwu` | `位置服务` | `base + industry.mobility-lbs` | Top3 出现目标词 |
| `lujingguihua` | `路径规划` | `base + industry.mobility-lbs` | Top5 出现目标词 |
| `wangyuechepaidan` | `网约车派单` | `base + industry.mobility-lbs` | Top5 出现目标词 |
| `dianziweilan` | `电子围栏` | `base + industry.mobility-lbs` | Top5 出现目标词 |
| `yujidaodashijian` | `预计到达时间` | `base + industry.mobility-lbs` | Top5 出现目标词 |
| `dongtaigaidao` | `动态改道` | `base + industry.mobility-lbs` | Top5 出现目标词 |
| `weizhifuwu` | `位置服务` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 77. D71 SaaS / 订阅制商业（`industry.saas-subscription`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + industry.saas-subscription`
- 边界对照：`base`（关闭 `industry.saas-subscription`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `dingyuetaocan` | `订阅套餐` | `base + industry.saas-subscription` | Top3 出现目标词 |
| `anliangjifei` | `按量计费` | `base + industry.saas-subscription` | Top5 出现目标词 |
| `zidongxufei` | `自动续费` | `base + industry.saas-subscription` | Top5 出现目标词 |
| `duozuhujiagou` | `多租户架构` | `base + industry.saas-subscription` | Top5 出现目标词 |
| `zuoxikuorong` | `座席扩容` | `base + industry.saas-subscription` | Top5 出现目标词 |
| `heguishenji` | `合规审计` | `base + industry.saas-subscription` | Top5 出现目标词 |
| `dingyuetaocan` | `订阅套餐` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |

## 78. D72 数据隐私与合规（`compliance.data-privacy`）专项回归 Query（2026-03-01）

激活组合说明：
- 正向命中：`base + compliance.data-privacy`
- 边界对照：`base`（关闭 `compliance.data-privacy`）

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `shujufenleifenji` | `数据分类分级` | `base + compliance.data-privacy` | Top3 出现目标词 |
| `zuixiaobiyao` | `最小必要` | `base + compliance.data-privacy` | Top5 出现目标词 |
| `tongyiguanlipingtai` | `同意管理平台` | `base + compliance.data-privacy` | Top5 出现目标词 |
| `shujubaohuyingxiangpinggu` | `数据保护影响评估` | `base + compliance.data-privacy` | Top5 出现目标词 |
| `qubiaoshihua` | `去标识化` | `base + compliance.data-privacy` | Top5 出现目标词 |
| `kuajingshujuchuanshu` | `跨境数据传输` | `base + compliance.data-privacy` | Top5 出现目标词 |
| `shujufenleifenji` | `数据分类分级` | `base` | 目标词较开启领域 profile 时明显后移（不应强前移） |
