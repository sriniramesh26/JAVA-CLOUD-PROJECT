@echo off
:: ============================================================================
:: SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
:: Student 2: Database Verification & Inspection Runner
:: ============================================================================
echo ======================================================================
echo  Student 2: Testing MySQL Port 3306 & Verifying smart_traffic Tables
echo ======================================================================
echo.

set "MYSQL_CMD=mysql"
if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
    set "MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
)

set /p DB_HOST=Enter Database Host (default localhost): 
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_USER=Enter MySQL User (default traffic_app): 
if "%DB_USER%"=="" set DB_USER=traffic_app

set /p DB_PASS=Enter MySQL Password (default SmartCity2026!): 
if "%DB_PASS%"=="" set DB_PASS=SmartCity2026!

echo.
echo Connecting to %DB_HOST%:3306 as %DB_USER%...
"%MYSQL_CMD%" -h %DB_HOST% -u %DB_USER% -p%DB_PASS% < verify_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database verification executed successfully!
) else (
    echo.
    echo [ERROR] Failed to query database. Please check credentials, port 3306, and host.
)
pause
