package com.bubble.dragon.ui;

import com.bubble.dragon.entity.boss.Boss;
import com.bubble.dragon.entity.boss.BossBubble;
import com.bubble.dragon.entity.boss.BossState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// 負責繪製 Boss 本體與 Boss 發射的藍色泡泡
final class BossRenderer {
    // 直接載入 Boss 各狀態的已裁切透明圖片
    private static final Image SCREAM = ImageLoader.load("/images/boss-scream.png");
    private static final Image FOLD_WINGS = ImageLoader.load("/images/boss-fold-wings.png");
    private static final Image DIVING_ATTACK = ImageLoader.load("/images/boss-diving-attack.png");
    private static final Image STAND = ImageLoader.load("/images/boss-stand.png");
    private static final Image LEFT_LEG = ImageLoader.load("/images/boss-left-leg.png");
    private static final Image RIGHT_LEG = ImageLoader.load("/images/boss-right-leg.png");
    private static final Image[] WALK_IMAGES = { LEFT_LEG, STAND, RIGHT_LEG, STAND };

    private static final double NORMAL_IMAGE_HEIGHT = 145; // 一般走路時與 Enemy 高度一致
    private static final double CORNER_ATTACK_IMAGE_HEIGHT = NORMAL_IMAGE_HEIGHT * 2.5; // 四角吐泡泡時放大三倍
    private static final double DIVING_ATTACK_IMAGE_WIDTH = 350; // 對角線飛行時固定寬度
    private static final long WALK_FRAME_NANOS = 120_000_000; // 走路動畫每幀顯示 0.12 秒

    private long walkAnimationStart;
    private boolean wasWalking;

    void draw(GraphicsContext graphics, Boss boss, Iterable<BossBubble> bubbles) {
        if (!boss.isActive() || boss.isDefeated())
            return;

        drawBoss(graphics, boss);
        for (BossBubble bubble : bubbles)
            drawBubble(graphics, bubble);
    }

    private void drawBoss(GraphicsContext graphics, Boss boss) {
        // 登場動畫使用 2.5 倍大小；俯衝圖片則在碰撞箱中央顯示
        boolean screaming = boss.getState() == BossState.SCREAM;
        boolean intro = screaming || boss.getState() == BossState.FOLD_WINGS;
        boolean diving = boss.getState() == BossState.DIVING_ATTACK;
        boolean walking = boss.getState() == BossState.PATROL && boss.getVelocityX() != 0;

        long now = System.nanoTime();
        if (walking && !wasWalking)
            walkAnimationStart = now;
        wasWalking = walking;

        Image image = switch (boss.getState()) {
            case SCREAM -> SCREAM;
            case FOLD_WINGS -> FOLD_WINGS;
            case DIVING_ATTACK -> DIVING_ATTACK;
            default -> STAND;
        };
        if (walking) {
            // 在左腳、站立、右腳、站立圖片之間循環，模擬走路動作
            int frame = (int) ((now - walkAnimationStart) / WALK_FRAME_NANOS % WALK_IMAGES.length);
            image = WALK_IMAGES[frame];
        }

        // 走路與四角攻擊以高度為基準；對角線飛行則以寬度為基準
        double imageWidth;
        double imageHeight;
        if (diving) {
            imageWidth = DIVING_ATTACK_IMAGE_WIDTH;
            imageHeight = imageWidth * image.getHeight() / image.getWidth();
        } else {
            imageHeight = intro ? CORNER_ATTACK_IMAGE_HEIGHT : NORMAL_IMAGE_HEIGHT;
            imageWidth = imageHeight * image.getWidth() / image.getHeight();
        }
        double imageX = boss.getX() + (boss.getWidth() - imageWidth) / 2;
        double imageY = intro || diving
                ? boss.getY() + (boss.getHeight() - imageHeight) / 2
                : boss.getY() + boss.getHeight() - imageHeight;

        graphics.save();
        // Boss 無敵時以半透明圖片提示玩家
        graphics.setGlobalAlpha(boss.isInvulnerable() ? .45 : 1);

        // 原圖面向左，Boss 向右時水平鏡像圖片
        if (!intro && !diving && boss.getDirection() > 0) {
            graphics.translate(imageX + imageWidth, 0);
            graphics.scale(-1, 1);
            imageX = 0;
        }
        graphics.drawImage(image, imageX, imageY, imageWidth, imageHeight);
        graphics.restore();
    }

    private void drawBubble(GraphicsContext graphics, BossBubble bubble) {
        graphics.setFill(Color.web("#48c8ff", .42));
        graphics.fillOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
        graphics.setStroke(Color.web("#b9efff", .9));
        graphics.setLineWidth(2);
        graphics.strokeOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
    }
}
