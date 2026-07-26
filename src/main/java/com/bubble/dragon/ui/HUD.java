package com.bubble.dragon.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class HUD extends HBox {
    private final Label hp = new Label();
    private final Label enemies = new Label();
    private final Label help = new Label("← → 移動　Space 跳躍　Z 泡泡　Esc 首頁");

    public HUD() {
        getStyleClass().add("info-panel");
        setPadding(new Insets(12, 20, 12, 20));
        setSpacing(24);

        // spacer: 占用中間多餘的空間，把左右兩邊的元件推開
        Region spacer = new Region();

        // HBox 是一種版面配置 Layout，會把裡面的元件由左到右排列
        // Priority 將多出來的空間給 spacer
        HBox.setHgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(hp, enemies, spacer, help);
    }

    public void update(int health, int enemyCount) {
        hp.setText("HP  " + "♥ ".repeat(Math.max(0, health)));
        enemies.setText("敵人  " + enemyCount);
    }
}
