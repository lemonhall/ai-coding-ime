# v3 Profile 领域词库 Backlog / Tasklist

## 文档关系

- 领域清单：[`docs/profile-dict-domain-checklist.md`](../profile-dict-domain-checklist.md)
- 运营规范：[`docs/profile-dict-ops-template.md`](../profile-dict-ops-template.md)
- 手工回归：[`docs/profile-dict-manual-test.md`](../profile-dict-manual-test.md)
- 下会话指令：[`docs/profile-dict-next-session-instruction.md`](../profile-dict-next-session-instruction.md)

## Goal

将 74 个技术+商业子领域逐步沉淀为可维护的 profile 词库，每个领域 200-500 词，并形成可复现的回归闭环。

## Progress Snapshot（as of 2026-03-01）

- DONE: 48 / 74
- SEEDED: 0 / 74
- TODO: 26 / 74
- 当前优先：按 wave 顺序推进剩余 TODO 领域。

## Scope

- In scope:
  - 每次会话完成 1 个领域词库（200-500 词）
  - 同步更新 checklist 状态与回归 query
  - 必要时更新 `ProfileDictionaryCatalog.entries` 以支持重导入
- Out of scope:
  - 一次会话并行产出多个领域
  - 云端分发/自动抓词/自动评估平台（后续阶段）

## 单会话标准任务（复制到每次会话执行）

- [ ] 选定一个 `TODO/SEEDED` 领域（并在 checklist 标记为 `DOING`）
- [ ] 产出 `docs/samples/profile-dictionaries/profile.<slug>.txt`（200-500 条）
- [ ] 同步到 `app/src/main/assets/projectdict/profile-dictionaries/profile.<slug>.txt`
- [ ] 若是新 slug，同步更新 `ProfileDictionaryCatalog.entries`
- [ ] 补充至少 5 条回归 query（命中/误召回/边界）
- [ ] 在 checklist 将该领域标记为 `DONE`
- [ ] 运行最小验证：
  - `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*"`

## 波次规划（每波 5 个领域）

### Wave 1：现有种子扩展（优先）

- [x] D08 Web前端（DONE: `frontend`, 378）
- [x] D22 移动端开发（DONE: `app.android`, 350）
- [x] D07 Web后端 / API设计（DONE: `network.web-backend-api`, 356）
- [x] D18 DevOps / CI/CD / SRE（DONE: `engineering.devops-sre`, 363）
- [x] D20 测试工程（DONE: `engineering.testing`, 297）

### Wave 2：基础工程能力

- [ ] D17 软件工程 / 架构模式
- [x] D19 版本控制 / 协作（DONE: `engineering.vcs-collaboration`, 250）
- [x] D21 包管理 / 构建系统（DONE: `engineering.package-build`, 270）
- [ ] D11 关系型数据库
- [x] D12 NoSQL / NewSQL（DONE: `data.nosql-newsql`, 220）

### Wave 3：基础设施与平台

- [x] D39 云计算 / IaaS（DONE: `infra.cloud-iaas`, 230）
- [x] D40 容器与编排（DONE: `infra.containers-orchestration`, 230）
- [x] D41 消息队列 / 事件流（DONE: `infra.mq-event-streaming`, 230）
- [x] D42 可观测性 / 监控（DONE: `infra.observability-monitoring`, 230）
- [x] D43 IaC / 基础设施即代码（DONE: `infra.iac`, 230）

### Wave 4：系统底层

- [x] D01 计算机体系结构 / CPU设计（DONE: `hardware.cpu-architecture`, 220）
- [x] D02 嵌入式系统 / IoT（DONE: `hardware.embedded-iot`, 220）
- [x] D03 操作系统内核（DONE: `hardware.os-kernel`, 220）
- [x] D04 驱动与硬件抽象（DONE: `hardware.driver-hal`, 220）
- [x] D05 FPGA / 硬件描述语言（DONE: `hardware.fpga-hdl`, 220）

### Wave 5：网络与安全

- [x] D06 计算机网络 / 协议栈（DONE: `network.protocols`, 220）
- [x] D09 网络安全 / 密码学（DONE: `network.security-crypto`, 220）
- [x] D10 分布式系统（DONE: `network.distributed-systems`, 220）
- [x] D14 搜索引擎 / 信息检索（DONE: `data.search-ir`, 220）
- [x] D13 数据工程 / ETL / 大数据（DONE: `data.engineering-etl`, 220）

