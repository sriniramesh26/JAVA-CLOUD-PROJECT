# =============================================================================
# SMART CITY TRAFFIC ROUTER - MYSQL 8.4 AUTOMATED INITIALIZER & STARTER
# Student 2: Database Host (Windows)
# =============================================================================
$ErrorActionPreference = "Stop"

$MYSQL_BIN = "C:\Program Files\MySQL\MySQL Server 8.4\bin"
$MYSQLD = Join-Path $MYSQL_BIN "mysqld.exe"
$MYSQL = Join-Path $MYSQL_BIN "mysql.exe"
$ADMIN = Join-Path $MYSQL_BIN "mysqladmin.exe"

$DATA_DIR = "E:\Team java\mysql_data"
$INI_FILE = "E:\Team java\my.ini"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " [Step 1] Initializing MySQL 8.4 Data Directory..." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

if (!(Test-Path $DATA_DIR)) {
    Write-Host "Creating clean data directory at $DATA_DIR..." -ForegroundColor Green
    & $MYSQLD --defaults-file=$INI_FILE --initialize-insecure --console
    Write-Host "[+] Data directory initialized successfully." -ForegroundColor Green
} else {
    Write-Host "[+] Existing data directory found at $DATA_DIR" -ForegroundColor Green
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " [Step 2] Starting MySQL Server (mysqld.exe on Port 3306)..." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

# Check if already running
$running = $false
try {
    $tcp = New-Object System.Net.Sockets.TcpClient
    $iar = $tcp.BeginConnect("127.0.0.1", 3306, $null, $null)
    $running = $iar.AsyncWaitHandle.WaitOne(1000, $false)
    if ($running) { $tcp.EndConnect($iar); $tcp.Close() }
} catch { $running = $false }

if (!$running) {
    Write-Host "Spawning background mysqld process..." -ForegroundColor Green
    Start-Process -FilePath $MYSQLD -ArgumentList "--defaults-file=`"$INI_FILE`"" -WindowStyle Hidden
    Start-Sleep -Seconds 4
} else {
    Write-Host "[+] MySQL server is already listening on Port 3306." -ForegroundColor Green
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " [Step 3] Importing Schema (4 Tables) & Seeding Intersections..." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

Write-Host "Importing smart_traffic_schema.sql..." -ForegroundColor Green
Get-Content "E:\Team java\smart_traffic_schema.sql" | & $MYSQL -u root --port=3306

Write-Host "Importing seed_intersections.sql..." -ForegroundColor Green
Get-Content "E:\Team java\seed_intersections.sql" | & $MYSQL -u root --port=3306

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " [Step 4] Creating Remote User: traffic_app@'%'..." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

$userSql = @"
CREATE USER IF NOT EXISTS 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
ALTER USER 'traffic_app'@'%' IDENTIFIED BY 'SmartCity2026!';
GRANT ALL PRIVILEGES ON smart_traffic.* TO 'traffic_app'@'%';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user='traffic_app';
"@

$userSql | & $MYSQL -u root --port=3306
Write-Host "[+] Remote user 'traffic_app'@'%' configured with password 'SmartCity2026!'" -ForegroundColor Green

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " [Step 5] Verifying smart_traffic Tables & Constraints..." -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Cyan

Get-Content "E:\Team java\verify_database.sql" | & $MYSQL -u traffic_app -pSmartCity2026! --port=3306

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host " [SUCCESS] STUDENT 2 DATABASE HOST IS LIVE & READY ON PORT 3306!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
