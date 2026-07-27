package com.bubble.dragon.ui;

import com.bubble.dragon.controller.GameController;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.util.Constants;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class GameCanvas extends Canvas {
    private static final Image PLAYER_STANDING_IMAGE = new Image(
            GameCanvas.class.getResource("/images/stand.png").toExternalForm());
    private static final Image PLAYER_LEFT_LEG_IMAGE = new Image(
            GameCanvas.class.getResource("/images/left_leg.png").toExternalForm());
    private static final Image PLAYER_RIGHT_LEG_IMAGE = new Image(
            GameCanvas.class.getResource("/images/right_leg.png").toExternalForm());
    private static final Image PLAYER_BLOW_IMAGE = new Image(
            GameCanvas.class.getResource("/images/blow.png").toExternalForm());
    private static final Image[] PLAYER_WALK_IMAGES = {
            PLAYER_LEFT_LEG_IMAGE,
            PLAYER_STANDING_IMAGE,
            PLAYER_RIGHT_LEG_IMAGE,
            PLAYER_STANDING_IMAGE
    };
    private static final long WALK_FRAME_NANOS = 60_000_000;

    // 從原圖的哪個位置開始裁切，以及要保留的範圍。
    private static final double PLAYER_IMAGE_CROP_X = 347;
    private static final double PLAYER_IMAGE_CROP_Y = 0;
    private static final double PLAYER_IMAGE_CROP_WIDTH = 741;
    private static final double PLAYER_IMAGE_CROP_HEIGHT = 872;

    // 遊戲中的顯示寬度；高度會依裁切範圍等比例計算。
    private static final double PLAYER_IMAGE_WIDTH = 100;
    private static final double PLAYER_IMAGE_HEIGHT = PLAYER_IMAGE_WIDTH
            * PLAYER_IMAGE_CROP_HEIGHT
            / PLAYER_IMAGE_CROP_WIDTH;
    private static final double PLAYER_INVULNERABLE_OPACITY = 0.48;

    private long walkAnimationStart;
    private boolean wasWalking;

    // 畫布大小
    public GameCanvas() {
        super(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT);
    }

    public void render(GameController game) {
        // 取得 Canvas 的繪圖工具（GraphicsContext）
        GraphicsContext g = getGraphicsContext2D();

        double w = getWidth();
        double h = getHeight();

        // 畫面重置，覆蓋上一幀的畫面，避免角色移動產生殘影 (Ghosting)
        g.setFill(Color.web("#102447"));
        g.fillRect(0, 0, w, h);
        g.setFill(Color.web("#17365f"));
        for (int i = 0; i < 18; i++)
            g.fillOval(i * 67 % (int) w, 40 + i * 83 % (int) h, 4, 4);

        // 繪製順序：背景 → 地圖 → 泡泡 → 敵人 → 玩家 → 出口。
        for (Tile tile : game.getTiles()) {
            g.setFill(Color.web("#3c7a57"));
            g.fillRoundRect(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(), 10, 10);
            g.setStroke(Color.web("#79c267"));
            g.strokeRoundRect(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(), 10, 10);
        }

        // 先畫敵人，再覆蓋半透明泡泡，讓受困敵人仍能從泡泡內隱約看見。
        for (Enemy enemy : game.getEnemies()) {
            if (enemy.getState() == EnemyState.DEFEATED)
                continue;
            g.setFill(Color.web("#ff6b6b"));
            g.fillRoundRect(enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), 14, 14);
            drawEyes(g, enemy.getX(), enemy.getY(), enemy.getWidth());
        }

        // 使用半透明黃色填色與亮色外框，保留泡泡內部的可見度。
        for (Bubble bubble : game.getBubbles()) {
            g.setFill(Color.web("#ffd54f", .38));
            g.fillOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
            g.setStroke(Color.web("#fff3a3", .85));
            g.setLineWidth(2);
            g.strokeOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
        }

        Player p = game.getPlayer();
        drawPlayer(g, p, game.isShooting());

        if (game.isDoorVisible()) {
            g.setFill(Color.web("#ffd166"));
            g.fillRoundRect(
                    game.getDoorX(),
                    game.getDoorY(),
                    Constants.DOOR_WIDTH,
                    Constants.DOOR_HEIGHT,
                    16,
                    16);
            g.setFill(Color.web("#604b2d"));
            g.fillOval(game.getDoorX() + 35, game.getDoorY() + 36, 6, 6);
            g.setFill(Color.WHITE);
            g.setFont(Font.font(18));
            g.fillText("出口", game.getDoorX() + 3, game.getDoorY() - 8);
        }
    }

    private void drawPlayer(GraphicsContext g, Player player, boolean shooting) {
        // 玩家剛開始走路時記錄目前時間，讓走路動畫從第一張圖開始播放。
        boolean walking = player.getState() == PlayerState.MOVING;
        long now = System.nanoTime();
        if (walking && !wasWalking)
            walkAnimationStart = now;
        wasWalking = walking;

        Image playerImage = shooting ? PLAYER_BLOW_IMAGE : PLAYER_STANDING_IMAGE;
        if (walking && !shooting) {
            int frameIndex = (int) ((now - walkAnimationStart) / WALK_FRAME_NANOS
                    % PLAYER_WALK_IMAGES.length);
            playerImage = PLAYER_WALK_IMAGES[frameIndex];
        }

        // 保存目前的透明度與座標方向。
        // 畫玩家時會修改透明度，面向左邊時還會翻轉座標；
        // 畫完後可用 restore() 恢復，避免影響後面的出口等圖案。
        g.save();

        // 玩家在無敵時間內顯示為 48% 不透明，其他時候保持完全不透明。
        g.setGlobalAlpha(player.isInvulnerable() ? PLAYER_INVULNERABLE_OPACITY : 1);

        // 玩家位置與寬高形成一個隱形矩形，遊戲用它判斷地面、牆壁和敵人碰撞。
        // 角色圖片可能比這個矩形大，因此將圖片水平置中，並讓圖片底部對齊矩形底部，
        // 使圖片中的腳和遊戲判定的站立位置一致。
        double playerImageX = player.getX() + (player.getWidth() - PLAYER_IMAGE_WIDTH) / 2;
        double playerImageY = player.getY() + player.getHeight() - PLAYER_IMAGE_HEIGHT;

        // 原圖面向右邊；玩家面向左邊時將圖片水平翻轉。
        if (player.isFacingRight()) {
            g.drawImage(
                    playerImage,
                    PLAYER_IMAGE_CROP_X,
                    PLAYER_IMAGE_CROP_Y,
                    PLAYER_IMAGE_CROP_WIDTH,
                    PLAYER_IMAGE_CROP_HEIGHT,
                    playerImageX,
                    playerImageY,
                    PLAYER_IMAGE_WIDTH,
                    PLAYER_IMAGE_HEIGHT);
        } else {
            g.translate(playerImageX + PLAYER_IMAGE_WIDTH, 0);
            g.scale(-1, 1);
            g.drawImage(
                    playerImage,
                    PLAYER_IMAGE_CROP_X,
                    PLAYER_IMAGE_CROP_Y,
                    PLAYER_IMAGE_CROP_WIDTH,
                    PLAYER_IMAGE_CROP_HEIGHT,
                    0,
                    playerImageY,
                    PLAYER_IMAGE_WIDTH,
                    PLAYER_IMAGE_HEIGHT);
        }
        // 恢復透明度和座標方向，避免影響後面繪製的內容。
        g.restore();
    }

    private void drawEyes(GraphicsContext g, double x, double y, double width) {
        g.setFill(Color.WHITE);
        g.fillOval(x + 9, y + 10, 8, 11);
        g.fillOval(x + width - 17, y + 10, 8, 11);
        g.setFill(Color.web("#17223b"));
        g.fillOval(x + 12, y + 14, 4, 5);
        g.fillOval(x + width - 14, y + 14, 4, 5);
    }
}
