# Foodara Docker Manager
# Usage: .\start.ps1 [up|down|status|restart|logs]

param(
    [ValidateSet("up", "down", "status", "restart", "logs")]
    [string]$Command = "status"
)

$DockerDir = $PSScriptRoot
$ComposeFile = Join-Path $DockerDir "docker-compose.yml"
$Services = @("db", "rabbitmq", "notification-db", "main-backend", "payment-service", "notification-service", "driver-service", "api-gateway", "cloudflared")
$HealthPorts = @{
    "api-gateway" = 8080
    "main-backend" = 8081
    "notification-service" = 8084
}

function Write-Banner {
    Write-Host ""
    Write-Host "  ╔══════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "  ║        🍜 Foodara Docker Manager         ║" -ForegroundColor Cyan
    Write-Host "  ╚══════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Step($msg) {
    Write-Host "  ⏳ $msg" -ForegroundColor Yellow
}

function Write-Ok($msg) {
    Write-Host "  ✅ $msg" -ForegroundColor Green
}

function Write-Err($msg) {
    Write-Host "  ❌ $msg" -ForegroundColor Red
}

function Invoke-Up {
    Write-Banner
    Write-Step "Building & starting all services..."
    Write-Host ""

    Push-Location $DockerDir
    try {
        docker compose up --build -d 2>&1 | ForEach-Object { Write-Host "     $_" }
        if ($LASTEXITCODE -ne 0) {
            Write-Err "docker compose up failed!"
            return
        }
    } finally {
        Pop-Location
    }

    Write-Host ""
    Write-Step "Waiting for services to become healthy..."

    # Wait for each health-checkable service
    $maxWait = 20
    $elapsed = 0
    $allHealthy = $false
    while ($elapsed -lt $maxWait -and -not $allHealthy) {
        Start-Sleep -Seconds 3
        $elapsed += 3
        $allHealthy = $true
        foreach ($svc in $HealthPorts.Keys) {
            $port = $HealthPorts[$svc]
            try {
                $r = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -TimeoutSec 2 -ErrorAction SilentlyContinue
                if ($r.status -ne "UP") { $allHealthy = $false }
            } catch {
                $allHealthy = $false
            }
        }
        Write-Host "     ." -NoNewline
    }
    Write-Host ""

    # Get tunnel URL
    $tunnelUrl = Get-TunnelUrl

    Invoke-Status

    if ($tunnelUrl) {
        Write-Host ""
        Write-Host "  🌐 Tunnel: $tunnelUrl" -ForegroundColor Green
        Write-Host "  🌐 API:    $tunnelUrl/api" -ForegroundColor Green
    }

    if ($allHealthy) {
        Write-Host ""
        Write-Host "  🎉 All services healthy!" -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "  ⚠️  Some services still starting... (waited ${elapsed}s)" -ForegroundColor Yellow
    }
    Write-Host ""
}

function Invoke-Down {
    Write-Banner
    Write-Step "Stopping all services..."
    Write-Host ""

    $confirm = Read-Host "  Confirm? (y/n)"
    if ($confirm -ne "y") {
        Write-Host "  Cancelled." -ForegroundColor Gray
        return
    }

    Push-Location $DockerDir
    try {
        docker compose down 2>&1 | ForEach-Object { Write-Host "     $_" }
    } finally {
        Pop-Location
    }
    Write-Ok "All services stopped."
    Write-Host ""
}

function Invoke-Status {
    Write-Banner
    Write-Host "  SERVICE                  STATUS             PORT" -ForegroundColor White
    Write-Host "  ────────────────────────────────────────────────" -ForegroundColor DarkGray

    Push-Location $DockerDir
    try {
        $raw = docker compose ps --format json 2>&1
        if ($LASTEXITCODE -ne 0) {
            foreach ($s in $Services) {
                $label = "  {0,-24}" -f $s
                Write-Host "$label 🔴 not created" -ForegroundColor Red
            }
            return
        }
    } finally {
        Pop-Location
    }

    $lines = $raw | ForEach-Object { $_ | ConvertFrom-Json }

    foreach ($svc in $Services) {
        $match = $lines | Where-Object { $_.Service -eq $svc -or $_.Name -like "*$svc*" }
        if (-not $match) {
            $label = "  {0,-24}" -f $svc
            Write-Host "$label 🔴 not running" -ForegroundColor Red
            continue
        }

        $state = $match.State
        $ports = if ($match.Publishers) {
            ($match.Publishers | ForEach-Object { "$($_.PublishedPort)" }) -join ","
        } else { "-" }

        $icon = "🔴"
        $color = "Red"
        if ($state -like "*running*") {
            if ($state -like "*healthy*") {
                $icon = "🟢"
                $color = "Green"
            } else {
                $icon = "🟡"
                $color = "Yellow"
            }
        }

        $namePad = $svc.PadRight(24)
        $statePad = $state.PadRight(18)
        Write-Host "  ${icon} " -NoNewline
        Write-Host "$namePad" -NoNewline
        Write-Host "$statePad" -ForegroundColor $color -NoNewline
        Write-Host ":${ports}"
    }
    Write-Host ""
}

function Invoke-Restart {
    Invoke-Down
    Start-Sleep -Seconds 3
    Invoke-Up
}

function Invoke-Logs {
    param([string]$Service = "")
    Push-Location $DockerDir
    try {
        if ($Service) {
            docker compose logs -f --tail=50 $Service
        } else {
            docker compose logs -f --tail=20
        }
    } finally {
        Pop-Location
    }
}

function Get-TunnelUrl {
    Push-Location $DockerDir
    try {
        $log = docker compose logs cloudflared 2>&1 | Out-String
        if ($log -match 'https://[a-zA-Z0-9.-]+\.trycloudflare\.com') {
            return $matches[0]
        }
    } finally {
        Pop-Location
    }
    return $null
}

# ── Main ──────────────────────────────────────────
switch ($Command) {
    "up"      { Invoke-Up }
    "down"    { Invoke-Down }
    "status"  { Invoke-Status }
    "restart" { Invoke-Restart }
    "logs"    { Invoke-Logs }
}
