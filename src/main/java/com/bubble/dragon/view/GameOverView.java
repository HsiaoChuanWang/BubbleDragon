package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.util.Constants;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class GameOverView {
    private final BubbleDragonApp app;
    private final boolean victory;

    public GameOverView(BubbleDragonApp app, boolean victory) { 
        this.app = app; 
        this.victory = victory;
    }

    public Scene createScene() {
        Label icon = new Label(victory ? "★" : "×"); icon.getStyleClass().add(victory ? "victory-icon" : "defeat-icon");
        Label title = new Label(victory ? "闖關成功！" : "遊戲結束"); title.getStyleClass().add("result-title");
        Label message = new Label(victory ? "所有敵人都被泡泡打敗了！" : "別灰心，小龍準備好再試一次。 "); message.getStyleClass().add("subtitle");
        Button retry = new Button("再玩一次"); retry.getStyleClass().add("primary-button"); retry.setOnAction(e -> app.startGame());
        Button home = new Button("返回首頁"); home.getStyleClass().add("secondary-button"); home.setOnAction(e -> app.showHome());
        HBox buttons = new HBox(16, retry, home); buttons.setAlignment(Pos.CENTER);
        VBox root = new VBox(22, icon, title, message, buttons); root.setAlignment(Pos.CENTER); root.getStyleClass().addAll("screen", "result-screen");
        return new Scene(root, Constants.WIDTH, Constants.HEIGHT);
    }
}
