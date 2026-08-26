@echo off
title Smart Campus - Start All
cd /d "%~dp0"

echo [1/6] Ensure MySQL is running...
net start MySQL57 >nul 2>&1
if errorlevel 1 echo   (MySQL57 already running, or needs admin rights)
echo   OK

echo [2/6] Starting Nacos standalone...
start "Nacos" /min cmd /c "cd /d O:\nacos-server-2.3.2\nacos\bin && startup.cmd -m standalone"
echo   waiting for port 8848 ...
powershell -NoProfile -Command "$ok=$false; for($i=0;$i -lt 40;$i++){ Start-Sleep -Seconds 2; if(Get-NetTCPConnection -LocalPort 8848 -State Listen -ErrorAction SilentlyContinue){$ok=$true;break} }; if(-not $ok){ Write-Host 'NACOS TIMEOUT'; exit 1 }"
if errorlevel 1 goto :fail
echo   Nacos UP

echo [3/6] Starting 5 business services...
start "user-service" /min cmd /k java -jar "%~dp0user-service\target\user-service-1.0.0.jar"
start "course-service" /min cmd /k java -jar "%~dp0course-service\target\course-service-1.0.0.jar"
start "selection-service" /min cmd /k java -jar "%~dp0selection-service\target\selection-service-1.0.0.jar"
start "recommend-service" /min cmd /k java -jar "%~dp0recommend-service\target\recommend-service-1.0.0.jar"
start "statistics-service" /min cmd /k java -jar "%~dp0statistics-service\target\statistics-service-1.0.0.jar"
echo   waiting for ports 8081-8085 ...
powershell -NoProfile -Command "$ports=8081,8082,8083,8084,8085; $up=@(); for($i=0;$i -lt 60;$i++){ Start-Sleep -Seconds 3; foreach($p in $ports){ if(($up -notcontains $p) -and (Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction SilentlyContinue)){ $up += $p } }; if($up.Count -eq 5){break} }; if($up.Count -lt 5){ Write-Host ('TIMEOUT waiting services, up=' + $up.Count); exit 1 }"
if errorlevel 1 goto :fail
echo   5 business services UP

echo [4/6] Starting gateway...
start "gateway-service" /min cmd /k java -jar "%~dp0gateway-service\target\gateway-service-1.0.0.jar"
echo   waiting for port 8080 ...
powershell -NoProfile -Command "$ok=$false; for($i=0;$i -lt 40;$i++){ Start-Sleep -Seconds 2; if(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue){$ok=$true;break} }; if(-not $ok){ Write-Host 'GATEWAY TIMEOUT'; exit 1 }"
if errorlevel 1 goto :fail
echo   Gateway UP

echo [5/6] Starting frontend (Vite)...
start "frontend" /min cmd /k "cd /d %~dp0frontend && npm run dev"
echo   waiting for port 5173 ...
powershell -NoProfile -Command "$ok=$false; for($i=0;$i -lt 30;$i++){ Start-Sleep -Seconds 2; if(Get-NetTCPConnection -LocalPort 5173 -State Listen -ErrorAction SilentlyContinue){$ok=$true;break} }; if(-not $ok){ Write-Host 'VITE TIMEOUT'; exit 1 }"
if errorlevel 1 goto :fail
echo   Frontend UP

echo [6/6] Opening browser...
start "" http://localhost:5173/login

echo.
echo ============================================
echo  ALL SERVICES STARTED
echo  Web:      http://localhost:5173
echo  Gateway:  http://localhost:8080
echo  Accounts: admin/Admin123  T1001/Pass1234  20210001/Pass1234
echo ============================================
pause
exit /b 0

:fail
echo.
echo STARTUP FAILED - check the minimized service windows for error logs.
pause
exit /b 1
