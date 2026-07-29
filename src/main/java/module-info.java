/**
 * 泡泡龍遊戲的 Java 模組設定。
 * 告訴 Java：
 * 
 * - 這個模組叫什麼名字。
 * - 需要哪些外部模組。
 * - 哪些 package 可以讓其他模組使用。
 * - 哪些 package 可以讓 Jackson 等工具使用反射。
 *
 * requires：宣告使用的外部模組。
 * exports：允許其他模組使用指定 package。
 * opens：允許指定模組透過 Reflection 讀取 package 內容。
 */

// src/main/java 底下所有 Java package 都屬於同一個模組
// 其他模組主要是專案引入的 Java 或第三方套件
module com.bubble.dragon {

    // JavaFX 視窗與圖形功能；使用本遊戲模組的程式也能存取 javafx.graphics。
    requires transitive javafx.graphics;

    // JavaFX 按鈕、標籤等介面元件。
    requires javafx.controls;

    // JavaFX 音效與音樂功能。
    requires javafx.media;

    // 使用 Jackson 將地圖 JSON 轉換成 Java 物件。
    requires com.fasterxml.jackson.databind;

    // 應用程式入口。
    exports com.bubble.dragon;

    // 遊戲物件及其分類。
    exports com.bubble.dragon.entity;
    exports com.bubble.dragon.entity.player;
    exports com.bubble.dragon.entity.enemy;

    // Boss 實體獨立成 package，供控制器與繪圖層共同使用。
    exports com.bubble.dragon.entity.boss;
    exports com.bubble.dragon.entity.weapon;

    // 地圖資料。
    exports com.bubble.dragon.map;

    // 碰撞與物理處理。
    exports com.bubble.dragon.physics;

    // 共用工具。
    exports com.bubble.dragon.util;

    // 允許 Jackson 讀取地圖物件並填入 JSON 資料。
    opens com.bubble.dragon.map to com.fasterxml.jackson.databind;
}
