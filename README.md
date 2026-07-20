# Bubble Dragon

使用 Java 21、Maven 與 JavaFX 製作的簡易 2D 泡泡龍平台遊戲。

## 執行

1. 安裝 Temurin JDK 21、Maven 與 VS Code 的 Extension Pack for Java。
2. 在專案根目錄執行 `mvn clean javafx:run`。

## 操作

- 左右方向鍵：移動
- 空白鍵：跳躍
- Z：發射泡泡
- Esc：返回首頁

泡泡碰到敵人後會將其困住。請在三秒內碰到泡泡消滅敵人，否則敵人會掙脫。消滅所有敵人後，進入右上方出現的門即可過關。
