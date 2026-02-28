# 项目词库人工测试指引（ProjectDict）

## 1. 测试目标

验证以下能力是否正常：
- 词库加载（剪贴板/文件）
- 候选词注入（带 `[P]` 或 `[P:hint]`）
- 查询规则（ID/ABBR/TERM/PHRASE，含拼音全拼/首字母）
- 误触拼音容错召回（JNI/libime fuzzy）
- 热替换与清空
- 权重排序与 Top5 限制
- 异常词条容错（坏行不崩溃）

## 2. 一次性准备

1. 在手机上启用输入法 `Fcitx5 for Android`。
2. 打开 `Fcitx5` 主应用。
3. 进入：`Android` -> `项目词库`（Project Dictionary）。
4. 准备一个可输入的测试页面（推荐：Termux、任意记事本、聊天输入框）。
5. 在输入法里确保当前是中文拼音输入（不要切到纯英文直通）。

## 3. 测试词库文本（可直接复制）

### 词库 A（基础回归，13 条）

```tsv
getUserInfo	id	950
setConfig	id	920
handleError	id	880
user_repository	id	850
ProjectConfig	id	840
API_BASE_URL	id	800
数据迁移	term	900	database migration
项目配置	term	880	project config
幂等性	term	750	idempotency
重新部署	phrase	700
回滚到上一个版本	phrase	650
cfg	abbr	600	config/configuration
repo	abbr	580	repository
```

### 词库 B（热替换验证）

```tsv
zzOnlyToken	id	999
```

### 词库 C（排序 + Top5 验证）

```tsv
fooA	id	100
fooB	id	900
fooC	id	800
fooD	id	700
fooE	id	600
fooF	id	500
```

### 词库 D（容错验证）

```tsv
okOne	id	900
badType	xxx	800
badWeight	id	abc
okTwo	abbr	700	ok
```

## 4. 测试场景（14 个）

### 场景 1：从剪贴板加载词库 A

操作：
1. 复制“词库 A”全文到系统剪贴板。
2. 打开 `Fcitx5` -> `Android` -> `项目词库`。
3. 点击 `从剪贴板加载`（Load from clipboard）。

期望：
- 出现“已加载 N 条词条”的提示。
- 页面里的 `词库信息` 不再是“未加载词库”。

---

### 场景 2：词库信息显示已加载

操作：
1. 保持在 `项目词库` 页面。
2. 查看 `词库信息` 一项。

期望：
- 显示类似 `Project: clipboard, Entries: 13`（或同义本地化文本）。

---

### 场景 3：ID 前缀匹配（getu）

操作：
1. 打开测试输入框。
2. 用拼音状态输入：`getu`（不要立刻空格上屏）。

期望：
- 候选列表中出现 `getUserInfo [P]`。
- 位置应在较靠前区域（通常会插到前部）。

---

### 场景 4：ID 大小写不敏感（api_ / API_）

操作：
1. 输入 `api_`（再试一次 `API_`）。

期望：
- 候选中出现 `API_BASE_URL [P]`。
- 小写和大写输入都能匹配到该词条。

---

### 场景 5：ABBR 精确匹配（cfg）

操作：
1. 输入 `cfg`。

期望：
- 候选中出现 `cfg [P:config/configuration]`（带 hint）。

---

### 场景 6：ABBR 前缀匹配（re）

操作：
1. 输入 `re`。

期望：
- 候选中出现 `repo [P:repository]`。

---

### 场景 7：另一个 ID 前缀匹配（user_）

操作：
1. 输入 `user_`。

期望：
- 候选中出现 `user_repository [P]`。

---

### 场景 8：清空词库后立即失效

操作：
1. 打开 `项目词库` 页面。
2. 点击 `清空词库`。
3. 回到输入框，再输入 `getu` 或 `cfg`。

期望：
- 词库候选（带 `[P]` 标记）不再出现。
- `词库信息` 显示未加载状态。

---

### 场景 9：热替换（A -> B）

操作：
1. 先加载词库 A，确认 `getu` 能出 `getUserInfo [P]`。
2. 再复制并加载词库 B。
3. 输入 `getu`，再输入 `zz`。

期望：
- `getUserInfo [P]` 消失（旧词库已被替换）。
- `zzOnlyToken [P]` 出现（新词库生效）。

---

### 场景 10：权重排序（词库 C）

操作：
1. 复制并加载词库 C。
2. 输入 `foo`。

期望：
- 候选顺序按权重高到低，前面优先看到：`fooB` > `fooC` > `fooD` > `fooE` > `fooF`。

---

### 场景 11：Top5 限制（词库 C）

操作：
1. 保持词库 C。
2. 输入 `foo` 并展开观察候选。

期望：
- 最多只注入 5 个项目词库候选。
- `fooA`（权重最低）不应在注入的前 5 个里出现。

---

### 场景 12：容错（词库 D，坏行不崩溃）

操作：
1. 复制并加载词库 D。
2. 输入 `okO`、`okT`，再输入 `bad`。

期望：
- `okOne [P]`、`okTwo [P:ok]` 可以正常匹配。
- `badType`、`badWeight` 不应作为有效词条出现。
- 应用不崩溃、不闪退。

---

### 场景 13：TERM/PHRASE 拼音匹配（全拼/首字母）

操作：
1. 切回词库 A。
2. 输入 `shuju`、`sjqy`、`huigun`。

期望：
- `shuju`、`sjqy` 能召回 `数据迁移 [P:database migration]`。
- `huigun` 能召回 `回滚到上一个版本 [P]`。

---

### 场景 14：误触拼音容错召回（JNI fuzzy）

操作：
1. 保持词库 A。
2. 输入 `hyigun`、`huugun`（可补测 `hyi`、`huu`）。

期望：
- 误触输入也能召回 `回滚到上一个版本 [P]`。
- 若同时存在精确命中，精确命中排序应高于容错召回。

## 5. 已知限制（当前实现）

- 尚未实现与 SSH 终端联动的自动词库热加载（`Phase 3.2`）。
- 尚未实现 Intent 调用方签名校验与 payload 限流（`Phase 3.3`）。
- 尚未建立 ProjectDict 的集成测试与性能基准自动化（`Phase 4.2/4.3`）。

## 6. 建议执行顺序

1. 场景 1 -> 7（基础能力）
2. 场景 8 -> 9（状态切换）
3. 场景 10 -> 11（排序和上限）
4. 场景 12（容错）
5. 场景 13 -> 14（拼音与容错召回）

完成这 14 项后，基本可以覆盖当前 ProjectDict 的核心手工回归面。

## 7. 相关文档

- `Phase 3.5`（上游 profile 词库启停）请参考：
  - `docs/profile-dict-manual-test.md`
