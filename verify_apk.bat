@echo off
echo ========================================
echo      驗證 Release APK 簽名狀態
echo ========================================
echo.

set APK_PATH=app\build\outputs\apk\release\app-release.apk

if not exist "%APK_PATH%" (
    echo ❌ 找不到 APK 文件: %APK_PATH%
    echo 請先運行 build_release.bat 構建 APK
    pause
    exit /b 1
)

echo ✓ 找到 APK 文件: %APK_PATH%
echo.

REM 顯示 APK 文件信息
echo 📱 APK 文件信息:
for %%f in ("%APK_PATH%") do (
    echo    文件大小: %%~zf bytes
    echo    修改時間: %%~tf
)
echo.

REM 檢查簽名狀態
echo 🔐 檢查簽名狀態...
for /f "tokens=*" %%a in ('dir /s /b "C:\Users\%USERNAME%\AppData\Local\Android\Sdk\build-tools\*\apksigner.bat" 2^>nul') do (
    set APKSIGNER=%%a
    goto :found
)

:found
if defined APKSIGNER (
    echo 使用 apksigner: %APKSIGNER%
    echo.
    "%APKSIGNER%" verify --verbose "%APK_PATH%"
    echo.
    if %ERRORLEVEL% EQU 0 (
        echo ✅ APK 簽名驗證成功！
    ) else (
        echo ❌ APK 簽名驗證失敗！
    )
) else (
    echo ❌ 找不到 apksigner 工具
    echo 請確保 Android SDK 已正確安裝
)

echo.
echo ========================================
pause