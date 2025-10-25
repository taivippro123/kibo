# Script debug badge issue
Write-Host "=== Debug Badge Issue ===" -ForegroundColor Green

# Clear old logs
Write-Host "`n1. Clearing old logs..." -ForegroundColor Yellow
adb logcat -c

# Start app
Write-Host "2. Starting app..." -ForegroundColor Yellow
adb shell monkey -p com.example.kibo -c android.intent.category.LAUNCHER 1 | Out-Null

# Wait for app to start
Write-Host "3. Waiting for app to start (5 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Get logs
Write-Host "4. Fetching logs..." -ForegroundColor Yellow
Write-Host "`n=== LOGS ===" -ForegroundColor Cyan

$logs = adb logcat -d | Select-String "MainActivity.*Badge|CartFragment.*Items|updateCartBadge|ShortcutBadger|bottomNav" 

if ($logs) {
    $logs | ForEach-Object {
        Write-Host $_.Line
    }
} else {
    Write-Host "No relevant logs found. Possible reasons:" -ForegroundColor Red
    Write-Host "  - App not started yet"
    Write-Host "  - User not logged in"
    Write-Host "  - User hasn't navigated to Cart tab"
    Write-Host "`nPlease:"
    Write-Host "  1. Open the app"
    Write-Host "  2. Login"
    Write-Host "  3. Go to Cart tab"
    Write-Host "  4. Run this script again"
}

Write-Host "`n=== Manual monitoring ===" -ForegroundColor Green
Write-Host "To monitor logs in real-time, run:"
Write-Host "  adb logcat | Select-String 'MainActivity|CartFragment'" -ForegroundColor Cyan
