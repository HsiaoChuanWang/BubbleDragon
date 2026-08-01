# Bubble Dragon

使用 Java 21、Maven 與 JavaFX 製作的簡易 2D 泡泡龍平台遊戲。

## 開發環境

1. 安裝 Temurin JDK 21。
2. 安裝 Maven。
3. 若使用 VS Code，安裝 Extension Pack for Java。

## 開發中執行與測試

1. 平常修改程式後，使用以下指令快速啟動遊戲：

```powershell
mvn javafx:run
```

2. 若懷疑舊編譯檔影響執行結果，先清除再啟動：

```powershell
mvn clean javafx:run
```

3. 執行自動測試：

```powershell
mvn test
```

`javafx:jlink` 主要用於發布前建立自包含 Runtime，不是日常開發必須執行的指令。

## 建立發布用 Runtime

1. 準備發布新版本時，在專案根目錄執行：

```powershell
mvn clean javafx:jlink
```

2. 確認建立成功後產生以下內容：

```text
target\BubbleDragonRuntime\
target\BubbleDragonRuntime.zip
```

3. 建立安裝程式前，先測試 Runtime：

```powershell
.\target\BubbleDragonRuntime\bin\BubbleDragon.bat
```

> `mvn clean` 會清除整個 `target` 目錄，包含先前建立的 Runtime 與 EXE 安裝檔。需要保留舊版本時，請先將安裝檔複製到其他位置。

## 建立免安裝可攜版（Demo 建議使用）

免安裝版不需要 WiX。玩家的電腦也不需要安裝 Java、JavaFX、Maven 或其他開發工具，解壓縮後即可離線執行。

1. 在專案根目錄建立最新的自包含 Runtime：

```powershell
mvn clean javafx:jlink
```

2. 使用 `jpackage` 建立免安裝的應用程式資料夾：

```powershell
jpackage `
  --type app-image `
  --name BubbleDragon `
  --app-version 1.0.0 `
  --dest "target\portable" `
  --runtime-image "target\BubbleDragonRuntime" `
  --module "com.bubble.dragon/com.bubble.dragon.Main" `
  --icon "src\main\resources\images\BubbleDragon.ico"
```

3. 建立完成後，直接雙擊以下檔案測試遊戲：

```text
target\portable\BubbleDragon\BubbleDragon.exe
```

4. 將整個應用程式資料夾壓縮，方便放到隨身碟、雲端或傳給其他人：

```powershell
Compress-Archive `
  -Path "target\portable\BubbleDragon" `
  -DestinationPath "target\BubbleDragon-免安裝版.zip" `
  -Force
```

5. Demo 電腦上的操作方式：

   1. 解壓縮 `BubbleDragon-免安裝版.zip`。
   2. 開啟解壓縮後的 `BubbleDragon` 資料夾。
   3. 雙擊 `BubbleDragon.exe` 開始遊戲。

> 不可只複製 `BubbleDragon.exe`。必須保留同一資料夾內的 `app`、`runtime` 等所有檔案，遊戲才能正常啟動。

`BubbleDragon.ico` 會作為執行檔圖示；遊戲視窗左上角使用 `player-stand.png`。免安裝版不會自動建立桌面捷徑，如有需要，可以在 `BubbleDragon.exe` 上按右鍵並選擇「傳送到 → 桌面（建立捷徑）」。

## 建立 Windows EXE 安裝程式

1. 開發電腦需要先安裝 WiX Toolset 3.14.1，並確認 `candle.exe` 與 `light.exe` 已加入 PATH。WiX 只用於建立安裝檔，玩家不需要安裝。

2. 將 Windows ICO 圖示放在以下位置：

```text
src\main\resources\images\BubbleDragon.ico
```

ICO 建議同時包含 `16×16`、`32×32`、`48×48`、`128×128` 與 `256×256` 尺寸，讓 Windows 桌面捷徑與開始選單在不同縮放比例下都能清楚顯示。

3. 建立 `1.0.0` 版安裝程式：

```powershell
jpackage --type exe --name BubbleDragon --app-version 1.0.0 --description "Bubble Dragon Game" --vendor "BubbleDragon" --dest "target\installer-v1.0.0" --runtime-image "target\BubbleDragonRuntime" --module "com.bubble.dragon/com.bubble.dragon.Main" --icon "src\main\resources\images\BubbleDragon.ico" --win-upgrade-uuid "8f8a5f1e-3f82-4a0d-b67e-91b5d2cce741" --win-shortcut --win-menu --win-dir-chooser --win-per-user-install
```

`--win-upgrade-uuid` 是 Windows 辨識同一個遊戲不同版本的固定編號。未來發布 `1.0.1`、`1.0.2` 等版本時，必須繼續使用同一個 UUID，不可重新產生或修改，否則 Windows 可能將新舊版視為不同產品。

4. 確認產生的安裝檔位於：

```text
target\installer-v1.0.0\BubbleDragon-1.0.0.exe
```

5. 雙擊 EXE 測試安裝、桌面捷徑、開始選單、自訂圖示與遊戲啟動是否正常。

這個 EXE 已內含遊戲所需的 Java Runtime，可以單獨傳給玩家；玩家不需要安裝 Java、JavaFX、Maven 或 WiX。

## 發布更新版本

1. 完成程式修改與測試。
2. 執行 `mvn clean javafx:jlink` 重建最新 Runtime。
3. 將 `--app-version` 提高，例如從 `1.0.0` 改為 `1.0.1`。
4. 同步改用新的輸出目錄，例如 `target\installer-v1.0.1`。
5. 確認 `--win-upgrade-uuid` 仍為 `8f8a5f1e-3f82-4a0d-b67e-91b5d2cce741`，不要隨版本修改。
6. 重新執行 `jpackage` 指令並測試新安裝檔。

例如建立 `1.0.1` 版：

```powershell
jpackage --type exe --name BubbleDragon --app-version 1.0.1 --description "Bubble Dragon Game" --vendor "BubbleDragon" --dest "target\installer-v1.0.1" --runtime-image "target\BubbleDragonRuntime" --module "com.bubble.dragon/com.bubble.dragon.Main" --icon "src\main\resources\images\BubbleDragon.ico" --win-upgrade-uuid "8f8a5f1e-3f82-4a0d-b67e-91b5d2cce741" --win-shortcut --win-menu --win-dir-chooser --win-per-user-install
```

## 操作

- 左右方向鍵：移動
- 空白鍵：跳躍
- Z：發射泡泡
- Esc：返回首頁

泡泡碰到敵人後會將其困住。請在敵人掙脫前碰到泡泡消滅敵人。消滅關卡內所有敵人後，進入出現的門即可過關。
