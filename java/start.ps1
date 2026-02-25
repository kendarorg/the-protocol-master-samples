#!/usr/bin/env pwsh

# Get script directory (equivalent of BASH_SOURCE)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Build protocol-runner source path
$ProtocolRunnerSrc = Join-Path $ScriptDir "../../the-protocol-master/protocol-runner/target/protocol-runner.jar"
$ProtocolRunnerSrc = Resolve-Path -Path $ProtocolRunnerSrc -ErrorAction SilentlyContinue

Write-Host $ProtocolRunnerSrc

# Check if file exists and is not empty
if ($ProtocolRunnerSrc -and (Test-Path $ProtocolRunnerSrc)) {
    $fileInfo = Get-Item $ProtocolRunnerSrc
    if ($fileInfo.Length -gt 0) {
        Write-Host "Copying protocol-runner"
        $destination = Join-Path $ScriptDir "Tpm/protocol-runner.jar"
        Copy-Item -Path $ProtocolRunnerSrc -Destination $destination -Force
    }
}

# Change directory to script directory
Set-Location $ScriptDir

# Run docker compose (modern syntax)
docker-compose up