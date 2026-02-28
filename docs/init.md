# ai-coding-ime — 项目词库增强输入法 点火文档

## 相关文档

- JNI 容错召回施工计划：`docs/plan/v1-projectdict-jni-fuzzy-recall.md`

## 项目目标

基于 Fcitx5 Android（https://github.com/fcitx5-android/fcitx5-android）进行 fork 改造，增加"项目词库"功能。核心场景：用户在 Android 终端通过 SSH 连接远端项目时，输入法自动加载该项目的 AI 生成词库（`.ime/dict.tsv`），使中英文输入都能感知项目上下文。

Fork 仓库：https://github.com/lemonhall/ai-coding-ime

## 前置知识

### 上游仓库信息
- 仓库：`fcitx5-android/fcitx5-android`
- 最新 Release：0.1.2（2025-11-01）
- 协议：LGPL-2.1-or-later
- 语言构成：Kotlin 88.4%，C++ 9.3%，CMake 2.0%
- 最新 commit：2026-02-17（master 分支，活跃维护中）

### 架构概览（五层）

```
┌─────────────────────────────────────────────┐
│  Android IME Service 层                      │
│  FcitxInputMethodService                     │
│  ├── InputView（虚拟键盘模式）                │
│  ├── CandidatesView（物理键盘浮动候选窗）     │
│  └── InputDeviceManager（设备模式切换）        │
├─────────────────────────────────────────────┤
│  Kotlin Core 层                              │
│  Fcitx.kt（单例，coroutine-based API）        │
│  ├── FcitxAPI 接口（public contract）         │
│  ├── FcitxEvent sealed class（事件类型定义）   │
│  ├── FcitxDispatcher（专用线程调度）           │
│  └── DataManager（插件发现与数据管理）         │
├─────────────────────────────────────────────┤
│  JNI Bridge 层（~700 行 C++）                │
│  native-lib.cpp                              │
│  ├── Kotlin→Native: sendKeyToFcitx*()        │
│  ├── Native→Kotlin: handleFcitxEvent()       │
│  ├── object-conversion.h（类型转换）          │
│  └── jni-utils.h（GlobalRefSingleton）        │
├─────────────────────────────────────────────┤
│  Fcitx5 Native 层                            │
│  ├── fcitx::Instance（引擎核心）              │
│  ├── AndroidFrontend（Android 前端适配）      │
│  ├── AndroidInputContext（per-app 输入上下文） │
│  └── InputContextCache（UID-based IC 缓存）   │
├─────────────────────────────────────────────┤
│  输入法引擎插件层                             │
│  ├── fcitx5-chinese-addons（拼音/双拼/五笔）  │
│  ├── plugin/rime（RIME 引擎）                 │
│  ├── plugin/anthy（日语）                     │
│  └── plugin/hangul（韩语）等                  │
└─────────────────────────────────────────────┘
```

### 关键源文件索引

| 文件 | 作用 | 改造相关度 |
|---|---|---|
| `app/src/main/java/org/fcitx/fcitx5/android/core/Fcitx.kt` | Fcitx5 的 Kotlin 封装，所有 JNI 调用入口 | ★★★ |
| `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxAPI.kt` | 公共 API 接口定义 | ★★☆ |
| `app/src/main/java/org/fcitx/fcitx5/android/core/FcitxEvent.kt` | 事件类型定义（sealed class） | ★★★ |
| `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt` | IME 服务主类，事件分发中心 | ★★★ |
| `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt` | 虚拟键盘视图，候选词显示 | ★★☆ |
| `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt` | 浮动候选窗（物理键盘模式） | ★★☆ |
| `app/src/main/java/org/fcitx/fcitx5/android/input/InputDeviceManager.kt` | 虚拟/物理键盘模式切换 | ★☆☆ |
| `app/src/main/cpp/native-lib.cpp` | JNI bridge 主文件 | ★☆☆ |
| `app/src/main/cpp/androidfrontend/androidfrontend.cpp` | Android 前端实现 | ★☆☆ |

