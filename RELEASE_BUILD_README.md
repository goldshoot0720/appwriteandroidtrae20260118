# Android Release APK 自動構建指南

本專案已配置自動生成已簽名的 Release APK。請按照以下步驟進行設置和使用。

## 🚀 快速開始

### 1. 首次設置

1. **生成 Keystore**
   ```bash
   generate_keystore.bat
   ```
   - 運行此腳本生成簽名用的 keystore 文件
   - 按提示輸入相關信息（公司名稱、組織等）
   - **重要**: 請記住設置的密碼！

2. **配置簽名信息**
   ```bash
   copy keystore.properties.template keystore.properties
   ```
   - 複製模板文件
   - 編輯 `keystore.properties` 文件，填入正確的密碼信息

3. **檢查配置**
   ```bash
   check_setup.bat
   ```
   - 運行此腳本檢查所有配置是否正確

### 2. 構建 Release APK

```bash
build_release.bat
```

此腳本會：
- 清理之前的構建
- 構建已簽名的 Release APK
- 啟用代碼混淆和資源壓縮
- 自動命名 APK 文件（包含版本信息）

## 📁 文件說明

| 文件 | 說明 |
|------|------|
| `generate_keystore.bat` | 生成 Android 簽名 keystore |
| `keystore.properties.template` | Keystore 配置模板 |
| `keystore.properties` | 實際的 keystore 配置（不會提交到 Git） |
| `build_release.bat` | 自動構建 Release APK |
| `check_setup.bat` | 檢查構建環境配置 |

## 🔧 配置詳情

### Gradle 配置特性

- **自動簽名**: 自動應用 keystore 簽名
- **代碼混淆**: 啟用 ProGuard 混淆
- **資源壓縮**: 移除未使用的資源
- **APK 命名**: 自動包含版本號的 APK 名稱
- **多環境支持**: Debug 和 Release 版本區分

### 安全性

- `keystore.properties` 和 `*.keystore` 文件已加入 `.gitignore`
- 敏感信息不會被提交到版本控制系統
- 支持環境變數配置（適用於 CI/CD）

## 🎯 輸出位置

構建完成的 APK 文件位於：
```
app/build/outputs/apk/release/
```

APK 文件命名格式：
```
app-release-v1.0-1.apk
```

## ⚠️ 重要提醒

1. **備份 Keystore**: 請務必備份 keystore 文件和密碼
2. **密碼安全**: 不要將密碼提交到版本控制系統
3. **版本管理**: 發布前記得更新 `versionCode` 和 `versionName`

## 🔄 CI/CD 集成

如需在 CI/CD 環境中使用，可以設置以下環境變數：
- `RELEASE_STORE_FILE`: keystore 文件路徑
- `RELEASE_STORE_PASSWORD`: keystore 密碼
- `RELEASE_KEY_ALIAS`: key 別名
- `RELEASE_KEY_PASSWORD`: key 密碼

## 🐛 故障排除

### 常見問題

1. **找不到 keytool 命令**
   - 確保已安裝 JDK 並配置 PATH 環境變數

2. **簽名失敗**
   - 檢查 `keystore.properties` 中的密碼是否正確
   - 確認 keystore 文件路徑是否正確

3. **構建失敗**
   - 運行 `gradlew clean` 清理後重試
   - 檢查 Android SDK 是否正確安裝

### 手動構建命令

如果腳本無法使用，可以手動運行：
```bash
gradlew clean
gradlew assembleRelease
```

---

🎉 現在你可以輕鬆生成已簽名的 Release APK 了！