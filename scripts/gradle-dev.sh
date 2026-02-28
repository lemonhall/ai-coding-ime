#!/usr/bin/env bash
set -euo pipefail

# Development wrapper for Gradle in this repo.
#
# Default mode:
#   - single ABI (arm64-v8a)
#   - stable build timestamp
#
# Fast mode (--fast):
#   - additionally skips native/CMake installation chain
#   - intended for Kotlin/UI iteration when C++ and submodules are unchanged
#   - requires at least one prior full build that produced native artifacts

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MODE_FAST=0
MODE_ULTRAFAST=0
TASK=":app:installDebug"
EXTRA_ARGS=()

usage() {
  cat <<'EOF'
Usage:
  scripts/gradle-dev.sh [--fast|--ultrafast] [--kotlin|--assemble|--install|--task <task>] [-- <extra-gradle-args>]

Examples:
  scripts/gradle-dev.sh
  scripts/gradle-dev.sh --kotlin
  scripts/gradle-dev.sh --assemble
  scripts/gradle-dev.sh --fast --assemble
  scripts/gradle-dev.sh --ultrafast --kotlin
  scripts/gradle-dev.sh --fast --task :app:installDebug -- --info
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --fast)
      MODE_FAST=1
      shift
      ;;
    --ultrafast)
      MODE_FAST=1
      MODE_ULTRAFAST=1
      shift
      ;;
    --kotlin)
      TASK=":app:compileDebugKotlin"
      shift
      ;;
    --assemble)
      TASK=":app:assembleDebug"
      shift
      ;;
    --install)
      TASK=":app:installDebug"
      shift
      ;;
    --task)
      if [[ $# -lt 2 ]]; then
        echo "Missing value for --task" >&2
        usage
        exit 2
      fi
      TASK="$2"
      shift 2
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    --)
      shift
      EXTRA_ARGS+=("$@")
      break
      ;;
    *)
      EXTRA_ARGS+=("$1")
      shift
      ;;
  esac
done

# Keep gradle cache in repo workspace by default.
# This avoids permission prompts and prevents cold-cache rebuilds from /tmp.
if [[ -z "${GRADLE_USER_HOME:-}" ]]; then
  export GRADLE_USER_HOME="$ROOT_DIR/.gradle-user-home"
fi
mkdir -p "$GRADLE_USER_HOME"

BASE_ARGS=(
  "$TASK"
  "-PbuildABI=arm64-v8a"
  "-PbuildTimestamp=0"
  "--build-cache"
  "--parallel"
)

FAST_EXCLUDES=(
  "-x" ":app:configureCMakeDebug[arm64-v8a]"
  "-x" ":app:buildCMakeDebug[arm64-v8a][androidfrontend,androidkeyboard,etc]"
  "-x" ":app:externalNativeBuildDebug"
  "-x" ":app:installLibraryConfig[fcitx5-chinese-addons]"
  "-x" ":app:installLibraryConfig[fcitx5-lua]"
  "-x" ":app:installLibraryConfig[fcitx5]"
  "-x" ":app:installLibraryConfig[libime]"
  "-x" ":app:installLibraryTranslation[fcitx5-chinese-addons]"
  "-x" ":app:installLibraryTranslation[fcitx5-lua]"
  "-x" ":app:installLibraryTranslation[fcitx5]"
  "-x" ":app:installLibraryTranslation[libime]"
  "-x" ":app:installProjectConfig"
  "-x" ":app:installProjectPrebuiltAssets"
  "-x" ":app:installProjectTranslation"
  "-x" ":app:installFcitxComponent"
  "-x" ":app:deleteFcitxComponentExcludeFiles"
  "-x" ":app:generateDataDescriptor"
  "-x" ":lib:fcitx5:configureCMakeDebug[arm64-v8a]"
  "-x" ":lib:fcitx5:buildCMakeDebug[arm64-v8a][Fcitx5Config,Fcitx5Core,etc]"
  "-x" ":lib:fcitx5:externalNativeBuildDebug"
  "-x" ":lib:fcitx5:installFcitxDevelComponent"
  "-x" ":lib:fcitx5:installFcitxHeaders"
  "-x" ":lib:fcitx5-lua:configureCMakeDebug[arm64-v8a]"
  "-x" ":lib:fcitx5-lua:buildCMakeDebug[arm64-v8a][cmake,luaaddonloader]"
  "-x" ":lib:fcitx5-lua:externalNativeBuildDebug"
  "-x" ":lib:fcitx5-lua:installFcitxHeaders"
  "-x" ":lib:libime:configureCMakeDebug[arm64-v8a]"
  "-x" ":lib:libime:buildCMakeDebug[arm64-v8a][IMECore,IMEPinyin,etc]"
  "-x" ":lib:libime:externalNativeBuildDebug"
  "-x" ":lib:libime:installFcitxHeaders"
  "-x" ":lib:fcitx5-chinese-addons:configureCMakeDebug[arm64-v8a]"
  "-x" ":lib:fcitx5-chinese-addons:buildCMakeDebug[arm64-v8a][chttrans,cmake,etc]"
  "-x" ":lib:fcitx5-chinese-addons:externalNativeBuildDebug"
  "-x" ":lib:fcitx5-chinese-addons:installFcitxHeaders"
)

ULTRAFAST_EXCLUDES=(
  "-x" ":app:kspDebugKotlin"
  "-x" ":codegen:compileKotlin"
  "-x" ":codegen:compileJava"
  "-x" ":codegen:processResources"
  "-x" ":codegen:classes"
  "-x" ":codegen:jar"
)

RUN_ARGS=("${BASE_ARGS[@]}")

echo "[gradle-dev] GRADLE_USER_HOME=$GRADLE_USER_HOME"
if (( MODE_FAST )); then
  echo "[gradle-dev] fast mode enabled: skip native/CMake chain"
  RUN_ARGS+=("${FAST_EXCLUDES[@]}")
fi
if (( MODE_ULTRAFAST )); then
  echo "[gradle-dev] ultrafast mode enabled: skip KSP/codegen (safe only when no annotation/schema changes)"
  RUN_ARGS+=("${ULTRAFAST_EXCLUDES[@]}")
fi

exec ./gradlew "${RUN_ARGS[@]}" "${EXTRA_ARGS[@]}"
