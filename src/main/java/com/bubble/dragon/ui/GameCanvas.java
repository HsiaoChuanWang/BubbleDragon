package com.bubble.dragon.ui;

import com.bubble.dragon.controller.GameController;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.util.Constants;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

// 控制遊戲畫面的繪製順序，簡單元素直接繪製，複雜實體交給專用 Renderer
public final class GameCanvas extends Canvas {
    // 背景圖片的上半部是第二關，下半部是第一關；轉場時會在兩者之間移動裁切位置
    private static final Image BACKGROUND_IMAGE = ImageLoader.load("/images/background.jpg");

    // 玩家、敵人與地磚的繪製細節分別交給專用 Renderer 處理
    private final PlayerRenderer playerRenderer = new PlayerRenderer();
    private final EnemyRenderer enemyRenderer = new EnemyRenderer();
    private final BossRenderer bossRenderer = new BossRenderer();
    private final TileRenderer tileRenderer = new TileRenderer();

    // 建立遊戲畫布；畫布高度扣除 HUD，避免遊戲畫面與下方資訊列重疊
    public GameCanvas() {
        super(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT);
    }

    /*
     * 依照 GameController 提供的最新遊戲狀態，重新繪製整個遊戲畫面
     * 繪製順序：背景 → 第一、二關地磚 → 敵人 → 泡泡 → 玩家 → 出口
     * 後畫的內容會蓋在先畫的內容上，因此半透明泡泡會顯示在敵人前方
     */
    public void render(GameController game) {
        // 取得 Canvas 的 2D 繪圖工具。
        GraphicsContext graphics = getGraphicsContext2D();

        // 取得目前畫布的實際寬度與高度，供背景縮放和關卡轉場使用
        double width = getWidth();
        double height = getHeight();

        // 背景會覆蓋整張畫布，也會清除上一幀的內容，避免移動物件留下殘影
        drawBackground(graphics, game.getLevelTransitionProgress(), width, height);

        // 將 0 ~ 1 的轉場進度換算成地圖需要垂直移動的像素距離
        double transitionOffset = game.getLevelTransitionProgress() * height;

        // 第一關從原位往上移出畫面
        tileRenderer.draw(graphics, game.getLevelOneTiles(), -transitionOffset);

        // 第二關一開始位於畫面下方，隨轉場進度往上移入畫面
        tileRenderer.draw(graphics, game.getLevelTwoTiles(), height - transitionOffset);

        // 先畫敵人，再畫半透明泡泡，讓受困敵人仍能從泡泡內看見
        enemyRenderer.drawAll(graphics, game.getEnemies());
        drawBubbles(graphics, game);

        // BossRenderer 同時負責 Boss 本體與敵方藍色泡泡，避免和玩家泡泡樣式混用
        bossRenderer.draw(graphics, game.getBoss(), game.getBossBubbles());

        // 玩家繪製在敵人和泡泡之上；isShooting() 用來選擇吹泡泡圖片
        playerRenderer.draw(graphics, game.getPlayer(), game.isShooting());

        // 出口最後繪製；尚未符合顯示條件時，drawDoor() 不會畫任何內容。
        drawDoor(graphics, game);
    }

    /*
     * 繪製目前關卡的背景
     * background.jpg 垂直放置了兩張等高的關卡背景：圖片上半部是第二關，下半部是第一關
     * transitionProgress 從 0 增加到 1 時，裁切位置會由下半部移到上半部
     */
    private void drawBackground(
            GraphicsContext graphics,
            double transitionProgress,
            double width,
            double height) {
        // 一個關卡背景的原圖高度是整張背景圖片的一半
        double stageHeight = BACKGROUND_IMAGE.getHeight() / 2;

        // 進度為 0 時從下半部開始裁切；進度為 1 時從上半部開始裁切
        double sourceY = stageHeight * (1 - transitionProgress);

        // 裁切一個關卡高度的背景，再縮放到整個遊戲畫布
        graphics.drawImage(
                // 背景原圖
                BACKGROUND_IMAGE,

                // 原圖裁切區域的起始 X
                0,

                // 原圖裁切區域的起始 Y，會依轉場進度向上移動
                sourceY,

                // 使用完整的原圖寬度
                BACKGROUND_IMAGE.getWidth(),

                // 每次裁切一個關卡背景的高度
                stageHeight,

                // 從畫布左上角 X = 0 開始繪製
                0,

                // 從畫布左上角 Y = 0 開始繪製
                0,

                // 將背景縮放成畫布寬度
                width,

                // 將背景縮放成畫布高度
                height);
    }

    // 逐一繪製目前泡泡清單中的物件；泡泡使用半透明填色，避免完全遮住內部的敵人
    private void drawBubbles(GraphicsContext graphics, GameController game) {
        for (Bubble bubble : game.getBubbles()) {
            // 設定半透明黃色並填滿泡泡內部
            graphics.setFill(Color.web("#ffd54f", .38));
            graphics.fillOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());

            // 設定較亮的半透明外框與 2px 線寬，再畫出泡泡輪廓
            graphics.setStroke(Color.web("#fff3a3", .85));
            graphics.setLineWidth(2);
            graphics.strokeOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
        }
    }

    // 所有敵人消滅且關卡轉場完成後，在指定位置繪製出口
    private void drawDoor(GraphicsContext graphics, GameController game) {
        // 出口尚未開啟時直接結束，不進行後續繪製。
        if (!game.isDoorVisible())
            return;

        // 繪製黃色圓角矩形作為門的主體
        graphics.setFill(Color.web("#ffd166"));
        graphics.fillRoundRect(
                game.getDoorX(),
                game.getDoorY(),
                Constants.DOOR_WIDTH,
                Constants.DOOR_HEIGHT,
                16,
                16);

        // 在門的右側繪製深色圓形門把
        graphics.setFill(Color.web("#604b2d"));
        graphics.fillOval(game.getDoorX() + 35, game.getDoorY() + 36, 6, 6);

        // 在門的上方顯示「出口」文字
        graphics.setFill(Color.WHITE);
        graphics.setFont(Font.font(18));
        graphics.fillText("出口", game.getDoorX() + 3, game.getDoorY() - 8);
    }
}
