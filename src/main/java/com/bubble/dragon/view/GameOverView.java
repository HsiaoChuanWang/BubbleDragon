package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.util.Constants;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public final class GameOverView {
    private final BubbleDragonApp app;
    private final boolean victory;

    public GameOverView(BubbleDragonApp app, boolean victory) {
        this.app = app;
        this.victory = victory;
    }

    public Scene createScene() {
        return victory ? createVictoryScene() : createDefeatScene();
    }

    private Scene createVictoryScene() {
        Button home = new Button("HOME");
        home.getStyleClass().add("custom-button");
        home.setOnAction(event -> app.showHome());

        HBox buttons = createButtonRow(home);
        Pane root = createBackgroundScreen(buttons, "success-screen");
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private Scene createDefeatScene() {
        Button retry = new Button("PLAY AGAIN");
        retry.getStyleClass().add("custom-button");
        retry.setOnAction(event -> app.startGame());

        Button home = new Button("HOME");
        home.getStyleClass().add("custom-button");
        home.setOnAction(event -> app.showHome());

        HBox buttons = createButtonRow(retry, home);
        Pane root = createBackgroundScreen(buttons, "game-over-screen");
        return new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
    }

    private HBox createButtonRow(Button... buttonsToAdd) {
        HBox buttons = new HBox(22, buttonsToAdd);
        buttons.setAlignment(Pos.CENTER);
        buttons.setLayoutX(0);
        buttons.setLayoutY(280);
        buttons.setPrefSize(Constants.WINDOW_WIDTH, 70);
        return buttons;
    }

    private Pane createBackgroundScreen(HBox buttons, String styleClass) {
        Pane root = new Pane(buttons);
        root.setPrefSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        root.getStyleClass().addAll("screen", styleClass);
        return root;
    }
}