### Wave 6：AI 核心

- [x] D26 经典机器学习（DONE: `ai.classic-ml`, 220）
- [x] D27 深度学习 / 神经网络（DONE: `ai.deep-learning`, 220）
- [x] D28 NLP / 大语言模型（DONE: `ai.nlp-llm`, 220）
- [x] D29 计算机视觉（DONE: `ai.computer-vision`, 220）
- [x] D30 推荐系统（DONE: `ai.recommender-systems`, 220）

### Wave 7：AI 工程化

- [x] D31 MLOps / 模型工程（DONE: `ai.mlops`, 220）
- [x] D32 强化学习（DONE: `ai.reinforcement-learning`, 220）
- [x] D15 编译器 / 解释器（DONE: `engineering.compiler-interpreter`, 220）
- [x] D16 编程语言理论（PLT）（DONE: `engineering.plt`, 220）
- [x] D48 编辑器 / IDE / 开发者工具（DONE: `domain.editor-ide-tooling`, 272）

### Wave 8：客户端图形

- [x] D23 桌面应用 / 跨平台GUI（DONE: `client.desktop-cross-platform`, 270）
- [x] D24 游戏开发（DONE: `client.game-dev`, 300）
- [x] D25 图形学 / 渲染（DONE: `client.graphics-rendering`, 220）
- [ ] D49 数据可视化
- [ ] D45 音视频处理 / 流媒体

### Wave 9：科学计算

- [x] D33 数值计算 / 科学计算（DONE: `science.scientific-computing`, 220）
- [x] D34 物理仿真 / 有限元（DONE: `science.physics-simulation-fem`, 220）
- [x] D35 信号处理 / DSP（DONE: `science.signal-processing-dsp`, 220）
- [x] D36 计算几何 / CAD（DONE: `science.computational-geometry-cad`, 220）
- [x] D37 生物信息学（DONE: `science.bioinformatics`, 220）

### Wave 10：专项应用

- [x] D38 量子计算（DONE: `science.quantum-computing`, 220）
- [x] D44 区块链 / Web3（DONE: `domain.blockchain-web3`, 220）
- [ ] D46 GIS / 地理信息
- [ ] D47 机器人 / ROS
- [ ] D50 低代码 / DSL / 配置语言

### Wave 11：企业软件（核心业务流）

- [ ] D51 ERP / 企业资源计划（SEEDED -> 200-500）
- [x] D53 HRM / 人力资源系统（DONE: `business.hrm`, 300）
- [ ] D54 财务 / 支付 / 结算
- [ ] D55 供应链 / 物流 / WMS
- [ ] D56 电商平台

### Wave 12：企业软件（协同与客户）

- [ ] D52 CRM / 客户关系管理（SEEDED -> 200-500）
- [ ] D57 OA / 协同办公 / 工作流
- [ ] D58 产品管理
- [ ] D59 增长 / 用户运营
- [ ] D63 数据分析 / BI

### Wave 13：运营与营销

- [ ] D60 广告与营销技术（AdTech/MarTech）
- [ ] D61 内容 / 社区运营
- [ ] D62 SEO / SEM / ASO
- [ ] D64 A/B测试 / 实验平台
- [ ] D71 SaaS / 订阅制商业

### Wave 14：垂直行业

- [ ] D65 金融科技（FinTech）
- [ ] D66 保险科技（InsurTech）
- [ ] D67 医疗健康（HealthTech）
- [ ] D68 教育科技（EdTech）
- [ ] D69 房产 / PropTech

### Wave 15：垂直与合规

- [ ] D70 出行 / 地图 / LBS
- [ ] D72 数据隐私与合规
- [ ] D73 信息安全合规
- [ ] D74 行业监管（中国特色）

## 风险与约束

- 词库膨胀风险：每领域超过 500 词容易引入噪音，建议先完成核心 200-300 再扩展。
- 资产一致性风险：`docs/samples` 与 `app/assets` 必须同名同步。
- 导入可见性风险：新增 slug 未进 `ProfileDictionaryCatalog` 会导致重导入不可见。
