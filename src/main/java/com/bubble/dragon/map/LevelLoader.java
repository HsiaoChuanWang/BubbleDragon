package com.bubble.dragon.map;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

// 從 classpath 的 resources 讀取 JSON，並轉換成 GameMap
public final class LevelLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public GameMap load(String resourcePath) {
        // 使用 getResourceAsStream，打包成 JAR 後仍可讀取 (因為不是讀取檔案，是讀取資料流)，不依賴工作目錄
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            if (input == null)
                throw new IllegalArgumentException("找不到關卡：" + resourcePath);

            return mapper.readValue(input, GameMap.class); // JSON 轉 GameMap
        } catch (IOException exception) {
            throw new IllegalStateException("無法讀取關卡：" + resourcePath, exception);
        }
    }
}
