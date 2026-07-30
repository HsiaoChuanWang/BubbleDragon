package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.util.Constants;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class HomeView {
    private final BubbleDragonApp app;

    public HomeView(BubbleDragonApp app) {
        this.app = app;
    }

    public Scene createScene() {
        Label title = new Label("BUBBLE\nDRAGON");
        title.getStyleClass().add("title");
        Label subtitle = new Label("用泡泡困住怪物，成為森林裡最勇敢的小龍！");
        subtitle.getStyleClass().add("subtitle");
        Button start = new Button("開始遊戲");
        start.getStyleClass().add("primary-button");
        start.setOnAction(e -> app.showStory());
        Label controls = new Label("方向鍵移動　·　Space 跳躍　·　Z 發射泡泡");
        controls.getStyleClass().add("hint");
        VBox root = new VBox(26, title, subtitle, start, controls);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().addAll("screen", "home-screen");
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }
}
