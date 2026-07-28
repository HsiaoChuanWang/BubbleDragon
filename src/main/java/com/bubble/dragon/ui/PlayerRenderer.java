package com.bubble.dragon.ui;

import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

final class PlayerRenderer {
    private static final Image STANDING = ImageLoader.load("/images/stand.png");
    private static final Image LEFT_LEG = ImageLoader.load("/images/left_leg.png");
    private static final Image RIGHT_LEG = ImageLoader.load("/images/right_leg.png");
    private static final Image BLOW = ImageLoader.load("/images/blow.png");
    private static final Image[] WALK_IMAGES = { LEFT_LEG, STANDING, RIGHT_LEG, STANDING };

    private static final long WALK_FRAME_NANOS = 60_000_000; // 每張走路圖片顯示 6000 萬奈秒 = 0.06 秒
    private static final double CROP_X = 347; // 從原圖 X = 347 的位置開始裁切
    private static final double CROP_Y = 0; // 從原圖最上方 Y = 0 的位置開始裁切
    private static final double CROP_WIDTH = 741; // 從原圖裁切的寬度
    private static final double CROP_HEIGHT = 872; // 從原圖裁切的高度
    private static final double IMAGE_WIDTH = 100; // 玩家圖片在遊戲畫面上的顯示寬度
    private static final double IMAGE_HEIGHT = IMAGE_WIDTH * CROP_HEIGHT / CROP_WIDTH; // 依裁切比例計算顯示高度，避免圖片變形
    private static final double INVULNERABLE_OPACITY = .48; // 玩家處於無敵時間時顯示為 48% 不透明

    private long walkAnimationStart;
    private boolean wasWalking;

    void draw(GraphicsContext graphics, Player player, boolean shooting) {
        boolean walking = player.getState() == PlayerState.MOVING;
        long now = System.nanoTime();
        if (walking && !wasWalking)
            walkAnimationStart = now;
        wasWalking = walking;

        Image image = shooting ? BLOW : STANDING;
        if (walking && !shooting) {
            int frame = (int) ((now - walkAnimationStart) / WALK_FRAME_NANOS % WALK_IMAGES.length);
            image = WALK_IMAGES[frame];
        }

        double imageX = player.getX() + (player.getWidth() - IMAGE_WIDTH) / 2;
        double imageY = player.getY() + player.getHeight() - IMAGE_HEIGHT;
        graphics.save();
        graphics.setGlobalAlpha(player.isInvulnerable() ? INVULNERABLE_OPACITY : 1);

        if (player.isFacingRight()) {
            drawImage(graphics, image, imageX, imageY);
        } else {
            graphics.translate(imageX + IMAGE_WIDTH, 0);
            graphics.scale(-1, 1);
            drawImage(graphics, image, 0, imageY);
        }
        graphics.restore();
    }

    private void drawImage(GraphicsContext graphics, Image image, double x, double y) {
        graphics.drawImage(
                image,
                CROP_X,
                CROP_Y,
                CROP_WIDTH,
                CROP_HEIGHT,
                x,
                y,
                IMAGE_WIDTH,
                IMAGE_HEIGHT);
    }
}
