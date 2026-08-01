package com.bubble.dragon.map;

import java.util.ArrayList;
import java.util.List;

// 對應地圖 JSON 的資料物件，只負責儲存資料
// Jackson 是 Java 常用的 JSON 處理函式庫，負責在「JSON 資料」和「Java 物件」之間進行轉換
public final class GameMap {
    public double playerX = 80;
    public double playerY = 500;
    public List<String> tileTextures = new ArrayList<>();
    public List<TileData> tiles = new ArrayList<>();
    public List<EnemyData> enemies = new ArrayList<>();

    public static final class TileData {
        public double x, y, width;
        public double collisionOffsetY = 0;
        public boolean solid = true;
        public List<String> texturePattern = new ArrayList<>();

        // 供 Jackson 建立物件並填入 JSON 資料
        public TileData() {
        }
    }

    public static final class EnemyData {
        public double x, y;
        public int direction = 1;

        // 供 Jackson 建立物件並填入 JSON 資料
        public EnemyData() {
        }
    }
}
