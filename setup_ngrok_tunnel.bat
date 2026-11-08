@echo off
:: =============================================================================
:: SMART CITY TRAFFIC ROUTER - 300 KM DISTANCE NGROK TUNNEL (PORT 3306)
:: =============================================================================
title 300 KM Remote TCP Tunnel - MySQL Port 3306
color 0B

powershell -NoProfile -ExecutionPolicy Bypass -File "E:\Team java\setup_ngrok_tunnel.ps1"

pause
