package com.bubble.dragon.ui;

import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

final class EnemyRenderer {

    // 預先載入敵人各狀態的已裁切透明圖片
    private static final Image JUMP = ImageLoader.load("/images/enemy-jump.png");
    private static final Image FLY = ImageLoader.load("/images/enemy-fly.png");
    private static final Image STOP = ImageLoader.load("/images/enemy-stop.png");
    private static final double IMAGE_HEIGHT = 112; // 敵人在畫面上的固定顯示高度

    void drawAll(GraphicsContext graphics, Iterable<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (enemy.getState() != EnemyState.DEFEATED)
                draw(graphics, enemy);
        }
    }

    private void draw(GraphicsContext graphics, Enemy enemy) {
        Image image = switch (enemy.getState()) {
            case FLY -> FLY;
            case STOP -> STOP;
            default -> JUMP;
        };

        // 直接顯示完整圖片，並依原圖長寬比計算寬度
        double imageWidth = IMAGE_HEIGHT * image.getWidth() / image.getHeight();
        // 圖片水平居中於敵人碰撞箱，底部與碰撞箱底部對齊
        double imageX = enemy.getX() + (enemy.getWidth() - imageWidth) / 2;
        double imageY = enemy.getY() + enemy.getHeight() - IMAGE_HEIGHT;

        graphics.save();
        if (enemy.getDirection() > 0) {
            // 向右時水平鏡像圖片
            graphics.translate(imageX + imageWidth, 0);
            graphics.scale(-1, 1);
            imageX = 0;
        }
        graphics.drawImage(image, imageX, imageY, imageWidth, IMAGE_HEIGHT);
        graphics.restore();
    }
}
