# Profile 示例词典（Phase 3.5 / Slice 1）

本目录提供“按上下文激活词库 profile”功能的首批词典源文件，格式为 libime 拼音文本词典（`词条 pinyin cost`）。

## 文件清单

- `profile.base.txt`
- `profile.frontend.txt`
- `profile.frontend.react.txt`
- `profile.backend.java.txt`
- `profile.backend.go.txt`
- `profile.backend.rust.txt`
- `profile.app.android.txt`
- `profile.business.crm.txt`
- `profile.business.erp.txt`

## 启动即 Ready（目标形态）

Slice 1 的目标不是手工导入，而是“App 启动后词典已就绪”：

1. 本目录 `*.txt` 作为 profile 词典源文件（repo 内维护）。
2. 构建/打包阶段将其转换并放入 `app/src/main/assets/usr/share/fcitx5/pinyin/dictionaries/`。
3. 安装并启动 App 后，这批 profile 词典默认可见、可启停，无需用户手工导入。
4. UI 侧只负责启停与应用重载，不再要求先做导入动作。

## 调试兜底（非主路径）

若在开发阶段需要快速试验单个词典，仍可临时通过“拼音词典管理”导入 `*.txt`，但这不属于 Slice 1 的验收路径。

## cost 建议

- `base`: 0.00 ~ 0.08
- 领域 profile: 0.08 ~ 0.22
- 保持温和提权，不要把 cost 拉太高。

## 备注

- 示例词典用于 Slice 1 内置词典能力验证与体验调参。
- 真实项目可按同命名规则扩展 profile。
- GitHub 承载的“云端词典同步”属于后续阶段，不在 Slice 1 范围内。
- 日常调词与回归模板见：`docs/profile-dict-ops-template.md`
- 全领域清单见：`docs/profile-dict-domain-checklist.md`
- 执行任务编排见：`docs/plan/v3-profile-domain-dictionary-backlog.md`
