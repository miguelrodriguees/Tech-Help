@echo off
setlocal
set "TECHHELP_TUNNEL_EXEC=%~dp0.local-tools\cloudflared.exe"
powershell.exe -NoProfile -Command "try { Get-Process -Name cloudflared -ErrorAction SilentlyContinue | Where-Object { $_.Path -eq $env:TECHHELP_TUNNEL_EXEC } | Stop-Process -ErrorAction Stop } catch { Write-Error $_; exit 1 }"
if errorlevel 1 exit /b 1
echo Link temporario deste projeto encerrado. O backend local continua separado.