### 候选词管线（改造的核心接入点）

候选词有两种模式：

1. Bulk Mode（虚拟键盘）：`CandidateListEvent` → `InputBroadcaster` → `HorizontalCandidateComponent`
2. Paged Mode（物理键盘）：`PagedCandidateEvent` → `CandidatesView` → `PagedCandidatesUi`

事件流：
```
用户按键 → FcitxInputMethodService
  → Fcitx.kt.sendKey() [Kotlin]
  → native-lib.cpp sendKeyToFcitx*() [JNI]
  → fcitx::Instance 处理 [C++]
  → AndroidFrontend 回调 [C++]
  → handleFcitxEvent() [JNI → Kotlin]
  → FcitxEvent.CandidateListEvent [Kotlin]
  → InputBroadcaster 分发 [Kotlin]
  → HorizontalCandidateComponent / CandidatesView 显示 [Kotlin]
```

## Phase 0：环境搭建与首次构建

### 0.1 构建环境：WSL2 Ubuntu 24 + Nix

项目自带 `flake.nix`，使用 Nix 管理所有构建依赖，无需手动安装 Android SDK/NDK/CMake。

#### 安装 Nix（如果 WSL2 中尚未安装）

```bash
# 安装 Nix（multi-user 模式）
sh <(curl -L https://nixos.org/nix/install) --daemon

# 启用 flakes 支持
mkdir -p ~/.config/nix
echo "experimental-features = nix-command flakes" >> ~/.config/nix/nix.conf

# 重启 shell 使配置生效
exec bash
```

#### 代理配置（WSL2 网络环境）

```bash
# 如果 WSL2 已配置本地代理转发（如 clash-verge 的 TUN 模式），直接用 127.0.0.1：
export http_proxy=http://127.0.0.1:7897 https_proxy=http://127.0.0.1:7897

# 如果 WSL2 需要走宿主机代理，使用宿主机局域网 IP：
# export http_proxy=http://192.168.50.250:7897 https_proxy=http://192.168.50.250:7897

# apt 走代理（临时）
sudo -E apt update

# git 走代理（仓库级，按实际代理地址填写）
git config --local http.proxy http://127.0.0.1:7897
git config --local https.proxy http://127.0.0.1:7897
```

注意：代理地址取决于 WSL2 的网络配置方式。如果用了 TUN 模式或 mirrored 网络，用 `127.0.0.1` 即可；否则需要用宿主机局域网 IP。

### 0.2 Clone 与构建

```bash
# 建议 clone 到 WSL2 原生文件系统（比 /mnt/e 快很多）
cd ~

# 配置代理（按实际情况选择地址）
export http_proxy=http://127.0.0.1:7897 https_proxy=http://127.0.0.1:7897

# Clone fork 仓库
git clone https://github.com/lemonhall/ai-coding-ime.git
cd ai-coding-ime

# 配置仓库级 git 代理
git config --local http.proxy http://127.0.0.1:7897
git config --local https.proxy http://127.0.0.1:7897

# 拉取所有 submodule（19 个，含嵌套子模块如 kenlm、anthy-unicode）
git submodule update --init --recursive

# 进入 Nix dev shell（不带 Android Studio，纯 CLI 构建）
# 首次执行会下载所有依赖，耗时较长
# 注意：nix shell 不会改变 prompt，用 echo $ANDROID_SDK_ROOT 验证是否进入
nix develop .#noAS

# 进入 nix shell 后，所有构建工具已就绪：
# - Android SDK Platform 35
# - Android SDK Build-Tools 35.0.1
# - Android NDK 28.0.13004108
# - CMake 3.31.6
# - JDK 17 (OpenJDK 17.0.15+6)
# - extra-cmake-modules
# - gettext, python3, icu
# 环境变量 ANDROID_SDK_ROOT / ANDROID_HOME / JAVA_HOME 已自动设置
# local.properties 已自动生成

# 验证环境
echo $ANDROID_SDK_ROOT   # 应输出 /nix/store/.../libexec/android-sdk
echo $JAVA_HOME          # 应输出 /nix/store/.../openjdk-17.0.15+6

# 构建 debug APK（首次构建含 C++ 层，实测约 55 分钟）
./gradlew assembleDebug

# 构建产物按 CPU 架构分包：
# app/build/outputs/apk/debug/org.fcitx.fcitx5.android-{commit}-{arch}-debug.apk
# 支持的架构：arm64-v8a, armeabi-v7a, x86, x86_64
```

