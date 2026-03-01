# Meta Validation Policy (D01-D74 Mode)

## Scope

- Target output: `.ime/meta.json`
- Tag domain: `D01-D74` only
- 选择策略：由 AI 手工分析项目后决定，不由脚本自动推荐。
- 校验职责：由 `scripts/validate_meta.py` 负责格式和范围约束。

## Important Distinction

Current profile catalog has more than 74 entries (79 in this repo snapshot), but D01-D74 mode intentionally constrains tagging to the 74 domain set.

Non-74 extras are excluded in this mode:

- `base`
- `frontend.react`
- `backend.java`
- `backend.go`
- `backend.rust`

`base` remains a runtime activation policy concern, not a project domain tag.

## Mandatory Validation Rules

1. `dict_profiles` 必须是 5 个唯一词典名。
2. `dict_profiles` 必须包含 `engineering.testing`。
3. `dict_profiles` 全部必须在 D01-D74 允许列表中。
4. `tags` 若存在，也必须全部在 D01-D74 列表中，且至少覆盖 `dict_profiles`。

## Output Schema

```json
{
  "version": 1,
  "project": "example",
  "lang": ["zh", "en"],
  "generated_at": "2026-03-01T12:00:00Z",
  "description": "Manual ProjectDict profile selection (D01-D74 constrained)",
  "dict_profiles": ["..."],
  "tags": ["..."]
}
```

## Validation Command

```bash
python skills/projectdict-meta-autotag/scripts/validate_meta.py --meta .ime/meta.json
```

## Review Checklist

- `dict_profiles` 数量等于 5。
- `engineering.testing` 已包含。
- 所有词典名都在 D01-D74 列表。
- JSON 结构与字段类型正确。
