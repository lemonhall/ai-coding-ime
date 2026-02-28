# ai-coding-ime

基于 [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android) 的 fork，增加"项目词库"功能。核心场景：用户在 Android 终端通过 SSH 连接远端项目时，输入法自动加载该项目的 AI 生成词库（`.ime/dict.tsv`），使中英文输入都能感知项目上下文。

上游项目：[Fcitx5](https://github.com/fcitx/fcitx5) input method framework and engines ported to Android.

## Download

[<img src="https://github.com/rubenpgrady/get-it-on-github/raw/refs/heads/main/get-it-on-github.png" alt="Git it on GitHub" width="207" height="80">](https://github.com/fcitx5-android/fcitx5-android/releases/latest)
[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" width="207" height="80">](https://f-droid.org/packages/org.fcitx.fcitx5.android)
[<img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" width="207" height="80">](https://play.google.com/store/apps/details?id=org.fcitx.fcitx5.android)

You can also download the **latest CI build** on our Jeninks server: [![build status](https://img.shields.io/jenkins/build.svg?jobUrl=https://jenkins.fcitx-im.org/job/android/job/fcitx5-android/)](https://jenkins.fcitx-im.org/job/android/job/fcitx5-android/)

> [!NOTE]
> APKs downloaded from GitHub Release/F-Droid/Jenkins have the same signature, which means they're compatible when upgrading, but Google Play's do not.
> <details>
> <summary>(click here for detailed signature info)</summary>
> <ul>
> <li>Package Name: <code>org.fcitx.fcitx5.android</code></li>
> <li>Certificate SHA-256 fingerprint:</li>
> <ul>
> <li>GitHub Release/Jenkins/F-Droid</li>
> <code>E4:DB:1E:9E:DF:F1:36:29:D0:7D:E4:BB:F8:16:5F:E9:BD:85:57:AB:55:09:26:72:DA:8E:40:DB:E4:84:EC:D7</code>
> <li>Google Play</li>
> <code>06:53:6F:F6:E8:76:C0:14:E1:4B:44:6F:61:FA:2B:80:9E:06:67:39:A1:D1:17:0D:0A:7A:89:88:4C:48:00:33</code>
> </ul>
> </ul>
> </details>

In case you want Fcitx5 on other platforms: [macOS](https://github.com/fcitx-contrib/fcitx5-macos), [iOS](https://github.com/fcitx-contrib/fcitx5-ios), [HarmonyOS](https://github.com/fcitx-contrib/fcitx5-harmony), [ChromeOS](https://github.com/fcitx-contrib/fcitx5-chrome), [Windows](https://github.com/fcitx-contrib/fcitx5-windows); or [try Fcitx5 in the browser](https://fcitx-contrib.github.io/online/index.html)

## Project status

### Project Dictionary Progress (as of 2026-02-28)

| Phase | Status | Notes |
|---|---|---|
| Phase 1 | ✅ Done | 协议定义完成（`.ime/dict.tsv` / `.ime/meta.json`） |
| Phase 2 | ✅ Done | Kotlin 核心完成（`ProjectDictEntry/Parser/Manager/Booster` + 候选注入） |
| Phase 3.1 | ✅ Done | 手动加载完成（设置页支持文件和剪贴板导入） |
| Phase 3.2 | ⏳ Pending | SSH 终端联动未实现（`ACTION_LOAD_PROJECT_DICT` 仍是草案） |
| Phase 3.3 | ⏳ Pending | 安全校验未实现（调用方签名校验 + 词库 payload 限流） |
| Phase 3.4 | ✅ Done | JNI/libime 容错召回已落地（`ProjectDictNative` + `native-lib.cpp`） |
| Phase 3.5 | ✅ Slice 1 Done | 上游词库分层激活第一刀已落地（UI 启停 + 应用重载 + 内置词典 ready + 重导入按钮 + 手工回归） |
| Phase 4 | 🚧 In progress | JVM 单测已落地并通过；集成测试与性能基准待补齐 |

- Latest focused verification:
  - `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.projectdict.*"` (`BUILD SUCCESSFUL`)
- Phase 3.5 slice-1 artifacts:
  - `docs/plan/v2-upstream-context-dictionary-profiles.md`
  - `docs/samples/profile-dictionaries/`
  - `docs/profile-dict-manual-test.md`
  - `docs/profile-dict-ops-template.md`
  - `docs/profile-dict-domain-checklist.md`
  - `docs/plan/v3-profile-domain-dictionary-backlog.md`
  - `docs/profile-dict-next-session-instruction.md`
  - Profile 矩阵状态（2026-02-28）：`D01-D74`，`DONE=11`，`SEEDED=2`，`TODO=61`
  - Note: GitHub 承载/云端同步不在 Slice 1 范围内

### Supported Languages

- English (with spell check)
- Chinese
  - Pinyin, Shuangpin, Wubi, Cangjie and custom tables (built-in, powered by [fcitx5-chinese-addons](https://github.com/fcitx/fcitx5-chinese-addons))
  - Zhuyin/Bopomofo (via [Chewing Plugin](./plugin/chewing))
  - Jyutping (via [Jyutping Plugin](./plugin/jyutping/), powered by [libime-jyutping](https://github.com/fcitx/libime-jyutping))
- Vietnamese (via [UniKey Plugin](./plugin/unikey), supports Telex, VNI and VIQR)
- Japanese (via [Anthy Plugin](./plugin/anthy))
- Korean (via [Hangul Plugin](./plugin/hangul))
- Sinhala (via [Sayura Plugin](./plugin/sayura))
- Thai (via [Thai Plugin](./plugin/thai))
- Generic (via [RIME Plugin](./plugin/rime), supports importing custom schemas)

### Implemented Features

- Virtual Keyboard (layout not customizable yet)
- Expandable candidate view
- Clipboard management (plain text only)
- Theming (custom color scheme, background image and dynamic color aka monet color after Android 12)
- Popup preview on key press
- Long press popup keyboard for convenient symbol input
- Symbol and Emoji picker
- Plugin System for loading addons from other installed apk
- Floating candidates panel when using physical keyboard
- Project Dictionary (manual load from file/clipboard, candidate boosting with `[P]` labels, pinyin prefix + JNI fuzzy recall)

### Planned Features

- Customizable keyboard layout
- More input methods (via plugin)
- SSH terminal linked Project Dictionary auto-load with trusted-caller verification
- Project Dictionary integration tests and performance benchmarks

## Screenshots

|拼音, Material Light theme, key border enabled|自然码双拼, Pixel Dark theme, key border disabled|
|:-:|:-:|
|<img src="https://github.com/fcitx5-android/fcitx5-android/assets/13914967/bd429247-62d9-4c78-bab8-70ef3ce47588" width="360px">|<img src="https://github.com/fcitx5-android/fcitx5-android/assets/13914967/3ae969c1-7ed0-4f92-a5df-19dc8c90a8c3" width="360px">|

|Emoji picker, Pixel Light theme, key border enabled|Symbol picker, Material Dark theme, key border disabled|
|:-:|:-:|
|<img src="https://user-images.githubusercontent.com/13914967/202181845-6a5f6bb2-a877-468c-851a-fd7e66e64ed4.png" width="360px">|<img src="https://user-images.githubusercontent.com/13914967/202181861-dd253439-1d5e-4f5f-9535-934f28796a6b.png" width="360px">|

## Get involved

Trello kanban: https://trello.com/b/gftk6ZdV/kanban

Matrix Room: https://matrix.to/#/#fcitx5-android:mozilla.org

Discuss on Telegram: [@fcitx5_android_group](https://t.me/fcitx5_android_group) ([@fcitx5_android](https://t.me/fcitx5_android) originally)

## Build

### Recommended: Nix (WSL2 / Linux)

项目自带 `flake.nix`，使用 Nix 管理所有构建依赖，无需手动安装 Android SDK/NDK/CMake。

```shell
# 安装 Nix（如果尚未安装）
sh <(curl -L https://nixos.org/nix/install) --daemon
mkdir -p ~/.config/nix
echo "experimental-features = nix-command flakes" >> ~/.config/nix/nix.conf

# Clone 并拉取 submodule
git clone https://github.com/lemonhall/ai-coding-ime.git
cd ai-coding-ime
git submodule update --init --recursive

# 进入 dev shell（纯 CLI，不含 Android Studio）
nix develop .#noAS

# 验证环境（nix shell 不会改变 prompt）
echo $ANDROID_SDK_ROOT   # 应输出 /nix/store/.../libexec/android-sdk
echo $JAVA_HOME          # 应输出 /nix/store/.../openjdk-17.0.15+6

# 构建（首次含 C++ 编译，约 55 分钟）
./gradlew assembleDebug
```

### Fast Development Mode (WSL2 / Linux)

日常只改 Kotlin/UI 时，使用仓库内脚本避免反复触发 native 链路：

```shell
# 默认：单 ABI + 增量友好参数（installDebug）
./scripts/gradle-dev.sh

# 快速模式：跳过 CMake/native 安装链（仅适用于未改 C++/submodule）
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --kotlin
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --assemble
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --fast --install

# 更激进（可选）：再跳过 KSP/codegen（仅限未改注解/Room schema 的 Kotlin/UI 迭代）
GRADLE_USER_HOME=$HOME/.gradle ./scripts/gradle-dev.sh --ultrafast --kotlin
```

默认本机提速配置（`~/.gradle/gradle.properties`）建议为：

```properties
buildABI=arm64-v8a
buildTimestamp=0
org.gradle.daemon=true
org.gradle.caching=true
org.gradle.parallel=true
```

注意：
- `--fast` 依赖至少一次完整构建产物（native 缓存已存在）。
- 不要在日常迭代里执行 `clean`，否则会退回接近全量构建。
- 当前项目的自定义任务与 `org.gradle.configuration-cache=true` 不兼容，请保持关闭。
- `GRADLE_USER_HOME` 变更到一个全新目录时，Gradle Wrapper 会重新下载分发包（一次性）。若想复用缓存，固定为 `~/.gradle`。

Nix dev shell 提供的环境：

| 组件 | 版本 |
|---|---|
| Android SDK Platform | 35 |
| Build-Tools | 35.0.1 |
| Platform-Tools | 35.0.2 |
| NDK | 28.0.13004108 |
| CMake | 3.31.6 |
| JDK | OpenJDK 17.0.15+6 |

构建产物按 CPU 架构分包，位于 `app/build/outputs/apk/debug/`：
- `org.fcitx.fcitx5.android-{commit}-arm64-v8a-debug.apk`（大多数现代手机）
- `org.fcitx.fcitx5.android-{commit}-armeabi-v7a-debug.apk`
- `org.fcitx.fcitx5.android-{commit}-x86_64-debug.apk`
- `org.fcitx.fcitx5.android-{commit}-x86-debug.apk`

### WSL2 安装到手机

WSL2 中 adb 通常无法直接识别 USB 设备，推荐在 Windows PowerShell 中先把 APK 复制到本地再安装：

```powershell
# 确认设备（如果 daemon 启动失败，先 taskkill /F /IM adb.exe）
adb devices

# 复制 WSL 中最新的 arm64-v8a debug APK 到 Windows Downloads
wsl.exe -e bash -lc 'cp "$(ls -t /home/lemonhall/ai-coding-ime/app/build/outputs/apk/debug/*arm64-v8a-debug.apk | head -n1)" /mnt/c/Users/lemon/Downloads/ime-debug.apk'
adb install -r "$env:USERPROFILE\Downloads\ime-debug.apk"
```

在 WSL 里可先检查产物时间戳，确认是刚构建出来的 APK：

```bash
ls -la /home/lemonhall/ai-coding-ime/app/build/outputs/apk/debug/
```

也可以直接用仓库脚本一键安装（在 Windows PowerShell 中执行）：

```powershell
.\scripts\install-latest-apk.ps1 -WslRepoPath /home/lemonhall/ai-coding-ime

# 如需从 Windows 一键触发 WSL 编译 + 安装
.\scripts\install-latest-apk.ps1 -WslRepoPath /home/lemonhall/ai-coding-ime -Build
```

### Alternative: Manual SDK Setup

- Android SDK Platform & Build-Tools 35.
- Android NDK (Side by side) 28 & CMake 3.31.6, they can be installed using SDK Manager in Android Studio or `sdkmanager` command line.
- [KDE/extra-cmake-modules](https://github.com/KDE/extra-cmake-modules)
- GNU Gettext >= 0.20 (for `msgfmt` binary; or install `appstream` if you really have to use gettext <= 0.19.)

### How to set up development environment

<details>
<summary>Prerequisites for Windows</summary>

- Enable [Developer Mode](https://learn.microsoft.com/en-us/windows/apps/get-started/enable-your-device-for-development) so that symlinks can be created without administrator privilege.

- Enable symlink support for `git`:

    ```shell
    git config --global core.symlinks true
    ```

</details>

First, clone this repository and fetch all submodules:

```shell
git clone https://github.com/lemonhall/ai-coding-ime.git
git submodule update --init --recursive
```

Install `extra-cmake-modules` and `gettext` with your system package manager:

```shell
# For Arch Linux (Arch has gettext in it's base meta package)
sudo pacman -S extra-cmake-modules

# For Debian/Ubuntu
sudo apt install extra-cmake-modules gettext

# For macOS
brew install extra-cmake-modules gettext

# For Windows, install MSYS2 and execute in its shell (UCRT64)
pacman -S mingw-w64-ucrt-x86_64-extra-cmake-modules mingw-w64-ucrt-x86_64-gettext
# then add C:\msys64\ucrt64\bin to PATH
```

Install Android SDK Platform, Android SDK Build-Tools, Android NDK and cmake via SDK Manager in Android Studio:

<details>
<summary>Detailed steps (screenshots)</summary>

**Note:** These screenshots are for references and the versions in them may be out of date.
The current recommended versions are recorded in [Versions.kt](build-logic/convention/src/main/kotlin/Versions.kt) file.

![Open SDK Manager](https://user-images.githubusercontent.com/13914967/202184493-3ee1546b-0a83-4cc9-9e41-d20b0904a0cf.png)

![Install SDK Platform](https://user-images.githubusercontent.com/13914967/202184534-340a9e7c-7c42-49bd-9cf5-1ec9dcafcf32.png)

![Install SDK Build-Tools](https://user-images.githubusercontent.com/13914967/202185945-0c7a9f39-1fcc-4018-9c81-b3d2bf1c2d3f.png)

![Install NDK](https://user-images.githubusercontent.com/13914967/202185601-0cf877ea-e148-4b88-bd2f-70533189b3d4.png)

![Install CMake](https://user-images.githubusercontent.com/13914967/202184655-3c1ab47c-432f-4bd7-a508-92096482de50.png)

</details>

### Trouble-shooting

- Android Studio indexing takes forever to complete and cosumes a lot of memory.

    Switch to "Project" view in the "Project" tool window (namely the file tree side bar), right click `lib/fcitx5/src/main/cpp/prebuilt` directory, then select "Mark Directory as > Excluded". You may also need to restart the IDE to interrupt ongoing indexing process.

- Gradle error: "No variants found for ':app'. Check build files to ensure at least one variant exists." or "[CXX1210] <whatever>/CMakeLists.txt debug|arm64-v8a : No compatible library found"

    Examine if there are environment variables set such as `_JAVA_OPTIONS` or `JAVA_TOOL_OPTIONS`. You might want to clear them (maybe in the startup script `studio.sh` of Android Studio), as some gradle plugin treats anything in stderr as errors and aborts.

## Nix

Appropriate Android SDK with NDK is available in the development shell.  The `gradlew` should work out-of-the-box, so you can install the app to your phone with `./gradlew installDebug` after applying the patch mentioned above. For development, you may want to install the unstable version of Android Studio, and point the project SDK path to `$ANDROID_SDK_ROOT` defined in the shell. Notice that Android Studio may generate wrong `local.properties` which sets the SDK location to `~/Android/SDK` (installed by SDK Manager). In such case, you need specify `sdk.dir` as the project SDK in that file manually, in case Android Studio sticks to the wrong global SDK.