### 0.3 安装到手机

WSL2 中 adb 通常无法直接识别 USB 设备，推荐在 Windows 侧用 adb 安装。

```bash
# 在 Windows PowerShell 中执行：

# 如果 adb 无法启动 daemon，先杀残留进程再重启
taskkill /F /IM adb.exe
adb kill-server
adb start-server

# 确认设备
adb devices

# 复制 WSL 中最新的 arm64-v8a debug APK 到 Windows Downloads
wsl.exe -e bash -lc 'cp "$(ls -t /home/lemonhall/ai-coding-ime/app/build/outputs/apk/debug/*arm64-v8a-debug.apk | head -n1)" /mnt/c/Users/lemon/Downloads/ime-debug.apk'
adb install -r "$env:USERPROFILE\Downloads\ime-debug.apk"

# 备选方案：adb over TCP
#   1. Windows 侧：adb tcpip 5555
#   2. WSL2 侧：adb connect <宿主机IP>:5555
#   3. WSL2 侧：./gradlew installDebug
```

建议每次安装前先看时间戳，确认是刚构建出来的产物：

```bash
ls -la /home/lemonhall/ai-coding-ime/app/build/outputs/apk/debug/
```

也可以用仓库脚本一键安装（在 Windows PowerShell 中执行）：

```powershell
.\scripts\install-latest-apk.ps1 -WslRepoPath /home/lemonhall/ai-coding-ime

# 如需从 Windows 一键触发 WSL 编译 + 安装
.\scripts\install-latest-apk.ps1 -WslRepoPath /home/lemonhall/ai-coding-ime -Build
```

### 0.3.1 在手机上启用输入法

1. 设置 → 系统和更新 → 语言和输入法 → 键盘和输入法
2. 找到 "Fcitx5 for Android"，打开开关（系统会弹安全提示，确认即可）
3. 打开任意输入框，点底部导航栏的键盘图标或下拉通知栏点"更改键盘"
4. 选择 Fcitx5

### 0.4 构建验证清单

- [x] `nix develop .#noAS` 成功进入 dev shell
- [x] `echo $ANDROID_SDK_ROOT` 输出非空路径（`/nix/store/.../libexec/android-sdk`）
- [x] `./gradlew assembleDebug` 成功，无报错（BUILD SUCCESSFUL in 55m 25s，745 tasks）
- [x] APK 安装到 Nova 9 上（通过 Windows 侧 adb install）
- [x] 在系统设置中启用 Fcitx5 输入法
- [ ] 拼音输入正常工作，候选词正常显示
- [ ] 切换英文输入正常

### 0.5 已知构建坑

