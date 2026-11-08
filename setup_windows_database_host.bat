@echo off
:: ============================================================================
:: SMART CITY TRAFFIC SIGNAL & EMERGENCY VEHICLE GREEN CORRIDOR ROUTER
:: Student 2: Windows Database Host & OS Firewall Configuration
:: ============================================================================
echo ======================================================================
echo  Student 2: Configuring Windows Firewall (Port 3306) & Remote Access
echo ======================================================================
echo.

:: 1. Open Windows Firewall Port 3306
echo [1/3] Adding Inbound Rule for Port 3306 in Windows Defender Firewall...
netsh advfirewall firewall show rule name="MySQL Remote 3306" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    netsh advfirewall firewall add rule name="MySQL Remote 3306" dir=in action=allow protocol=TCP localport=3306
    echo [+] Windows Firewall rule created successfully for Port 3306 TCP.
) else (
    echo [+] Windows Firewall rule already exists.
)

:: 2. Display Host Network Information
echo.
echo [2/3] Fetching Your Network IP Address...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    echo [+] Local LAN IP: %%a
)

echo.
echo ======================================================================
echo  [SUCCESS] Firewall Configuration Complete!
echo  If testing over LAN/Wi-Fi: Share your Local IP above with Student 1.
echo  If testing across 300 KM: Run 'setup_ngrok_tunnel.bat'
echo ======================================================================
pause
