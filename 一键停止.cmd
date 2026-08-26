@echo off
title Smart Campus - Stop All
cd /d "%~dp0"

echo Stopping frontend + gateway + services + Nacos (ports 5173/8080-8085/8848/9848)...
powershell -NoProfile -Command "$ports=5173,8080,8081,8082,8083,8084,8085,8848,9848; $all=@(); foreach($p in $ports){ Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue | ForEach-Object { $all += $_.OwningProcess } }; $all = $all | Select-Object -Unique; foreach($procId in $all){ try { Stop-Process -Id $procId -Force -ErrorAction Stop } catch {} }; Write-Host ('Stopped ' + $all.Count + ' process(es)')"
if exist "O:\nacos-server-2.3.2\nacos\bin\shutdown.cmd" call "O:\nacos-server-2.3.2\nacos\bin\shutdown.cmd"

echo.
echo MySQL57 is left running (auto-start service).
echo DONE - all application processes stopped.
pause
