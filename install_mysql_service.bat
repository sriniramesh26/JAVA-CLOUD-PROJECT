@echo off
:: =============================================================================
:: SMART CITY TRAFFIC ROUTER - INSTALL MYSQL 8.4 AS WINDOWS SERVICE
:: Run this file by right-clicking and selecting "Run as administrator"
:: =============================================================================
title Install MySQL 8.4 Service - Smart Traffic
color 0A

echo ======================================================================
echo   Installing MySQL 8.4 as a Permanent Background Windows Service
echo ======================================================================
echo.

set "MYSQLD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
set "INI_FILE=E:\Team java\my.ini"

if not exist "%MYSQLD%" (
    echo [ERROR] mysqld.exe not found at %MYSQLD%
    pause
    exit /b 1
)

echo [1/2] Registering Windows Service 'MySQL84'...
"%MYSQLD%" --install MySQL84 --defaults-file="%INI_FILE%"

echo.
echo [2/2] Starting MySQL84 service...
net start MySQL84

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ======================================================================
    echo  [SUCCESS] MySQL 8.4 is now permanently running as a Windows Service!
    echo  It will automatically run on Port 3306 in the background.
    echo ======================================================================
) else (
    echo.
    echo [NOTICE] If service failed to start, make sure to Run as Administrator,
    echo or use 'start_mysql_foreground.bat' to run it in a visible window.
)

pause
