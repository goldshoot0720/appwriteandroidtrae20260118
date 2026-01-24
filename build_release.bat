@echo off
echo ========================================
echo    自動構建已簽名的 Release APK
echo ========================================
echo.

REM 檢查是否存在 keystore 配置
if not exist "keystore.properties" (
    echo ❌ 找不到 keystore.properties 文件
    echo.
    echo 請先執行以下步驟:
    echo 1. 運行 generate_keystore.bat 生成 keystore
    echo 2. 複製 keystore.properties.template 為 keystore.properties
    echo 3. 在 keystore.properties 中填入正確的密碼信息
    echo.
    pause
    exit /b 1
)

echo ✓ 找到 keystore 配置文件
echo.

REM 清理之前的構建
echo 🧹 清理之前的構建...
call gradlew clean
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 清理失敗
    pause
    exit /b 1
)

echo ✓ 清理完成
echo.

REM 構建 Release APK
echo 🔨 開始構建 Release APK...
call gradlew assembleRelease
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 構建失敗
    pause
    exit /b 1
)

echo.
echo ✅ 構建成功！
echo.

REM 顯示生成的 APK 位置
echo 📱 生成的 APK 文件位置:
for /r "app\build\outputs\apk\release" %%f in (*.apk) do (
    echo    %%f
)

echo.
echo 🎉 Release APK 構建完成！
echo.

REM 詢問是否打開輸出目錄
set /p openDir="是否打開 APK 輸出目錄? (y/n): "
if /i "%openDir%"=="y" (
    start "" "app\build\outputs\apk\release"
)

pause