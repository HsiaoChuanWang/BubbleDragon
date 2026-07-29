package com.bubble.dragon.ui;

import com.bubble.dragon.entity.boss.Boss;
import com.bubble.dragon.entity.boss.BossBubble;
import com.bubble.dragon.entity.boss.BossState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

// 依 Boss 狀態選圖，並繪製 Boss 專用的藍色半透明攻擊泡泡
final class BossRenderer {
    private static final Image SCREAM = ImageLoader.loadEnemy("/images/boss-scream.jpg");
    private static final Image FOLD_WINGS = ImageLoader.loadEnemy("/images/boss-fold-wings.jpg");
    private static final Image DIVING_ATTACK = ImageLoader.loadEnemy("/images/boss-diving-attack.png");
    private static final Image STAND = ImageLoader.loadEnemy("/images/boss-stand.jpg");

    // 與 EnemyRenderer 的一般顯示高度一致；登場狀態再放大三倍
    private static final double NORMAL_IMAGE_HEIGHT = 112;

    void draw(GraphicsContext graphics, Boss boss, Iterable<BossBubble> bubbles) {
        if (!boss.isActive() || boss.isDefeated())
            return;

        drawBoss(graphics, boss);
        for (BossBubble bubble : bubbles)
            drawBubble(graphics, bubble);
    }

    private void drawBoss(GraphicsContext graphics, Boss boss) {
        // intro 狀態需置中且放大
        // diving 只需置中，不放大
        boolean screaming = boss.getState() == BossState.SCREAM;
        boolean intro = screaming || boss.getState() == BossState.FOLD_WINGS;
        boolean diving = boss.getState() == BossState.DIVING_ATTACK;
        Image image = switch (boss.getState()) {
            case SCREAM -> SCREAM;
            case FOLD_WINGS -> FOLD_WINGS;
            case DIVING_ATTACK -> DIVING_ATTACK;
            default -> STAND;
        };

        double imageHeight = NORMAL_IMAGE_HEIGHT * (intro ? 3 : 1);

        // 依原圖比例反推寬度，避免不同動作圖片被壓扁或拉長。
        double imageWidth = imageHeight * image.getWidth() / image.getHeight();
        double imageX = boss.getX() + (boss.getWidth() - imageWidth) / 2;

        // 登場與俯衝以碰撞箱中心對齊；巡邏則以腳底貼齊平台
        double imageY = intro || diving
                ? boss.getY() + (boss.getHeight() - imageHeight) / 2
                : boss.getY() + boss.getHeight() - imageHeight;

        graphics.save();

        // 受傷無敵期間用半透明提供清楚的視覺回饋
        graphics.setGlobalAlpha(boss.isInvulnerable() ? .45 : 1);

        // 只翻轉巡邏圖；登場及俯衝圖片保持素材原本方向
        if (!intro && !diving && boss.getDirection() > 0) {
            graphics.translate(imageX + imageWidth, 0);
            graphics.scale(-1, 1);
            imageX = 0;
        }
        graphics.drawImage(image, imageX, imageY, imageWidth, imageHeight);
        graphics.restore();
    }

    private void drawBubble(GraphicsContext graphics, BossBubble bubble) {
        // 填色保留透明度，外框提高泡泡在深色背景上的辨識度
        graphics.setFill(Color.web("#48c8ff", .42));
        graphics.fillOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
        graphics.setStroke(Color.web("#b9efff", .9));
        graphics.setLineWidth(2);
        graphics.strokeOval(bubble.getX(), bubble.getY(), bubble.getWidth(), bubble.getHeight());
    }
}
