package com.bubble.dragon;

import com.bubble.dragon.util.Constants;
import com.bubble.dragon.view.GameOverView;
import com.bubble.dragon.view.GameView;
import com.bubble.dragon.view.HomeView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class BubbleDragonApp extends Application {
    private Stage stage;
    private GameView activeGame;

    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle(Constants.GAME_TITLE);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> stopGame());
        showHome();
        stage.show();
    }

    public void showHome() {
        stopGame();
        setScene(new HomeView(this).createScene());
    }

    public void startGame() {
        stopGame();
        activeGame = new GameView(this);
        setScene(activeGame.createScene());
        activeGame.start();
    }

    public void showResult(boolean victory) {
        stopGame();
        setScene(new GameOverView(this, victory).createScene());
    }

    private void setScene(Scene scene) {
        String css = getClass().getResource("/css/application.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void stopGame() {
        if (activeGame != null) {
            activeGame.stop();
            activeGame = null;
        }
    }
}
