package com.bubble.dragon.ui;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// 集中載入與快取圖片，並提供敵人素材的白底轉透明處理
final class ImageLoader {
    // 以資源路徑作為 key 保存載入完成的圖片，避免同一張圖片被重複讀取與處理
    private static final Map<String, Image> CACHE = new HashMap<>();

    // 工具類別只提供 static 方法，不需要建立 ImageLoader 物件，因此將建構子設為 private
    private ImageLoader() {
    }

    // 載入一般圖片；若快取已有相同路徑，就直接回傳原本的 Image
    static Image load(String resourcePath) {
        // computeIfAbsent 只會在 key 不存在時呼叫 read()，並把載入結果放入快取
        return CACHE.computeIfAbsent(resourcePath, ImageLoader::read);
    }

    // 載入敵人圖片；第一次載入時會額外移除白色背景
    static Image loadEnemy(String resourcePath) {
        // key 加上 enemy: 前綴，避免處理過的敵人圖片與相同路徑的一般圖片使用同一個快取項目
        return CACHE.computeIfAbsent("enemy:" + resourcePath, ignored -> removeWhiteBackground(resourcePath));
    }

    // 從 resources 讀取一般圖片，不進行縮放或背景透明化
    private static Image read(String resourcePath) {
        // getResource() 找到 classpath 內的資源，再轉成 JavaFX Image 可讀取的 URL
        return new Image(ImageLoader.class.getResource(resourcePath).toExternalForm());
    }

    // 載入敵人圖片，逐一檢查像素，將接近白色的背景改成透明
    private static Image removeWhiteBackground(String resourcePath) {
        // 讀取敵人原圖，並縮放成指定的 682 × 384
        Image source = new Image(
                // 將 resources 中的圖片路徑轉成 JavaFX Image 可讀取的 URL
                ImageLoader.class.getResource(resourcePath).toExternalForm(),

                // 載入後的要求寬度
                682,

                // 載入後的要求高度
                384,

                // false 表示不保持原圖比例，圖片會配合指定寬高縮放
                false,

                // true 表示縮放時使用較平滑的影像過濾
                true);

        // 建立一張相同大小、允許修改像素的新圖片，用來保存透明化結果
        WritableImage result = new WritableImage((int) source.getWidth(), (int) source.getHeight());

        // PixelReader 負責讀取來源圖片每個座標的顏色
        PixelReader reader = source.getPixelReader();

        // PixelWriter 負責將處理後的顏色寫入結果圖片
        PixelWriter writer = result.getPixelWriter();

        // 從圖片最上方到最下方，逐列檢查每個 Y 座標
        for (int y = 0; y < source.getHeight(); y++) {
            // 從圖片最左側到最右側，逐一檢查該列的每個 X 座標
            for (int x = 0; x < source.getWidth(); x++) {
                // 讀取來源圖片目前 (x, y) 像素的顏色
                Color color = reader.getColor(x, y);

                // RGB 三個色彩分量都高於 0.92 時，將該像素判定為接近白色的背景
                boolean whiteBackground = color.getRed() > .92
                        && color.getGreen() > .92
                        && color.getBlue() > .92;

                // 白色背景寫成完全透明；其他顏色則保留原本的顏色與透明度
                writer.setColor(x, y, whiteBackground ? Color.TRANSPARENT : color);
            }
        }

        // 回傳完成白底透明化的新圖片
        return result;
    }
}
