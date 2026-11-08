# =============================================================================
# SMART CITY TRAFFIC ROUTER - 300 KM REMOTE TCP TUNNEL FOR STUDENT 1
# Student 2: Database Host (ngrok TCP 3306)
# =============================================================================

$NGROK_EXE = "E:\Team java\ngrok.exe"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   STUDENT 2: 300 KM REMOTE DATABASE TUNNEL FOR STUDENT 1 (PORT 3306) " -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Check if ngrok exists in PATH or workspace
if (Get-Command ngrok.exe -ErrorAction SilentlyContinue) {
    $NGROK_CMD = "ngrok"
} elseif (Test-Path $NGROK_EXE) {
    $NGROK_CMD = $NGROK_EXE
} else {
    Write-Host "[!] ngrok.exe not found in workspace." -ForegroundColor Yellow
    Write-Host "    Downloading official ngrok v3 for Windows..." -ForegroundColor Green
    $zipPath = "E:\Team java\ngrok.zip"
    $url = "https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip"
    try {
        Invoke-WebRequest -Uri $url -OutFile $zipPath
        Expand-Archive -Path $zipPath -DestinationPath "E:\Team java" -Force
        Remove-Item $zipPath -Force
        $NGROK_CMD = $NGROK_EXE
        Write-Host "[+] ngrok installed successfully to E:\Team java\ngrok.exe" -ForegroundColor Green
    } catch {
        Write-Host "[-] Automatic download failed: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "    Please download ngrok manually from https://ngrok.com and put ngrok.exe in this folder." -ForegroundColor Yellow
        exit 1
    }
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " INSTRUCTIONS FOR 300 KM DISTANCE CONNECTIVITY" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "1. If you haven't added your free ngrok auth token yet, get it from:"
Write-Host "   https://dashboard.ngrok.com/get-started/your-authtoken" -ForegroundColor Cyan
Write-Host "   And run: ngrok config add-authtoken <YOUR_TOKEN>"
Write-Host ""
Write-Host "2. Starting TCP tunnel on Port 3306..." -ForegroundColor Green
Write-Host "   Look for the line that says:" -ForegroundColor Yellow
Write-Host "   Forwarding   tcp://<host>:<port> -> localhost:3306" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Share that <host> and <port> with Student 1!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

& $NGROK_CMD tcp 3306
