package com.bubble.dragon.ui;

import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

final class PlayerRenderer {

    // 預先載入玩家各動作的已裁切透明圖片
    private static final Image STANDING = ImageLoader.load("/images/player-stand.png");
    private static final Image LEFT_LEG = ImageLoader.load("/images/player-left-leg.png");
    private static final Image RIGHT_LEG = ImageLoader.load("/images/player-right-leg.png");
    private static final Image BLOW = ImageLoader.load("/images/player-blow.png");
    private static final Image[] WALK_IMAGES = { LEFT_LEG, STANDING, RIGHT_LEG, STANDING };

    private static final long WALK_FRAME_NANOS = 60_000_000; // 走路動畫每幀顯示 0.06 秒
    private static final double IMAGE_WIDTH = 100; // 玩家圖片在畫面上的顯示寬度
    private static final double INVULNERABLE_OPACITY = .48; // 玩家無敵時的圖片不透明度

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
            // 依經過時間在走路圖片中循環切換
            int frame = (int) ((now - walkAnimationStart) / WALK_FRAME_NANOS % WALK_IMAGES.length);
            image = WALK_IMAGES[frame];
        }

        // 直接顯示完整圖片，並依原圖長寬比計算高度
        double imageHeight = IMAGE_WIDTH * image.getHeight() / image.getWidth();
        // 圖片水平居中於玩家碰撞箱，底部與碰撞箱底部對齊
        double imageX = player.getX() + (player.getWidth() - IMAGE_WIDTH) / 2;
        double imageY = player.getY() + player.getHeight() - imageHeight;

        graphics.save();
        graphics.setGlobalAlpha(player.isInvulnerable() ? INVULNERABLE_OPACITY : 1);
        if (!player.isFacingRight()) {
            graphics.drawImage(image, imageX, imageY, IMAGE_WIDTH, imageHeight);
        } else {
            // 原圖面向左，玩家向右時水平鏡像圖片
            graphics.translate(imageX + IMAGE_WIDTH, 0);
            graphics.scale(-1, 1);
            graphics.drawImage(image, 0, imageY, IMAGE_WIDTH, imageHeight);
        }
        graphics.restore();
    }
}
