# 下一会话执行指令（词库维护 / 回归）

用途：在 `D01-D74` 已全部落地后，指导后续会话做质量维护、调词和回归。

当前矩阵（2026-03-01）：
- 领域范围：`D01-D74`
- 当前状态：`DONE=74`，`DOING=0`，`SEEDED=0`，`TODO=0`
- 统一进度看板：`docs/profile-dict-domain-checklist.md`
- 实现侧 catalog 现状：`79` 项（`74` 领域 + `5` 个非领域兼容 profile）

先读：

- [`docs/profile-dict-domain-checklist.md`](./profile-dict-domain-checklist.md)
- [`docs/plan/v3-profile-domain-dictionary-backlog.md`](./plan/v3-profile-domain-dictionary-backlog.md)
- [`docs/profile-dict-ops-template.md`](./profile-dict-ops-template.md)

---

## 可直接复制给新会话的 Prompt（维护模式）

```text
请按以下要求执行“单领域 profile 词库维护”任务（一次只做 1 个领域）：

1) 先读取：
- docs/profile-dict-domain-checklist.md
- docs/plan/v3-profile-domain-dictionary-backlog.md
- docs/profile-dict-ops-template.md

2) 从 checklist 里任选 1 个已完成领域（DONE），作为本次维护对象。

3) 在不改领域边界的前提下，对该领域词库做维护（例如补词、去噪、调 cost）：
- docs/samples/profile-dictionaries/profile.<slug>.txt

4) 同步到内置资产：
- app/src/main/assets/projectdict/profile-dictionaries/profile.<slug>.txt

5) 若本次涉及非领域兼容 profile（`base` / `frontend.react` / `backend.java` / `backend.go` / `backend.rust`），同步确认：
- app/src/main/java/org/fcitx/fcitx5/android/projectdict/profile/ProfileDictionaryService.kt
里的 catalog 条目与资产文件一致。

6) 在 docs/profile-dict-manual-test.md 为该领域补充或更新至少 5 条回归 query（含期望）。

7) 运行验证：
- ./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*" --console=plain

8) 最后输出：
- 本次维护的领域 ID/名称
- 新增或修改的文件列表
- 词条变更摘要（新增/删除/调权）
- 回归 query 摘要
- 验证命令结果
```

---

## 会话后自检

- 只维护了 1 个领域（没有跨领域扩张）
- 资产与样例目录文件一致
- 回归 query 和验证命令已执行
- 如有状态变化，checklist/backlog 已同步
