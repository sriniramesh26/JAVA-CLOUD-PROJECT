@echo off
:: =============================================================================
:: SMART CITY TRAFFIC ROUTER - START MYSQL 8.4 IN FOREGROUND CONSOLE
:: Keeps MySQL running in an open console window (No admin rights required)
:: =============================================================================
title MySQL 8.4 Server - Smart City Traffic Database (Port 3306)
color 0E

echo ======================================================================
echo   SMART CITY TRAFFIC ENGINE - MYSQL 8.4 SERVER (PORT 3306)
echo   Keep this console window open while Student 1 connects!
echo ======================================================================
echo.

set "MYSQLD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
set "INI_FILE=E:\Team java\my.ini"

"%MYSQLD%" --defaults-file="%INI_FILE%" --console

pause
