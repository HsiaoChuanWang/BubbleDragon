/** 宣告本應用程式使用的 Java 模組及對外開放的 package。 */
module com.bubble.dragon {
    // 公開的 BubbleDragonApp.start(Stage) 使用 Stage，因此將 javafx.graphics
    // 傳遞給所有讀取本模組的程式，避免公開 API 的型別不可見。
    requires transitive javafx.graphics;
    // JavaFX 控制元件與預留的音效功能。
    requires javafx.controls;
    requires javafx.media;
    // 將 level1.json 轉換成 GameMap。
    requires com.fasterxml.jackson.databind;

    exports com.bubble.dragon;
    exports com.bubble.dragon.entity;
    exports com.bubble.dragon.entity.player;
    exports com.bubble.dragon.entity.enemy;
    exports com.bubble.dragon.entity.weapon;
    exports com.bubble.dragon.map;
    exports com.bubble.dragon.physics;
    exports com.bubble.dragon.util;

    // 允許 Jackson 透過反射建立地圖資料物件。
    opens com.bubble.dragon.map to com.fasterxml.jackson.databind;
}