1. 如果报 `No variants found for ':app'`，检查是否有 `_JAVA_OPTIONS` 或 `JAVA_TOOL_OPTIONS` 环境变量，在 nix shell 中执行 `unset _JAVA_OPTIONS JAVA_TOOL_OPTIONS`
2. submodule 拉取失败大概率是网络问题，确保代理配置正确
3. 首次构建含 C++ 层，实测耗时约 55 分钟（WSL2 原生文件系统，非 /mnt 挂载）
4. WSL2 中如果 `/mnt/e` 路径下构建很慢（Windows 文件系统 I/O 瓶颈），务必把项目 clone 到 WSL2 原生文件系统（如 `~/ai-coding-ime`），构建速度会快很多
5. `flake.nix` 中的版本号需要与 `build-logic/convention/src/main/kotlin/Versions.kt` 保持一致，如果上游更新了版本，两边都要改
6. `nix develop` 进入 shell 后不会改变 prompt，用 `echo $ANDROID_SDK_ROOT` 验证是否已进入
7. Windows 侧 adb 安装时，WSL 路径中的发行版名称需要精确匹配（如 `Ubuntu-24.04` 而非 `Ubuntu`），用 `wsl -l -q` 确认
8. adb daemon 启动失败（`could not read ok from ADB Server`）时，先 `taskkill /F /IM adb.exe` 杀残留进程，再 `adb start-server`
9. APK 按 CPU 架构分包，Nova 9（华为，Kirin）使用 `arm64-v8a` 版本

### 0.6 Nix dev shell 说明

项目提供两个 dev shell：

| Shell | 命令 | 说明 |
|---|---|---|
| `default` | `nix develop` | 包含 Android Studio Beta，适合 GUI 开发 |
| `noAS` | `nix develop .#noAS` | 不含 Android Studio，纯 CLI 构建，体积更小 |

`flake.nix` 中锁定的关键版本：
- CMake: 3.31.6
- Build-Tools: 35.0.1
- Platform-Tools: 35.0.2
- Platform: 35
- NDK: 28.0.13004108
- JDK: 17

### 0.7 开发提速模式（2026-02-28）

问题背景：默认 `assembleDebug` 会走多 ABI + native 任务链，首次或缓存失效时可达 ~53 分钟。

#### 本机提速配置（用户级）

`~/.gradle/gradle.properties`：

```properties
buildABI=arm64-v8a
buildTimestamp=0
org.gradle.daemon=true
org.gradle.caching=true
org.gradle.parallel=true
```

说明：
- `buildABI=arm64-v8a`：默认仅构建手机目标 ABI（大幅减少 native 任务图）
- `buildTimestamp=0`：避免每次时间戳变化导致增量失效
- `configuration-cache` 目前不要开启（项目自定义 `CMakeBuildInstallTask` 不兼容）

#### 仓库脚本（推荐）

新增脚本：`scripts/gradle-dev.sh`

```bash
# 常规开发（单 ABI + 增量友好参数）
./scripts/gradle-dev.sh

# 快速模式：跳过 native/CMake 安装链（仅 Kotlin/UI 迭代）
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --kotlin
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --assemble
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --install

# 更激进（可选）：再跳过 KSP/codegen（仅限未改注解/Room schema）
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --ultrafast --kotlin
```

#### fast 模式边界

- `--fast` 只适用于“不改 C++、不改 submodule、native 产物已存在”的迭代
- 如果改了 C++/submodule，或怀疑 native 缓存脏，回到非 fast 模式
- 不要在日常开发里执行 `clean`，否则会触发接近全量重编译
- `GRADLE_USER_HOME` 切到一个新目录时，Gradle Wrapper 会重新下载分发包（一次性）；为复用缓存，固定使用 `~/.gradle`

## 当前进度（截至 2026-02-28）

- Phase 0 ✅ 已完成（构建、安装与基础验证）
- Phase 1 ✅ 已完成（提交：`15b8468a`）
- Phase 2 ✅ 已完成（提交：`14142249`）
- Phase 3.1 ✅ 已完成（提交：`19f3a8c6`）
- Phase 3.2 ⏳ 未开始（SSH 终端联动/Intent 热加载）
- Phase 3.3 ⏳ 未开始（调用方签名校验 + payload 限流未实现）
- Phase 3.4 ✅ 已完成（JNI/libime 容错召回已落地，详见 `docs/plan/v1-projectdict-jni-fuzzy-recall.md`）
- Phase 3.5 ✅ Slice 1 已完成（手动启停 + 应用重载 + 内置词典 ready + 重导入）
- Phase 4 🚧 进行中（JVM 单测已补齐并通过，集成/性能待补）

