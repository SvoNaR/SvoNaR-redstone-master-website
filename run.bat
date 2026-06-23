@echo off
setlocal
cd /d "%~dp0"

set PORT_PID=
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do set PORT_PID=%%a

if defined PORT_PID (
    echo.
    echo Port 8080 is already in use ^(PID %PORT_PID%^).
    echo This usually means the server is already running from a previous start.
    echo.
    choice /C YN /M "Stop it and start a new instance"
    if errorlevel 2 (
        echo.
        echo Open http://localhost:8080/ in your browser, or run stop.bat to shut down.
        echo.
        pause
        exit /b 0
    )
    echo Stopping PID %PORT_PID%...
    taskkill /PID %PORT_PID% /F >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo.
echo Starting Redstone Master Web on http://localhost:8080/
echo Press Ctrl+C to stop, or run stop.bat from another window.
echo.

if not exist "application-local.properties" (
    if exist "application-local.properties.example" (
        echo Creating application-local.properties from example...
        copy /Y "application-local.properties.example" "application-local.properties" >nul
        echo Edit application-local.properties if you need SMTP or a custom admin password.
        echo.
    ) else (
        echo ERROR: application-local.properties is missing and no example file was found.
        echo Create application-local.properties with app.admin.password=...
        pause
        exit /b 1
    )
)

set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn generate-resources spring-boot:run
pause
