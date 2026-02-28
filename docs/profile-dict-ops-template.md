# Profile 词库运营模板（调词/调 cost/回归）

适用范围：`Phase 3.5` 及后续 profile 词库迭代。  
目标：在不改底层算法的前提下，稳定提升候选体验。

关联文档：

- 领域清单：`docs/profile-dict-domain-checklist.md`
- 执行 backlog：`docs/plan/v3-profile-domain-dictionary-backlog.md`
- 下一会话指令：`docs/profile-dict-next-session-instruction.md`

---

## 1. 维护对象

- 词库源文件目录：`docs/samples/profile-dictionaries/`
- App 内置资产目录：`app/src/main/assets/projectdict/profile-dictionaries/`
- 运行时验证入口：`设置 -> Project Dictionary -> Context Profiles`

---

## 2. 词条格式规范

当前采用 libime 文本词典格式：

```text
词条 pinyin cost
```

示例：

```text
客诉工单 ke'su'gong'dan 0.18
工单流转 gong'dan'liu'zhuan 0.17
```

规则：

1. `词条`：优先放完整业务词，避免只放碎片词。
2. `pinyin`：使用全拼音节，音节间用 `'` 分隔。
3. `cost`：使用小数，按下方分级策略填写。
4. 去重：同一 profile 内不重复词条；跨 profile 重复词允许，但 cost 需有层级差异。
5. 变体：缩写/别名建议单独成词条（如 `crm`、`客诉`），并设置略低权重。

---

## 3. cost 分级模板

建议先按“温和提权”策略调参，避免候选霸榜。

| 层级 | 目标 | base profile | 领域 profile |
|---|---|---|---|
| T1 核心高频 | 每天都用、应稳定前移 | 0.05 ~ 0.08 | 0.14 ~ 0.20 |
| T2 常用词 | 高频但不应压过通用词 | 0.04 ~ 0.07 | 0.11 ~ 0.16 |
| T3 长尾词 | 可召回即可，少干预排序 | 0.02 ~ 0.05 | 0.08 ~ 0.13 |

调参原则：

1. 单次改动优先控制在 `±0.01 ~ ±0.03`。
2. 若词条“霸榜”，先降 `0.02` 观察，不要一次大幅重写全表。
3. 优先调高“完整业务词”，谨慎调高单字/短词。

---

## 4. 词库编辑工作流

1. 编辑 `docs/samples/profile-dictionaries/profile.<domain>.txt`。
2. 同步到内置资产目录（保持同名文件）。
3. 在 App 中点击“重导入内置词库”。
4. 根据目标场景开启对应 profile，点击“应用并重载”。
5. 执行回归 query 清单并记录结果。

---

## 5. 回归 Query 清单模板

每次词库改动至少补 5-10 条 query，覆盖“命中、误召回、边界词”。

| Query | 目标词条 | 激活 profile | 期望 |
|---|---|---|---|
| `kesugongdan` | `客诉工单` | `base + business.crm` | Top3 出现目标词 |
| `gongdan` | `工单流转` | `base + business.crm` | Top5 出现目标词，且不被无关词压制 |
| `bianyi` | `编译缓存` | `base` | Top3 稳定 |
| `gouzi` | `自定义钩子` | `base + frontend.react` | react 开启时命中，关闭后明显下降 |
| `gouzi` | （react 词） | `base + frontend` | 关闭 react 后不应强前移 |

验收记录模板：

```text
[日期]
改动 profile:
改动词条数:
改动 cost 区间:

Query 通过率: x / y
主要问题:
- ...

下一步:
- ...
```

---

## 6. 提交前检查

1. 词典文件无格式错误（每行 3 列）。
2. 不存在明显重复词条与冲突拼音。
3. 至少完成一轮手工回归（见 `docs/profile-dict-manual-test.md`）。
4. 关键验证命令通过：  
   `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*"`
