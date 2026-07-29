package com.bubble.dragon.ui;

import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// 依敵人狀態、方向選擇、裁切，並繪製對應圖片
final class EnemyRenderer {
    private static final Image JUMP = ImageLoader.loadEnemy("/images/enemy-jump.png");
    private static final Image FLY = ImageLoader.loadEnemy("/images/enemy-fly.png");
    private static final Image STOP = ImageLoader.loadEnemy("/images/enemy-stop.png");
    private static final double IMAGE_HEIGHT = 112;

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

        // 設定原圖裁切區域的起始 X
        double sourceX = enemy.getState() == EnemyState.FLY ? 0 : 200;

        // 設定原圖裁切區域的起始 Y，略過圖片頂端 20px
        double sourceY = 20;

        // 設定原圖裁切寬度：飛行狀態保留整張圖的寬度與完整翅膀，其他狀態裁切 300px
        double sourceWidth = enemy.getState() == EnemyState.FLY ? image.getWidth() : 300;

        // 設定從原圖向下裁切的高度
        double sourceHeight = 350;

        // 按照裁切區域的寬高比例計算顯示寬度，避免圖片被水平拉伸或壓縮
        double imageWidth = IMAGE_HEIGHT * sourceWidth / sourceHeight;

        double imageX = enemy.getX() + (enemy.getWidth() - imageWidth) / 2;

        // 讓顯示圖片的底部對齊敵人碰撞範圍的底部
        double imageY = enemy.getY() + enemy.getHeight() - IMAGE_HEIGHT;

        // 保存目前的畫布座標與縮放狀態，避免圖片翻轉影響後續繪圖
        graphics.save();

        // 敵人面向右側時，將圖片沿著垂直軸水平翻轉
        if (enemy.getDirection() > 0) {
            // 將畫布原點移到圖片右側，作為水平翻轉後的定位基準
            graphics.translate(imageX + imageWidth, 0);

            // 將 X 軸縮放設為 -1，使圖片左右翻轉；Y 軸維持原方向
            graphics.scale(-1, 1);

            // 畫布已經移到圖片右側，因此翻轉後從新的 X = 0 開始繪製
            // 畫圖片之前，先把畫布的座標原點移到圖片右側，再反轉 X 軸，接著才將圖片畫上去
            imageX = 0;
        }

        // 從原始圖片裁切指定區域，再縮放並繪製到遊戲畫布上的目標區域。
        graphics.drawImage(
                image,
                sourceX,
                sourceY,
                sourceWidth,
                sourceHeight,
                imageX,
                imageY,
                imageWidth,
                IMAGE_HEIGHT);

        // 恢復 save() 前的畫布狀態，取消本次繪圖使用的位移與翻轉
        graphics.restore();
    }
}
