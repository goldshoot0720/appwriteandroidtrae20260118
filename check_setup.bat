@echo off
echo ========================================
echo      檢查 Release APK 構建配置
echo ========================================
echo.

set allGood=1

REM 檢查 Java/Keytool
echo 🔍 檢查 Java 環境...
keytool -help >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Java keytool 可用
) else (
    echo ❌ Java keytool 不可用，請確保已安裝 JDK
    set allGood=0
)

echo.

REM 檢查 Gradle
echo 🔍 檢查 Gradle...
call gradlew --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✓ Gradle 可用
) else (
    echo ❌ Gradle 不可用
    set allGood=0
)

echo.

REM 檢查 keystore 配置
echo 🔍 檢查簽名配置...
if exist "keystore.properties" (
    echo ✓ keystore.properties 存在
    
    REM 檢查 keystore 文件
    for /f "tokens=2 delims==" %%a in ('findstr "storeFile" keystore.properties') do (
        if exist "%%a" (
            echo ✓ Keystore 文件存在: %%a
        ) else (
            echo ❌ Keystore 文件不存在: %%a
            set allGood=0
        )
    )
) else (
    echo ❌ keystore.properties 不存在
    echo   請複製 keystore.properties.template 並填入正確信息
    set allGood=0
)

echo.

REM 檢查 build.gradle.kts
echo 🔍 檢查構建配置...
if exist "app\build.gradle.kts" (
    echo ✓ app/build.gradle.kts 存在
) else (
    echo ❌ app/build.gradle.kts 不存在
    set allGood=0
)

echo.
echo ========================================

if %allGood% EQU 1 (
    echo ✅ 所有配置檢查通過！
    echo 你可以運行 build_release.bat 來構建 Release APK
) else (
    echo ❌ 發現配置問題，請修復後再試
)

echo ========================================
pause