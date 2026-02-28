# v3 Profile 领域词库 Backlog / Tasklist

## 文档关系

- 领域清单：[`docs/profile-dict-domain-checklist.md`](../profile-dict-domain-checklist.md)
- 运营规范：[`docs/profile-dict-ops-template.md`](../profile-dict-ops-template.md)
- 手工回归：[`docs/profile-dict-manual-test.md`](../profile-dict-manual-test.md)
- 下会话指令：[`docs/profile-dict-next-session-instruction.md`](../profile-dict-next-session-instruction.md)

## Goal

将 50 个技术子领域逐步沉淀为可维护的 profile 词库，每个领域 200-500 词，并形成可复现的回归闭环。

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

- [ ] D08 Web前端（从 SEEDED 扩展到 200-500）
- [ ] D22 移动端开发（从 SEEDED 扩展到 200-500）
- [ ] D07 Web后端 / API设计
- [ ] D18 DevOps / CI/CD / SRE
- [ ] D20 测试工程

### Wave 2：基础工程能力

- [ ] D17 软件工程 / 架构模式
- [ ] D19 版本控制 / 协作
- [ ] D21 包管理 / 构建系统
- [ ] D11 关系型数据库
- [ ] D12 NoSQL / NewSQL

### Wave 3：基础设施与平台

- [ ] D39 云计算 / IaaS
- [ ] D40 容器与编排
- [ ] D41 消息队列 / 事件流
- [ ] D42 可观测性 / 监控
- [ ] D43 IaC / 基础设施即代码

### Wave 4：系统底层

- [ ] D01 计算机体系结构 / CPU设计
- [ ] D02 嵌入式系统 / IoT
- [ ] D03 操作系统内核
- [ ] D04 驱动与硬件抽象
- [ ] D05 FPGA / 硬件描述语言

### Wave 5：网络与安全

- [ ] D06 计算机网络 / 协议栈
- [ ] D09 网络安全 / 密码学
- [ ] D10 分布式系统
- [ ] D14 搜索引擎 / 信息检索
- [ ] D13 数据工程 / ETL / 大数据

### Wave 6：AI 核心

- [ ] D26 经典机器学习
- [ ] D27 深度学习 / 神经网络
- [ ] D28 NLP / 大语言模型
- [ ] D29 计算机视觉
- [ ] D30 推荐系统

### Wave 7：AI 工程化

- [ ] D31 MLOps / 模型工程
- [ ] D32 强化学习
- [ ] D15 编译器 / 解释器
- [ ] D16 编程语言理论（PLT）
- [ ] D48 编辑器 / IDE / 开发者工具

### Wave 8：客户端图形

- [ ] D23 桌面应用 / 跨平台GUI
- [ ] D24 游戏开发
- [ ] D25 图形学 / 渲染
- [ ] D49 数据可视化
- [ ] D45 音视频处理 / 流媒体

### Wave 9：科学计算

- [ ] D33 数值计算 / 科学计算
- [ ] D34 物理仿真 / 有限元
- [ ] D35 信号处理 / DSP
- [ ] D36 计算几何 / CAD
- [ ] D37 生物信息学

### Wave 10：专项应用

- [ ] D38 量子计算
- [ ] D44 区块链 / Web3
- [ ] D46 GIS / 地理信息
- [ ] D47 机器人 / ROS
- [ ] D50 低代码 / DSL / 配置语言

## 风险与约束

- 词库膨胀风险：每领域超过 500 词容易引入噪音，建议先完成核心 200-300 再扩展。
- 资产一致性风险：`docs/samples` 与 `app/assets` 必须同名同步。
- 导入可见性风险：新增 slug 未进 `ProfileDictionaryCatalog` 会导致重导入不可见。
