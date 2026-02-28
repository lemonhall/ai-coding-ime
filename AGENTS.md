# Agent Notes (ai-coding-ime)

## 1) Architecture Overview
### Areas
- Android App (主产品)
  - entry: `app/src/main/java/org/fcitx/fcitx5/android/input/FcitxInputMethodService.kt`
  - UI/event: `app/src/main/java/org/fcitx/fcitx5/android/input/InputView.kt`, `app/src/main/java/org/fcitx/fcitx5/android/input/CandidatesView.kt`
  - settings: `app/src/main/java/org/fcitx/fcitx5/android/ui/main/`
- Project Dictionary (Kotlin 侧增量功能)
  - core: `app/src/main/java/org/fcitx/fcitx5/android/projectdict/`
  - settings entry: `ProjectDictFragment.kt` + `SettingsRoute.ProjectDict`
- Native + SDK 子系统
  - app JNI/CMake: `app/src/main/cpp/`
  - native libs: `lib/fcitx5`, `lib/libime`, `lib/fcitx5-lua`, `lib/fcitx5-chinese-addons`
  - plugins: `plugin/*`
- Build system
  - Android/Gradle conventions: `build-logic/convention/src/main/kotlin/`
  - Nix dev shell: `flake.nix`

### Data Flow
```text
Key/Input
  -> FcitxInputMethodService / InputView
  -> Fcitx.kt (Kotlin API)
  -> JNI bridge (app/src/main/cpp/native-lib.cpp)
  -> fcitx/libime native engine
  -> FcitxEvent back to Kotlin
  -> ProjectDictBooster (candidate merge)
  -> InputView/CandidatesView render
```

### Config Entry Points
- Nix toolchain: `flake.nix` (`default` / `noAS` shell)
- Project Gradle defaults: `gradle.properties`
- Local SDK path: `local.properties` (`sdk.dir=...`)
- Local dev speed profile (user-level): `~/.gradle/gradle.properties`
- Project dictionary protocol samples: `.ime/dict.tsv`, `.ime/meta.json`

### Persistence
- Bundled runtime assets (repo): `app/src/main/assets/**`
- Generated asset descriptor (ignored): `app/src/main/assets/descriptor.json`
- Android app prefs: `PreferenceManager.getDefaultSharedPreferences(...)`
- ProjectDict runtime state: in-memory only (`ProjectDictManager`), not durable across process restart

## 2) Code Conventions (Negative Knowledge)
- Do not run `clean` in daily iteration.
  - Why: `clean` removes `.cxx`/Gradle intermediates and can force near-full native rebuild (tens of minutes).
  - Do instead: use `./scripts/gradle-dev.sh` or `./scripts/gradle-dev.sh --fast`.
  - Verify: `./gradlew :app:assembleDebug -m --console=plain` should not show full multi-ABI native chain unexpectedly.

- Do not enable `org.gradle.configuration-cache=true` for this repo.
  - Why: custom `CMakeBuildInstallTask` is currently incompatible with configuration cache.
  - Do instead: keep only `daemon/caching/parallel` enabled in `~/.gradle/gradle.properties`.
  - Verify: build should not fail with “Configuration cache problems found”.

- Do not use `--fast` mode when touching C++/submodule/native assets.
  - Why: fast mode excludes native install/build tasks and may produce stale or incomplete native output.
  - Do instead: run non-fast build (`./scripts/gradle-dev.sh` or full `./gradlew ...`).
  - Verify: modified native behavior appears in APK after rebuild.

- Do not edit `lib/*` and `plugin/*` native/submodule code unless explicitly required.
  - Why: this explodes compile surface and invalidates most caches.
  - Do instead: keep feature work in Kotlin layer when requirement allows.
  - Verify: `git status` should show only intended app-layer files for Kotlin-only tasks.

- Do not commit generated build artifacts or ignored generated assets.
  - Why: noisy diffs and reproducibility issues.
  - Do instead: commit source/config/docs only; leave `build/`, `.cxx/`, generated descriptors untracked.
  - Verify: `git status --short` before commit contains only intentional source/doc changes.

- Do not build from Windows-mounted paths (`/mnt/*`) in WSL2.
  - Why: heavy IO penalty makes C++/Gradle much slower.
  - Do instead: clone/build under Linux filesystem (e.g. `~/ai-coding-ime`).
  - Verify: build outputs/intermediates are under `/home/...`, not `/mnt/...`.

- Do not update Android SDK/NDK/CMake versions in one place only.
  - Why: mismatch between `flake.nix` and `build-logic/.../Versions.kt` causes toolchain drift.
  - Do instead: update both in one change.
  - Verify: Nix shell env and Gradle versions align during build.

## 3) Testing Strategy
### Environment
- Preferred: WSL2/Linux with Nix
  - `cd ~/ai-coding-ime`
  - `nix develop .#noAS`

### Full Build (slow, release confidence)
- `./gradlew :app:assembleDebug -PbuildABI=arm64-v8a,armeabi-v7a,x86,x86_64`

### Daily Dev Build (recommended)
- Normal dev: `./scripts/gradle-dev.sh`
- Fast Kotlin/UI iteration: `./scripts/gradle-dev.sh --fast --assemble`
- Install to device: `./scripts/gradle-dev.sh --fast --install`

### Unit Tests
- App unit tests: `./gradlew :app:testDebugUnitTest`
- All JVM unit tests: `./gradlew test`
- Single test example:
  - `./gradlew :app:testDebugUnitTest --tests "org.fcitx.fcitx5.android.StringEscapeTest"`

### Instrumentation Tests (device/emulator required)
- `./gradlew :app:connectedDebugAndroidTest`

### Build Debugging / Hang Triage
- Dry-run task graph: `./gradlew :app:assembleDebug -m --console=plain`
- Daemon status: `./gradlew --status`
- Reset daemon: `./gradlew --stop`
- Verbose logs: `./gradlew :app:assembleDebug --info`

### Windows-side ADB install (WSL output APK)
- `adb install "\\wsl$\Ubuntu-24.04\home\<user>\ai-coding-ime\app\build\outputs\apk\debug\org.fcitx.fcitx5.android-<commit>-arm64-v8a-debug.apk"`

## 4) Repo Safety / Permissions
- Prefer Linux binaries inside WSL2; avoid unnecessary cross-boundary Windows tooling from WSL.
- Keep destructive commands explicit and minimal (`rm -rf`, resets) and require clear intent.
- Run the smallest verifying command first, then widen scope.

## 5) Runtime Config
- `buildABI`: local ABI override for dev speed (`arm64-v8a` recommended).
- `buildTimestamp`: set `0` locally to preserve incremental behavior.
- `ANDROID_SDK_ROOT`, `JAVA_HOME`: provided by `nix develop`.

## 6) Release Notes
- Before release or CI parity check, disable fast shortcuts and run full non-fast build.
- Re-verify ProjectDict behavior without relying on local stale native caches.
