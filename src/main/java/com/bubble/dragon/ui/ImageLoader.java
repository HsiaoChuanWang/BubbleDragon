package com.bubble.dragon.ui;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

// 統一負責從 resources 載入並快取圖片
final class ImageLoader {
    // 避免每個畫格重複讀取同一個圖片資源
    private static final Map<String, Image> CACHE = new HashMap<>();

    // 工具類只提供靜態方法，不允許建立實體
    private ImageLoader() {
    }

    // 直接載入已裁切並去背的完整圖片，不再進行像素處理
    static Image load(String resourcePath) {
        return CACHE.computeIfAbsent(resourcePath, ImageLoader::read);
    }

    // 將 resources 中的圖片路徑轉成 JavaFX Image 可讀取的 URL
    private static Image read(String resourcePath) {
        return new Image(ImageLoader.class.getResource(resourcePath).toExternalForm());
    }
}
