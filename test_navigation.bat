@echo off
echo ========================================
echo Android App Navigation Test
echo ========================================
echo.

echo Checking if emulator is running...
adb devices | findstr "emulator"
if %errorlevel% neq 0 (
    echo ERROR: No emulator detected!
    echo Please start an emulator first.
    pause
    exit /b 1
)
echo Emulator detected!
echo.

echo Installing latest APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk
if %errorlevel% neq 0 (
    echo ERROR: Failed to install APK
    pause
    exit /b 1
)
echo.

echo Starting MainActivity...
adb shell am start -n com.example.appwriteandroidtrae/.MainActivity
timeout /t 2 /nobreak >nul
echo.

echo ========================================
echo TEST INSTRUCTIONS:
echo ========================================
echo.
echo Please manually test the following on the emulator:
echo.
echo 1. Main Menu (主頁面):
echo    - Verify all 5 menu cards are visible:
echo      * 訂閱管理 (Subscription Management)
echo      * 銀行統計 (Bank Statistics)
echo      * 食物管理 (Food Management)
echo      * 鋒兄筆記 (Feng Notes)
echo      * 鋒兄常用 (Feng Common)
echo.
echo 2. Test Navigation and Back Button:
echo    a) Click "食物管理" (Food Management)
echo       - Verify food list loads
echo       - Click back button (←) - should return to main menu
echo.
echo    b) Click "鋒兄筆記" (Feng Notes)
echo       - Verify article list loads
echo       - Click back button (←) - should return to main menu
echo.
echo    c) Click "鋒兄常用" (Feng Common)
echo       - Verify expandable list loads with sites and notes
echo       - Click back button (←) - should return to main menu
echo.
echo    d) Click "訂閱管理" (Subscription Management)
echo       - Verify subscription list loads
echo       - Click back button (←) - should return to main menu
echo.
echo    e) Click "銀行統計" (Bank Statistics)
echo       - Verify bank list loads
echo       - Click back button (←) - should return to main menu
echo.
echo 3. Verify Data Display:
echo    - Food items show: name, amount, price, shop, expiry date
echo    - Articles show: title, content preview, date, URL count
echo    - Common items show: expandable groups with sites/notes
echo    - Subscriptions show: name, site, price, account, next date
echo    - Banks show: name, account, deposit, withdrawals, transfer
echo.
echo ========================================
echo Current app status:
adb shell dumpsys window | findstr "mCurrentFocus"
echo.
echo ========================================
echo Test completed! Check the emulator.
echo ========================================
pause
