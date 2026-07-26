# Bubble Dragon（泡泡龍 Java 復刻版）

Bubble Dragon 是一個使用 JavaFX 製作的 2D 平台遊戲，核心玩法參考經典「泡泡龍」。專案採用物件導向、狀態機與分層架構，先完成可單人遊玩的 MVP，並保留日後加入多關卡、音效與魔王關卡的擴充空間。

## 開發環境

- Visual Studio Code
- Temurin JDK 21
- Extension Pack for Java
- Maven
- JavaFX 21（由 Maven 自動下載，不需另外安裝 JavaFX SDK）

專案不再使用 Gradle、LibGDX、gdx-liftoff、LWJGL3 或 construo。

## 核心遊戲機制（MVP）

- **生命值（HP）**：玩家初始與最大生命值皆為 3，顯示於畫面左上角。
- **玩家操作**：方向鍵左右移動、空白鍵跳躍、`Z` 鍵發射泡泡。
- **地形互動**：玩家與敵人會受到重力影響，並與實體地磚發生碰撞。
- **泡泡機制**：
    1. 玩家發射泡泡，泡泡可困住碰到的敵人。
    2. 被困住的敵人進入 `TRAPPED` 狀態。
    3. 玩家必須在 3 秒內碰觸該泡泡以消滅敵人。
    4. 超過 3 秒未擊破泡泡，敵人會掙脫並恢復移動。
- **通關條件**：敵人全數消滅後生成通關門，玩家碰觸通關門即可過關。
- **失敗條件**：玩家 HP 歸零時切換至遊戲結束畫面。

## 架構原則

- `Main` 是 JavaFX 程式進入點，只負責啟動應用程式。
- `BubbleDragonApp` 繼承 `Application`，持有主要 `Stage` 並負責畫面切換。
- 每個畫面是一個 JavaFX `Scene`，由對應的 view 類別建立。
- `GameController` 管理輸入、更新、碰撞、勝敗判斷等遊戲流程。
- `GameLoop` 使用 JavaFX `AnimationTimer` 驅動每一幀的更新與繪製。
- entity 與 map 類別不直接處理畫面切換，避免模型與 UI 過度耦合。
- 圖形先使用 JavaFX 的 `Rectangle`、`Circle`、`Text` 等基本節點繪製；日後可直接替換為圖片素材。

## 專案目錄架構

```text
BubbleDragon/
├── pom.xml                                  # Maven、JDK 21、JavaFX 與執行外掛設定
├── README.md                                # 安裝、執行方式與操作說明
├── PROJECT_STRUCTURE.md                     # 專案規格與架構文件
├── .gitignore
│
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java             # Java 模組與 JavaFX 模組宣告
    │   │   └── com/bubble/dragon/
    │   │       ├── Main.java                # 一般 main() 入口，啟動 JavaFX
    │   │       ├── BubbleDragonApp.java     # Application、Stage 與 Scene 切換
    │   │       │
    │   │       ├── controller/
    │   │       │   └── GameController.java  # 遊戲流程、輸入、更新與勝敗判斷
    │   │       │
    │   │       ├── entity/
    │   │       │   ├── GameObject.java      # 座標、大小、速度等共用屬性
    │   │       │   ├── player/
    │   │       │   │   ├── Player.java
    │   │       │   │   └── PlayerState.java # IDLE、MOVING、JUMPING、DEAD
    │   │       │   ├── enemy/
    │   │       │   │   ├── Enemy.java
    │   │       │   │   └── EnemyState.java  # MOVING、TRAPPED、DEFEATED
    │   │       │   └── weapon/
    │   │       │       └── Bubble.java       # 泡泡與被困住的敵人
    │   │       │
    │   │       ├── game/
    │   │       │   └── GameLoop.java         # AnimationTimer 遊戲主迴圈
    │   │       │
    │   │       ├── map/
    │   │       │   ├── GameMap.java          # 關卡內地磚、出生點與敵人配置
    │   │       │   ├── LevelLoader.java      # 從 resources 載入關卡資料
    │   │       │   └── Tile.java             # 地磚位置與 solid 屬性
    │   │       │
    │   │       ├── physics/
    │   │       │   └── OverlapChecker.java   # AABB 碰撞偵測工具
    │   │       │
    │   │       ├── ui/
    │   │       │   ├── GameCanvas.java       # Canvas 遊戲區域與圖形繪製
    │   │       │   └── HUD.java        # HP、敵人數量與提示文字
    │   │       │
    │   │       ├── util/
    │   │       │   ├── Constants.java        # 視窗、速度、重力等固定值
    │   │       │   └── CountdownTimer.java   # 泡泡困敵 3 秒倒數
    │   │       │
    │   │       └── view/
    │   │           ├── HomeView.java          # 首頁與開始遊戲按鈕
    │   │           ├── GameView.java          # 遊戲畫面與鍵盤事件綁定
    │   │           └── GameOverView.java      # 勝利／失敗與重新開始
    │   │
    │   └── resources/
    │       ├── css/
    │       │   └── application.css           # JavaFX UI 樣式
    │       ├── images/                        # 玩家、敵人、泡泡、地磚圖片
    │       ├── sounds/                        # 跳躍、發射、困住、破裂音效
    │       └── maps/
    │           └── level1.json                # 第一關資料
    │
    └── test/
        └── java/com/bubble/dragon/
            ├── physics/
            │   └── OverlapCheckerTest.java
            └── util/
                └── CountdownTimerTest.java
```

