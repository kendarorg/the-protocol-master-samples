#!/usr/bin/env pwsh

# Get script directory (PowerShell equivalent of BASH_SOURCE)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Equivalent of chmod +x (mainly needed on Windows to unblock files)
$startScripts = @(
    "python/Tpm/start.ps1",
    "java/Tpm/start.ps1",
    "golang/Tpm/start.ps1",
    "net-core/Tpm/start.ps1",
    "mitm/Tpm/start.ps1"
)

foreach ($relativePath in $startScripts) {
    $fullPath = Join-Path $ScriptDir $relativePath
    if (Test-Path $fullPath) {
        # On Windows this removes "downloaded from internet" restriction
        Unblock-File -Path $fullPath -ErrorAction SilentlyContinue
    }
}

# Optional override:
# $env:FORCE_ARCH="amd64"
if ($env:FORCE_ARCH) {
    $arch = $env:FORCE_ARCH
}
else {
    $arch = [System.Runtime.InteropServices.RuntimeInformation]::ProcessArchitecture.ToString()
}

switch ($arch.ToLower()) {
    "x64" { $platform = "linux/amd64" }
    "amd64" { $platform = "linux/amd64" }
    "arm64" { $platform = "linux/arm64" }
    default {
        Write-Warning "Unknown architecture '$arch'. Defaulting to linux/amd64"
        $platform = "linux/amd64"
    }
}

Write-Host "Detected architecture: $arch"
Write-Host "Using Docker platform: $platform"

function Update-EnvPlatform {
    param (
        [Parameter(Mandatory = $true)]
        [string]$TargetDir
    )

    if (-not (Test-Path $TargetDir -PathType Container)) {
        Write-Error "Directory not found: $TargetDir"
        return
    }

    $envFile = Join-Path $TargetDir ".env"

    $existingLines = @()
    if (Test-Path $envFile) {
        $existingLines = Get-Content $envFile |
            Where-Object {
                $_ -notmatch '^DOCKER_PLATFORM=' -and
                $_ -notmatch '^TARGET_ARCH='
            }
    }

    $updatedLines = $existingLines + @(
        "DOCKER_PLATFORM=$platform"
        "TARGET_ARCH=$platform"
    )

    Set-Content -Path $envFile -Value $updatedLines -Encoding UTF8

    Write-Host "Updated $envFile"

    $ProtocolRunnerSrc = Join-Path $ScriptDir "../the-protocol-master/protocol-runner/target/protocol-runner.jar"
    $ProtocolRunnerSrc = Resolve-Path -Path $ProtocolRunnerSrc -ErrorAction SilentlyContinue

    $ProtocolRunnerDns = Join-Path $ScriptDir "../the-protocol-master/protocol-dns-plugin/target/protocol-dns-plugin.jar"
    $ProtocolRunnerDns = Resolve-Path -Path $ProtocolRunnerDns -ErrorAction SilentlyContinue

    Write-Host $ProtocolRunnerSrc
    Write-Host $ProtocolRunnerDns

    # Check if file exists and is not empty
    if ($ProtocolRunnerSrc -and (Test-Path $ProtocolRunnerSrc)) {
        $fileInfo = Get-Item $ProtocolRunnerSrc
        if ($fileInfo.Length -gt 0) {
            Write-Host "Copying protocol-runner"
            $destination = Join-Path $ScriptDir "Tpm/protocol-runner.jar"
            Copy-Item -Path $ProtocolRunnerSrc -Destination $destination -Force
            $destination = Join-Path $ScriptDir "Tpm/protocol-dns-plugin.jar"
            Copy-Item -Path $ProtocolRunnerDns -Destination $destination -Force
        }
    }
}

Update-EnvPlatform (Join-Path $ScriptDir "python")
Update-EnvPlatform (Join-Path $ScriptDir "net-core")
Update-EnvPlatform (Join-Path $ScriptDir "golang")
Update-EnvPlatform (Join-Path $ScriptDir "mitm")
Update-EnvPlatform (Join-Path $ScriptDir "java")