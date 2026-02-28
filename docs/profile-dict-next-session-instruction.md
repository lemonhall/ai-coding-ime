# 下一会话执行指令（单领域词库产出）

用途：每次新会话直接复制本页指令，稳定推进一个领域一个领域的词库建设。

当前矩阵（2026-02-28）：
- 领域范围：`D01-D74`
- 当前状态：`DONE=11`，`SEEDED=2`，`TODO=61`
- 统一进度看板：`docs/profile-dict-domain-checklist.md`

先读：

- [`docs/profile-dict-domain-checklist.md`](./profile-dict-domain-checklist.md)
- [`docs/plan/v3-profile-domain-dictionary-backlog.md`](./plan/v3-profile-domain-dictionary-backlog.md)
- [`docs/profile-dict-ops-template.md`](./profile-dict-ops-template.md)

---

## 可直接复制给新会话的 Prompt

```text
请按以下要求执行“单领域 profile 词库产出”任务（一次只做 1 个领域）：

1) 先读取：
- docs/profile-dict-domain-checklist.md
- docs/plan/v3-profile-domain-dictionary-backlog.md
- docs/profile-dict-ops-template.md

2) 选择 checklist 中一个状态为 TODO 或 SEEDED 的领域，把它先改成 DOING。

3) 为该领域产出词库文件（200-500 条）：
- docs/samples/profile-dictionaries/profile.<slug>.txt
词条格式必须是：
词条 pinyin cost

4) 同步词库到内置资产：
- app/src/main/assets/projectdict/profile-dictionaries/profile.<slug>.txt

5) 如果 <slug> 是新增领域（不在 catalog 中），请同步更新：
- app/src/main/java/org/fcitx/fcitx5/android/projectdict/profile/ProfileDictionaryService.kt
里的 ProfileDictionaryCatalog.entries（默认 defaultEnabled=false；base 例外）。

6) 在 docs/profile-dict-manual-test.md 追加至少 5 条该领域的回归 query（含期望）。

7) 把 checklist 中该领域状态从 DOING 改为 DONE，并记录实际词条数。

8) 运行验证：
- ./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*" --console=plain

9) 最后输出：
- 本次选的领域 ID/名称
- 新增或修改的文件列表
- 实际词条数
- 回归 query 摘要
- 验证命令结果
```

---

## 会话后自检

- 只完成了 1 个领域（没有跨领域扩张）
- 词条数在 200-500
- 资产与样例目录文件一致
- checklist/backlog/手工回归文档已更新
