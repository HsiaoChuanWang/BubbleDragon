package com.bubble.dragon.view;

import com.bubble.dragon.BubbleDragonApp;
import com.bubble.dragon.controller.GameController;
import com.bubble.dragon.game.GameLoop;
import com.bubble.dragon.ui.GameCanvas;
import com.bubble.dragon.ui.HUD;
import com.bubble.dragon.util.Constants;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

public final class GameView {
    private final BubbleDragonApp app;

    // 負責接收鍵盤狀態、更新所有實體、處理物理與碰撞，並判斷勝敗
    private final GameController controller;

    // 藉由取得當下的位置刷新 UI，只讀取 GameController 狀態，不修改邏輯
    private final GameCanvas canvas = new GameCanvas();

    // 負責顯示玩家 HP、剩餘敵人數量及按鍵操作提示
    private final HUD info = new HUD();

    // 遊戲的主迴圈
    // 內部使用 JavaFX AnimationTimer，不斷呼叫 frame()
    // 讓遊戲持續更新角色狀態、處理碰撞、重新繪製畫面
    // 這個欄位會在建構子中建立，所以此處沒有直接使用 new
    private final GameLoop loop;

    public GameView(BubbleDragonApp app) {
        this.app = app;
        controller = new GameController(app::showResult); // 等同於 (score) -> app.showResult(score)
        loop = new GameLoop(this::frame); // 等同於 (dt -> this.frame(dt));
    }

    public Scene createScene() {
        BorderPane root = new BorderPane(canvas, info, null, null, null);
        root.getStyleClass().add("game-screen");
        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // Controller 保存按鍵集合，因此長按方向鍵時可在每一幀持續移動。
        // 按下按鍵 時執行 setOnKeyPressed
        // 按其他按鍵就只是把它keep下來
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE)
                app.showHome();
            else
                controller.press(event.getCode());
        });

        // 放開按鍵時，通知 Controller 這個按鍵已經不再被按下
        scene.setOnKeyReleased(event -> controller.release(event.getCode()));
        return scene;
    }

    public void start() {
        // 立即顯示玩家生命值與敵人數量
        info.update(controller.getPlayer().getHp(), controller.getActiveEnemyCount());

        // 立即把玩家、敵人、地圖和泡泡等目前狀態畫到 Canvas
        canvas.render(controller);

        // 開始每幀呼叫 frame()，讓遊戲持續更新
        loop.start();
    }

    public void stop() {
        loop.stop();
    }

    // frame 定義「每一幀要做什麼」
    private void frame(double dt) {
        // 每次準備畫出新畫面前，先計算遊戲中的物件現在應該變成什麼狀態
        controller.update(dt);

        info.update(controller.getPlayer().getHp(), controller.getActiveEnemyCount());
        canvas.render(controller);
    }
}
