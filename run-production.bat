@echo off
setlocal
cd /d "%~dp0"

if not exist "application-local.properties" (
    if exist "application-production-local.properties.example" (
        echo Creating application-local.properties from production example...
        copy /Y "application-production-local.properties.example" "application-local.properties" >nul
        echo Edit application-local.properties: set app.admin.password and spring.mail.password.
        echo.
    )
)

echo.
echo Starting Redstone Master Web (production profile) on https://redstone-master.ru
echo Behind nginx: proxy_pass to port 8080 on this machine.
echo.

set MAVEN_OPTS=-Dfile.encoding=UTF-8
mvn -q -Dspring-boot.run.profiles=production spring-boot:run
pause
