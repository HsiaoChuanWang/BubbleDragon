package com.bubble.dragon.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class InfoPanel extends HBox {
    private final Label hp = new Label();
    private final Label enemies = new Label();
    private final Label help = new Label("← → 移動　Space 跳躍　Z 泡泡　Esc 首頁");

    public InfoPanel() {
        getStyleClass().add("info-panel");
        setPadding(new Insets(12, 20, 12, 20));
        setSpacing(24);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(hp, enemies, spacer, help);
    }

    public void update(int health, int enemyCount) {
        hp.setText("HP  " + "♥ ".repeat(Math.max(0, health)));
        enemies.setText("敵人  " + enemyCount);
    }
}
