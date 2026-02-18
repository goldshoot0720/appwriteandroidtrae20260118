@echo off
echo 正在生成 Android 簽名 keystore...

REM 設置 keystore 參數
set KEYSTORE_NAME=release-key.keystore
set KEY_ALIAS=release-key
set VALIDITY_DAYS=10000

REM 檢查是否已存在 keystore
if exist %KEYSTORE_NAME% (
    echo Keystore 已存在: %KEYSTORE_NAME%
    echo 如果要重新生成，請先刪除現有的 keystore 文件
    pause
    exit /b 1
)

echo.
echo 請輸入以下信息來生成 keystore:
echo.

REM 生成 keystore
keytool -genkey -v -keystore %KEYSTORE_NAME% -alias %KEY_ALIAS% -keyalg RSA -keysize 2048 -validity %VALIDITY_DAYS%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Keystore 生成成功: %KEYSTORE_NAME%
    echo.
    echo 請將以下信息保存到 keystore.properties 文件中:
    echo storeFile=%KEYSTORE_NAME%
    echo keyAlias=%KEY_ALIAS%
    echo storePassword=你設置的密碼
    echo keyPassword=你設置的密碼
    echo.
    echo 注意: 請妥善保管 keystore 文件和密碼！
) else (
    echo ✗ Keystore 生成失敗
)

pause