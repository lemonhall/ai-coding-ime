---
name: projectdict-meta-autotag
description: Use when choosing `.ime/meta.json` `dict_profiles` for a coding project and enforcing ai-coding-ime D01-D74 constraints, exactly 5 profiles, and mandatory `engineering.testing`.
---

# ProjectDict Meta Autotag

## Overview

这个技能用于指导 AI 自己读项目并判断最相关词典，然后写入 `.ime/meta.json`。  
Python 脚本只负责校验，不负责扫描、打分、推荐。

## Quick Start

```bash
python skills/projectdict-meta-autotag/scripts/validate_meta.py \
  --meta /path/to/project/.ime/meta.json
```

## Core Rules

- `dict_profiles` 必须是 **恰好 5 个**唯一词典名。
- `dict_profiles` 必须包含 `engineering.testing`（测试词典必加载）。
- `dict_profiles` 只能来自 D01-D74 允许列表（见下文硬编码清单）。
- AI 必须自己根据项目性质做判断，不用脚本做扫描或打分。
- 校验脚本只做格式和范围约束检查。

## Workflow (AI 手工判断)

1. 阅读项目证据，至少覆盖这些位置：
   - `README*`、`docs/*`
   - 构建文件（如 `build.gradle*`, `settings.gradle*`, `flake.nix`, `CMakeLists.txt`）
   - 主要源码目录（如 `app/src/main/*`, `src/*`）
   - 测试目录（如 `test`, `androidTest`, `spec`）
2. 先固定一个词典：`engineering.testing`。
3. 再选 4 个与项目最相关的 D01-D74 词典，总数达到 5 个。
4. 写入 `.ime/meta.json` 的 `dict_profiles`。
5. 运行校验脚本；失败就修复直到通过。

## Meta Template

```json
{
  "version": 1,
  "project": "example-project",
  "lang": ["zh", "en"],
  "generated_at": "2026-03-01T12:00:00Z",
  "description": "Manual ProjectDict profile selection (D01-D74 constrained)",
  "dict_profiles": [
    "engineering.testing",
    "app.android",
    "engineering.package-build",
    "engineering.software-architecture",
    "domain.editor-ide-tooling"
  ],
  "tags": [
    "engineering.testing",
    "app.android",
    "engineering.package-build",
    "engineering.software-architecture",
    "domain.editor-ide-tooling"
  ]
}
```

## D01-D74 Allowed Profiles (Hardcoded)

```text
D01 hardware.cpu-architecture
D02 hardware.embedded-iot
D03 hardware.os-kernel
D04 hardware.driver-hal
D05 hardware.fpga-hdl
D06 network.protocols
D07 network.web-backend-api
D08 frontend
D09 network.security-crypto
D10 network.distributed-systems
D11 data.relational-db
D12 data.nosql-newsql
D13 data.engineering-etl
D14 data.search-ir
D15 engineering.compiler-interpreter
D16 engineering.plt
D17 engineering.software-architecture
D18 engineering.devops-sre
D19 engineering.vcs-collaboration
D20 engineering.testing
D21 engineering.package-build
D22 app.android
D23 client.desktop-cross-platform
D24 client.game-dev
D25 client.graphics-rendering
D26 ai.classic-ml
D27 ai.deep-learning
D28 ai.nlp-llm
D29 ai.computer-vision
D30 ai.recommender-systems
D31 ai.mlops
D32 ai.reinforcement-learning
D33 science.scientific-computing
D34 science.physics-simulation-fem
D35 science.signal-processing-dsp
D36 science.computational-geometry-cad
D37 science.bioinformatics
D38 science.quantum-computing
D39 infra.cloud-iaas
D40 infra.containers-orchestration
D41 infra.mq-event-streaming
D42 infra.observability-monitoring
D43 infra.iac
D44 domain.blockchain-web3
D45 domain.audio-video-streaming
D46 domain.gis
D47 domain.robotics-ros
D48 domain.editor-ide-tooling
D49 domain.data-visualization
D50 domain.lowcode-dsl-config
D51 business.erp
D52 business.crm
D53 business.hrm
D54 business.finance-payments
D55 business.supply-chain-logistics-wms
D56 business.ecommerce-platform
D57 business.oa-workflow
D58 product.management
D59 product.growth-operations
D60 product.adtech-martech
D61 product.content-community-ops
D62 product.seo-sem-aso
D63 product.analytics-bi
D64 product.ab-testing
D65 industry.fintech
D66 industry.insurtech
D67 industry.healthtech
D68 industry.edtech
D69 industry.proptech
D70 industry.mobility-lbs
D71 industry.saas-subscription
D72 compliance.data-privacy
D73 compliance.infosec
D74 compliance.cn-regulation
```

## Quick Reference

| Task | Command |
|---|---|
| Validate current repo meta | `python skills/projectdict-meta-autotag/scripts/validate_meta.py --meta .ime/meta.json` |
| Validate another repo meta | `python skills/projectdict-meta-autotag/scripts/validate_meta.py --meta /path/to/proj/.ime/meta.json` |
| Print all allowed D01-D74 slugs | `python skills/projectdict-meta-autotag/scripts/validate_meta.py --print-allowed` |

## Common Mistakes

| Mistake | Fix |
|---|---|
| `dict_profiles` 少于或多于 5 个 | 调整到恰好 5 个 |
| 忘记 `engineering.testing` | 强制加入并再选其余 4 个 |
| 误用 `base` / `frontend.react` / `backend.*` | 改为 D01-D74 清单内词典 |
| 只写 `dict_profiles` 不写 `tags` | 先让 `tags` 与 `dict_profiles` 保持一致 |

## Red Flags

- 依赖脚本“自动推荐词典”。
- `dict_profiles` 不是 5 个。
- 没有 `engineering.testing`。
- 出现非 D01-D74 的词典名。

## Resources

- Script: `scripts/validate_meta.py`
- Policy notes: `references/meta-generation-policy.md`
