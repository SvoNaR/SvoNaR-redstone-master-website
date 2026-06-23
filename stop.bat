@echo off
setlocal
cd /d "%~dp0"

set PORT_PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do set PORT_PID=%%a

if not defined PORT_PID (
    echo Port 8080 is free. Nothing to stop.
    pause
    exit /b 0
)

echo Stopping process on port 8080 ^(PID %PORT_PID%^)...
taskkill /PID %PORT_PID% /F
timeout /t 2 /nobreak >nul

netstat -ano | findstr ":8080" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo Failed to free port 8080. Try closing other apps manually.
    pause
    exit /b 1
)

echo Port 8080 is free.
pause