## Phase 1：项目词库协议定义 ✅

**状态**：已完成（2026-02-27）

示例文件已创建在项目根目录：
- `.ime/dict.tsv` - 词库文件示例（13 条词条）
- `.ime/meta.json` - 元信息文件示例

### 1.1 词库文件格式

在远端项目根目录下创建 `.ime/dict.tsv`：

```tsv
# .ime/dict.tsv
# 项目词库协议 v1
# 格式：词条<TAB>类型<TAB>权重<TAB>上下文提示(可选)
#
# 类型：id=标识符, term=领域术语, abbr=缩写, phrase=常用短语
# 权重：0-1000，越大越优先
# 以 # 开头的行为注释

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

### 1.2 元信息文件

`.ime/meta.json`：

```json
{
  "version": 1,
  "project": "my-awesome-project",
  "lang": ["zh", "en"],
  "generated_at": "2026-02-27T07:13:00Z"
}
```

### 1.3 设计原则

- TSV 格式：`cat`/`grep`/`awk`/`sort` 直接可操作
- 词库文件是幂等的：AI agent 重新生成不会产生副作用
- 协议层与引擎层解耦：未来换引擎不影响词库格式

## Phase 2：Kotlin 层改造（核心工作）✅

**状态**：已完成（2026-02-27，提交 `14142249`）

### 2.1 新增文件清单

```
app/src/main/java/org/fcitx/fcitx5/android/projectdict/
├── ProjectDictManager.kt      # 词库管理器（加载、解析、缓存）
├── ProjectDictEntry.kt        # 词条数据类
├── ProjectDictParser.kt       # TSV 解析器
└── ProjectDictBooster.kt      # 候选词加权/注入逻辑
```

### 2.2 ProjectDictEntry 数据类

```kotlin
data class ProjectDictEntry(
    val text: String,           // 词条文本
    val type: EntryType,        // id / term / abbr / phrase
    val weight: Int,            // 0-1000
    val hint: String? = null    // 上下文提示（可选）
) {
    enum class EntryType { ID, TERM, ABBR, PHRASE }
}
```

### 2.3 ProjectDictParser

解析 `.ime/dict.tsv` 文件内容为 `List<ProjectDictEntry>`。

要求：
- 跳过空行和 `#` 开头的注释行
- 容错处理：字段不足时跳过该行，不崩溃
- 支持 UTF-8 编码

### 2.4 ProjectDictManager

职责：
- 接收词库文件内容（String），调用 Parser 解析
- 维护当前活跃的项目词库（内存中）
- 提供查询接口：`fun query(input: String): List<ProjectDictEntry>`
- 支持热替换：加载新词库时替换旧词库，无需重启输入法

查询逻辑：
- `id` 类型：前缀匹配（输入 `getU` 匹配 `getUserInfo`），大小写不敏感
- `term` 类型：中文前缀匹配 + 拼音前缀匹配（全拼/首字母）
- `abbr` 类型：精确匹配或前缀匹配
- `phrase` 类型：中文前缀匹配 + 拼音前缀匹配（全拼/首字母）
- 严格匹配未命中时：可走 JNI/libime 容错召回（Phase 3.4）

### 2.5 ProjectDictBooster — 候选词注入

这是最关键的改造点。当前接入位置在：
- `InputView.kt`（`CandidateListEvent` / 虚拟键盘模式）
- `CandidatesView.kt`（`PagedCandidateEvent` / 物理键盘模式）

策略：
1. 监听 `CandidateListEvent`（Bulk Mode）和 `PagedCandidateEvent`（Paged Mode）
2. 在引擎返回候选词后，用当前输入串查询 ProjectDictManager
3. 将匹配的项目词库词条插入候选列表的前部（或按权重混合排序）
4. 项目词库词条在 UI 上可以有视觉区分（如小标签 `[P]`），方便用户识别

接入点分析：

