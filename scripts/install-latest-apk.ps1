<#
.SYNOPSIS
Copy latest debug APK from WSL and install it with Windows adb.

.DESCRIPTION
This script targets the common WSL2 workflow:
1) Build APK in WSL.
2) Copy latest ABI-specific APK to Windows Downloads.
3) Install with `adb install -r`.

Use `-Build` to trigger step 1 automatically.
#>
[CmdletBinding()]
param(
    [string]$WslRepoPath = "~/ai-coding-ime",
    [string]$Abi = "arm64-v8a",
    [string]$OutputName = "ime-debug.apk",
    [string]$Distro = "",
    [switch]$Build
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
    throw "adb not found in PATH. Install Android platform-tools or add adb.exe to PATH."
}

$winApkPath = Join-Path $env:USERPROFILE "Downloads\$OutputName"
$wslApkPath = "/mnt/c/Users/$($env:USERNAME)/Downloads/$OutputName"

$buildScript = @'
set -euo pipefail

repo_path="$1"
cd "$repo_path"

GRADLE_USER_HOME="$HOME/.gradle" ./scripts/gradle-dev.sh --fast --assemble
'@

$bashScript = @'
set -euo pipefail

repo_path="$1"
abi="$2"
target_apk="$3"

latest_apk="$(ls -t "$repo_path"/app/build/outputs/apk/debug/*-"$abi"-debug.apk 2>/dev/null | head -n1 || true)"
if [ -z "$latest_apk" ]; then
  echo "No APK found for abi '$abi' under $repo_path/app/build/outputs/apk/debug/" >&2
  exit 2
fi

cp "$latest_apk" "$target_apk"
printf '%s\n' "$latest_apk"
'@

$wslArgs = @()
if ($Distro -ne "") {
    $wslArgs += @("-d", $Distro)
}

if ($Build) {
    Write-Host "[apk-install] Building APK in WSL: $WslRepoPath"
    $buildArgs = @()
    $buildArgs += $wslArgs
    $buildArgs += @("-e", "bash", "-lc", $buildScript, "_", $WslRepoPath)
    wsl.exe @buildArgs
}

$wslArgs += @(
    "-e", "bash", "-lc", $bashScript,
    "_", $WslRepoPath, $Abi, $wslApkPath
)

$latestApk = (wsl.exe @wslArgs).Trim()

Write-Host "[apk-install] Source APK: $latestApk"
Write-Host "[apk-install] Local APK : $winApkPath"

adb install -r $winApkPath
