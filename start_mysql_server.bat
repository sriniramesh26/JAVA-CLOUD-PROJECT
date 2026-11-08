@echo off
:: =============================================================================
:: SMART CITY TRAFFIC ROUTER - 1-CLICK MYSQL 8.4 LAUNCHER & CONFIGURATOR
:: Student 2: Database Host (Windows)
:: =============================================================================
title Smart City Traffic DB - MySQL 8.4 Host Launcher
color 0A

echo ======================================================================
echo   SMART CITY TRAFFIC NETWORK - STUDENT 2 DATABASE HOST INITIALIZER
echo ======================================================================
echo.

set "MYSQL_DIR=C:\Program Files\MySQL\MySQL Server 8.4\bin"
if not exist "%MYSQL_DIR%\mysql.exe" (
    echo [ERROR] MySQL 8.4 bin directory not found at:
    echo %MYSQL_DIR%
    pause
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "E:\Team java\init_and_start_mysql.ps1"

echo.
echo Press any key to close this window...
pause >nul