当前代码路径（简化）：

```kotlin
// InputView.kt
is FcitxEvent.CandidateListEvent -> {
    val boosted = ProjectDictBooster.boostCandidateList(it, currentPreeditText)
    broadcaster.onCandidateUpdate(boosted.data)
}

// CandidatesView.kt
is FcitxEvent.PagedCandidateEvent -> {
    val currentInput = inputPanel.preedit.toString()
    val boosted = ProjectDictBooster.boostPagedCandidate(it, currentInput)
    paged = boosted.data
    updateUi()
}
```

注意事项：
- Phase 2 仅改 Kotlin 层；Phase 3.4 允许在 app JNI 层做最小桥接改动
- 当前候选查询路径为同步执行；若词库规模增大，需要优先补异步化与性能回归
- 如果项目词库为空或未加载，行为与原版完全一致（零侵入）

## Phase 3：词库加载通道

### 3.1 初期方案：手动加载

**状态**：已完成（2026-02-27，提交 `19f3a8c6`）

在 app 的设置界面中增加一个"项目词库"入口：
- 支持从文件选择器导入 `.tsv` 文件
- 支持从剪贴板粘贴 TSV 内容
- 当前显示词库信息：项目名 + 词条数（`generated_at` 暂未接入 UI）

### 3.2 进阶方案：与 SSH 终端联动

**状态**：未开始（尚未实现 `ACTION_LOAD_PROJECT_DICT` 接收链路）

这一步依赖柠檬叔自己的 SSH 终端 app。设计思路：
- SSH 终端 app 检测到 `cd` 到新项目目录时，检查 `.ime/dict.tsv` 是否存在
- 如果存在，通过 Android Intent 或 ContentProvider 将词库内容传递给输入法
- 输入法收到后调用 `ProjectDictManager.load()` 热加载

Intent 协议草案：
```
Action: org.fcitx.fcitx5.android.ACTION_LOAD_PROJECT_DICT
Extra:
  - "project_name": String
  - "dict_content": String (TSV 内容)
  - "meta_json": String (可选)
```

### 3.3 安全考虑

- Intent 接收端需要验证调用方签名，防止恶意 app 注入词库
- 词库内容做基本的大小限制（如 < 1MB），防止 OOM
- 词条数量上限（如 10000 条），防止查询性能退化

### 3.4 容错召回增强：复用 JNI/libime 能力（已完成）

**状态**：已完成（2026-02-28）

目标：
- 让项目词库对拼音误触输入具备与主拼音更接近的弹性召回能力。
- 典型目标输入：`hyi`、`huu`、`hyigun`、`huugun` 也能召回“回滚到上一个版本 [P]”。

设计约束：
- 复用 libime 已有 `Correction + Fuzzy` 能力，不自行在 Kotlin 重写一套拼音纠错算法。
- 仅允许在 app 层新增最小 JNI 桥接，不改 `lib/*` 与 `plugin/*` 上游/子模块源码。
- ProjectDict 保持“项目候选在前”的现有混合策略，但要对纠错命中做降权，避免误召回压过精确命中。

文档入口：
- 施工计划：`docs/plan/v1-projectdict-jni-fuzzy-recall.md`
- Kotlin 门面：`app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectDictNative.kt`
- JNI 实现：`app/src/main/cpp/native-lib.cpp` (`matchPinyinFuzzyBatchNative`)
- 管理器接入：`app/src/main/java/org/fcitx/fcitx5/android/projectdict/ProjectDictManager.kt`

### 3.5 上游词库分层激活（Context-Aware Profiles）

**状态**：Slice 1 已完成（2026-02-28），自动 context 激活待施工

目标：
- 仅使用上游词库能力做“词汇强调”，不继续扩大 ProjectDict 前插候选策略。
- 将“全局词库”拆成多份 profile（如 `base/frontend/react/backend/java/go/rust/android/crm/erp`）。
- 基于项目上下文（例如 `.ime/meta.json` 的 tags）自动激活词库子集，降低无关词干扰。

