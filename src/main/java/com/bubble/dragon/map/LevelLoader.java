package com.bubble.dragon.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;

public final class LevelLoader {
    private final ObjectMapper mapper = new ObjectMapper();

    public GameMap load(String resourcePath) {
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            if (input == null) throw new IllegalArgumentException("找不到關卡：" + resourcePath);
            return mapper.readValue(input, GameMap.class);
        } catch (IOException exception) {
            throw new IllegalStateException("無法讀取關卡：" + resourcePath, exception);
        }
    }
}
