# Profile 词库领域 Checklist（50 Domains）

用途：把“一个领域一个领域产出 200-500 词词典”变成可持续推进的任务面板。  
配套文档：

- 运营规范：[`docs/profile-dict-ops-template.md`](./profile-dict-ops-template.md)
- 执行 backlog：[`docs/plan/v3-profile-domain-dictionary-backlog.md`](./plan/v3-profile-domain-dictionary-backlog.md)
- 下会话指令：[`docs/profile-dict-next-session-instruction.md`](./profile-dict-next-session-instruction.md)
- 对话归档：[`docs/archive/2026-02-28-programming-context-lexicon-dialogue.md`](./archive/2026-02-28-programming-context-lexicon-dialogue.md)

状态说明：

- `TODO`：未开始
- `DOING`：进行中
- `DONE`：已完成（并通过回归）
- `SEEDED`：已有种子词库（<200 词），需扩展到 200-500

---

## A. 硬件与系统底层

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D01 | 计算机体系结构 / CPU设计 | `hardware.cpu-architecture` | 200-500 | TODO |
| D02 | 嵌入式系统 / IoT | `hardware.embedded-iot` | 200-500 | TODO |
| D03 | 操作系统内核 | `hardware.os-kernel` | 200-500 | TODO |
| D04 | 驱动与硬件抽象 | `hardware.driver-hal` | 200-500 | TODO |
| D05 | FPGA / 硬件描述语言 | `hardware.fpga-hdl` | 200-500 | TODO |

## B. 网络与通信

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D06 | 计算机网络 / 协议栈 | `network.protocols` | 200-500 | TODO |
| D07 | Web后端 / API设计 | `network.web-backend-api` | 200-500 | DONE (`network.web-backend-api`, 356) |
| D08 | Web前端 | `network.web-frontend` | 200-500 | DONE (`frontend`, 378) |
| D09 | 网络安全 / 密码学 | `network.security-crypto` | 200-500 | TODO |
| D10 | 分布式系统 | `network.distributed-systems` | 200-500 | TODO |

## C. 数据与存储

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D11 | 关系型数据库 | `data.relational-db` | 200-500 | TODO |
| D12 | NoSQL / NewSQL | `data.nosql-newsql` | 200-500 | TODO |
| D13 | 数据工程 / ETL / 大数据 | `data.engineering-etl` | 200-500 | TODO |
| D14 | 搜索引擎 / 信息检索 | `data.search-ir` | 200-500 | TODO |

## D. 编程语言与工程

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D15 | 编译器 / 解释器 | `engineering.compiler-interpreter` | 200-500 | TODO |
| D16 | 编程语言理论（PLT） | `engineering.plt` | 200-500 | TODO |
| D17 | 软件工程 / 架构模式 | `engineering.software-architecture` | 200-500 | TODO |
| D18 | DevOps / CI/CD / SRE | `engineering.devops-sre` | 200-500 | DONE (`engineering.devops-sre`, 363) |
| D19 | 版本控制 / 协作 | `engineering.vcs-collaboration` | 200-500 | DONE (`engineering.vcs-collaboration`, 250) |
| D20 | 测试工程 | `engineering.testing` | 200-500 | DONE (`engineering.testing`, 297) |
| D21 | 包管理 / 构建系统 | `engineering.package-build` | 200-500 | TODO |

## E. 客户端与用户界面

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D22 | 移动端开发（Android/iOS） | `client.mobile` | 200-500 | DONE (`app.android`, 350) |
| D23 | 桌面应用 / 跨平台GUI | `client.desktop-cross-platform` | 200-500 | TODO |
| D24 | 游戏开发 | `client.game-dev` | 200-500 | TODO |
| D25 | 图形学 / 渲染 | `client.graphics-rendering` | 200-500 | TODO |

## F. AI / ML / 数据科学

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D26 | 经典机器学习 | `ai.classic-ml` | 200-500 | TODO |
| D27 | 深度学习 / 神经网络 | `ai.deep-learning` | 200-500 | TODO |
| D28 | NLP / 大语言模型 | `ai.nlp-llm` | 200-500 | TODO |
| D29 | 计算机视觉 | `ai.computer-vision` | 200-500 | TODO |
| D30 | 推荐系统 | `ai.recommender-systems` | 200-500 | TODO |
| D31 | MLOps / 模型工程 | `ai.mlops` | 200-500 | TODO |
| D32 | 强化学习 | `ai.reinforcement-learning` | 200-500 | TODO |

## G. 科学计算与仿真

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D33 | 数值计算 / 科学计算 | `science.scientific-computing` | 200-500 | TODO |
| D34 | 物理仿真 / 有限元 | `science.physics-simulation-fem` | 200-500 | TODO |
| D35 | 信号处理 / DSP | `science.signal-processing-dsp` | 200-500 | TODO |
| D36 | 计算几何 / CAD | `science.computational-geometry-cad` | 200-500 | TODO |
| D37 | 生物信息学 | `science.bioinformatics` | 200-500 | TODO |
| D38 | 量子计算 | `science.quantum-computing` | 200-500 | TODO |

## H. 基础设施与平台

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D39 | 云计算 / IaaS | `infra.cloud-iaas` | 200-500 | TODO |
| D40 | 容器与编排 | `infra.containers-orchestration` | 200-500 | TODO |
| D41 | 消息队列 / 事件流 | `infra.mq-event-streaming` | 200-500 | TODO |
| D42 | 可观测性 / 监控 | `infra.observability-monitoring` | 200-500 | TODO |
| D43 | IaC / 基础设施即代码 | `infra.iac` | 200-500 | TODO |

## I. 特定领域应用

| ID | 子领域 | 建议 profile slug | 词数目标 | 状态 |
|---|---|---|---|---|
| D44 | 区块链 / Web3 | `domain.blockchain-web3` | 200-500 | TODO |
| D45 | 音视频处理 / 流媒体 | `domain.audio-video-streaming` | 200-500 | TODO |
| D46 | GIS / 地理信息 | `domain.gis` | 200-500 | TODO |
| D47 | 机器人 / ROS | `domain.robotics-ros` | 200-500 | TODO |
| D48 | 编辑器 / IDE / 开发者工具 | `domain.editor-ide-tooling` | 200-500 | TODO |
| D49 | 数据可视化 | `domain.data-visualization` | 200-500 | TODO |
| D50 | 低代码 / DSL / 配置语言 | `domain.lowcode-dsl-config` | 200-500 | TODO |

---

## 每个领域完成定义（DoD）

1. 生成词典源文件：`docs/samples/profile-dictionaries/profile.<slug>.txt`。
2. 词条数量在 `200-500`。
3. 词条格式符合 `词条 pinyin cost`。
4. 已补至少 5 条领域回归 query（可追加到 `docs/profile-dict-manual-test.md`）。
5. 在 Checklist 中把对应领域状态从 `TODO/DOING` 更新为 `DONE`。

注意：当前实现中，新增领域若要在 App 内“重导入内置词库”自动生效，需要同步更新  
`ProfileDictionaryCatalog.entries`（路径：`app/src/main/java/org/fcitx/fcitx5/android/projectdict/profile/ProfileDictionaryService.kt`）。