原则：
- 复用现有 `PinyinDictionary` 与 `.dict/.dict.disable` 启停机制，不改 `lib/*` 与 `plugin/*`。
- 通过词典 `cost` 做温和排序引导，不依赖“重复加词”来提权。
- 切换过程做差量启停 + 单次 `reloadPinyinDict()`，避免频繁重载抖动。

Slice 1（第一刀）交付门槛：
- 提供可见 UI 入口：可手动启停 profile 词典并应用重载。
- 提供首批内置词典资产：安装并启动后即 ready，无需手工导入。
- 提供“重导入内置词典”入口：当开发调词后可一键覆盖本地旧 profile。
- 提供独立人工测试指引，确保 UI/启停/重载可被直接验收。
- GitHub 承载的云端词典同步不在 Slice 1 范围内，后续阶段再评估。

文档入口：
- 设计文档：`docs/plan/v2-upstream-context-dictionary-profiles.md`
- 手工回归：`docs/profile-dict-manual-test.md`
- 示例词典：`docs/samples/profile-dictionaries/`

## Phase 4：测试验证

**状态**：进行中（JVM 单测已落地并通过；集成测试/性能基准未完成）

### 4.1 单元测试

- ✅ `ProjectDictParserTest`：验证 TSV 解析正确性和容错性
- ✅ `ProjectDictManagerTest`：验证前缀匹配、拼音匹配、JNI 容错召回与排序保护
- ✅ `ProjectDictBoosterTest`：验证候选词混合与索引映射逻辑
- 通过命令：`./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*"`

### 4.2 集成测试

- 加载一个真实项目的词库（~100 条），验证输入法候选词中出现项目词条
- 切换词库（模拟切换项目），验证旧词库被替换
- 空词库 / 格式错误的词库，验证不影响正常输入

### 4.3 性能基准

- 词库加载时间：1000 条词库应在 < 50ms 内完成解析和索引
- 查询延迟：单次查询应在 < 5ms 内返回结果
- 内存占用：1000 条词库的内存增量应 < 1MB

## 开发顺序（已更新）

1. ✅ **Phase 0** → 构建跑通，确认原版在 Nova 9 上正常工作
2. ✅ **Phase 1** → 定义词库格式，落地 `.ime/dict.tsv` / `.ime/meta.json`
3. ✅ **Phase 2** → 完成 ProjectDict 核心与候选词注入（Kotlin 层）
4. ✅ **Phase 3.1** → 完成手动加载入口（文件/剪贴板）
5. ✅ **Phase 3.4** → 复用 JNI/libime 做 ProjectDict 容错召回
6. ✅ **Phase 3.5** → Slice 1 已落地（UI 启停 + 应用重载 + 启动即 ready + 重导入入口 + 手工回归）
7. 🚧 **Phase 4** → 持续补齐集成测试与性能基准（单元测试已完成）
8. ⏳ **Phase 3.2 + 3.3** → 与 SSH 终端联动（Intent + 安全校验）

## 约束与原则

- Phase 2/3.1/3.2/3.3 以 Kotlin 层为主；Phase 3.4 允许在 app JNI 桥接层做最小改动
- Phase 3.5 优先复用上游词库启停/重载能力，避免继续增强 ProjectDict 前插“强干预”路径
- 项目词库功能是纯增量的，不修改任何现有功能的行为
- 词库未加载时，输入法行为与原版 100% 一致
- 遵循上游的代码风格和目录结构约定
- 目标设备：华为 Nova 9，API 30-31
- 构建环境：WSL2 Ubuntu 24.04 + Nix 2.33.3（使用项目自带的 flake.nix）
- WSL2 代理地址：127.0.0.1:7897（本地代理转发）或宿主机局域网 IP（视网络配置而定）
- Phase 0 完成时间：2026-02-27
- 文档状态更新时间：2026-02-28