## Package 職責

| Package             | 職責                                   |
| ------------------- | -------------------------------------- |
| `com.bubble.dragon` | JavaFX 啟動與全域畫面切換              |
| `controller`        | 協調遊戲模型、輸入、每幀更新與結果判斷 |
| `entity`            | 遊戲物件的共用資料與行為               |
| `entity.player`     | 玩家狀態、移動、跳躍與生命值           |
| `entity.enemy`      | 敵人移動、受困、掙脫與消滅狀態         |
| `entity.weapon`     | 泡泡移動、存活時間與困敵資訊           |
| `game`              | JavaFX 遊戲主迴圈                      |
| `map`               | 關卡內容、地磚與關卡載入               |
| `physics`           | 碰撞判斷與物理相關工具                 |
| `ui`                | 遊戲中的 Canvas 繪製與 HUD             |
| `util`              | 全域常數與通用計時工具                 |
| `view`              | 首頁、遊戲、結算等 Scene 畫面          |

## Maven 執行方式

在 VS Code 開啟專案根目錄後，可由終端機執行：

```powershell
mvn clean javafx:run
```

執行測試：

```powershell
mvn test
```

JavaFX 的程式修改後需要重新編譯或重新執行才會反映。若只調整 CSS，開發階段可在程式重新載入樣式表後查看結果；JavaFX 本身不像網頁開發伺服器預設提供完整的即時熱更新。

## Maven 設定方向

`pom.xml` 將包含以下主要項目：

- `maven.compiler.release` 設為 `21`。
- `javafx-controls`：JavaFX 視窗、控制元件、Canvas 與動畫。
- `javafx-media`：預留音效播放功能。
- `jackson-databind`：讀取 JSON 關卡資料。
- `junit-jupiter`：單元測試。
- `javafx-maven-plugin`：使用 `mvn javafx:run` 啟動程式。
- `maven-surefire-plugin`：執行 JUnit 5 測試。

## 第一階段完成標準

1. `mvn clean javafx:run` 可以成功開啟首頁。
2. 點擊開始後可進入遊戲畫面。
3. 玩家可以左右移動、跳躍並發射泡泡。
4. 敵人、地板、平台、泡泡與 HUD 可使用簡單幾何圖形呈現。
5. 泡泡能困住敵人，並套用 3 秒掙脫規則。
6. HP 歸零會顯示失敗畫面；消滅全部敵人並進門會顯示勝利畫面。
7. 可以從結算畫面重新開始或返回首頁。

## 後續擴充

- 將簡單圖形替換成圖片與動畫素材。
- 加入音效、背景音樂與音量設定。
- 加入更多關卡、不同敵人與魔王。
- 增加暫停、設定、排行榜與存檔功能。
- 使用 Maven 搭配 `jpackage` 產生 Windows 安裝包或可執行應用程式。
