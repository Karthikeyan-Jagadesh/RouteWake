@echo off
echo ========================================================
echo   Starting RouteWake Spring Boot Backend Server
echo ========================================================

REM Find Maven
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MVN_CMD=mvn
) else (
    if exist "C:\Program Files\Apache\apache-maven-3.9.14\bin\mvn.cmd" (
        set MVN_CMD="C:\Program Files\Apache\apache-maven-3.9.14\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found! Please ensure Maven is installed or added to PATH.
        pause
        exit /b 1
    )
)

echo [INFO] Using Maven: %MVN_CMD%
echo [INFO] Starting Spring Boot application on port 8081...
echo [INFO] Press Ctrl+C to stop.
echo.

%MVN_CMD% spring-boot:run
pause
