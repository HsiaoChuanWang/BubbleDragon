package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.util.Constants;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class HomeView {
    private final BubbleDragonApp app;

    public HomeView(BubbleDragonApp app) {
        this.app = app;
    }

    public Scene createScene() {
        Region topSpacing = new Region();
        topSpacing.setPrefHeight(280);
        Button start = new Button("START");
        start.getStyleClass().add("custom-button");
        start.setOnAction(e -> app.showStory());
        Label controls = new Label("← → 移動　·　Space 跳躍　·　Z 發射泡泡");
        controls.getStyleClass().add("hint");
        VBox root = new VBox(26, topSpacing, start, controls);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().addAll("screen", "home-screen");
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }
}
