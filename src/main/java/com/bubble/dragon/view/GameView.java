package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.controller.GameController;
import com.bubble.dragon.game.GameLoop;
import com.bubble.dragon.ui.GameCanvas;
import com.bubble.dragon.ui.InfoPanel;
import com.bubble.dragon.util.Constants;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

public final class GameView {
    private final BubbleDragonApp app;
    private final GameController controller;
    private final GameCanvas canvas = new GameCanvas();
    private final InfoPanel info = new InfoPanel();
    private final GameLoop loop;

    public GameView(BubbleDragonApp app) {
        this.app = app;
        controller = new GameController(app::showResult);
        loop = new GameLoop(this::frame);
    }

    public Scene createScene() {
        BorderPane root = new BorderPane(canvas, info, null, null, null); root.getStyleClass().add("game-screen");
        Scene scene = new Scene(root, Constants.WIDTH, Constants.HEIGHT);
        scene.setOnKeyPressed(event -> { if (event.getCode() == KeyCode.ESCAPE) app.showHome(); else controller.press(event.getCode()); });
        scene.setOnKeyReleased(event -> controller.release(event.getCode()));
        return scene;
    }

    public void start() { info.update(controller.getPlayer().getHp(), controller.getActiveEnemyCount()); canvas.render(controller); loop.start(); }
    public void stop() { loop.stop(); }
    private void frame(double dt) { controller.update(dt); info.update(controller.getPlayer().getHp(), controller.getActiveEnemyCount()); canvas.render(controller); }
}
