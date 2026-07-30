package com.bubble.dragon;

import com.bubble.dragon.util.Constants;
import com.bubble.dragon.view.GameOverView;
import com.bubble.dragon.view.GameView;
import com.bubble.dragon.view.HomeView;
import com.bubble.dragon.view.StoryView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// @JavaFX 應用程式
// 集中管理唯一的主視窗與各 Scene 的切換
public final class BubbleDragonApp extends Application {
    // JavaFX 提供一 視窗 Stage
    private Stage stage;
    private GameView activeGame;
    private StoryView activeStory;

    public static void launchApp(String[] args) {
        launch(args);
    }

    // 初始化 Stage 與 Scene，並顯示首頁
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage; // 由 JavaFX 接管視窗建立
        stage.setTitle(Constants.GAME_TITLE); // 設定視窗上方的標題
        stage.setResizable(false); // 固定視窗大小，不能隨意拉大縮小
        stage.setOnCloseRequest(event -> stopGame()); // 右上角 X 關閉觸發
        showHome(); // 預設先顯示「首頁」
        stage.show(); // 正式把視窗顯示在螢幕上
    }

    public void showHome() {
        stopStory();
        stopGame();
        setScene(new HomeView(this).createScene());
    }

    public void showStory() {
        stopGame();
        stopStory();
        activeStory = new StoryView(this);
        setScene(activeStory.createScene());
        activeStory.play();
    }

    // GameView 會有 60 FPS 的 Game Loop，必須在切換畫面時停止，否則會持續消耗 CPU
    public void startGame() {
        stopStory();
        stopGame();
        activeGame = new GameView(this);
        setScene(activeGame.createScene());
        activeGame.start();
    }

    public void showResult(boolean victory) {
        stopStory();
        stopGame();
        setScene(new GameOverView(this, victory).createScene());
    }

    private void setScene(Scene scene) {
        // 所有畫面共用同一份 CSS，避免每個 View 重複設定樣式
        String css = getClass().getResource("/css/application.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setScene(scene);
        stage.centerOnScreen(); // 讓視窗自動對齊螢幕正中間
    }

    private void stopGame() {
        if (activeGame != null) {
            activeGame.stop();
            activeGame = null;
        }
    }

    private void stopStory() {
        if (activeStory != null) {
            activeStory.stop();
            activeStory = null;
        }
    }
}
