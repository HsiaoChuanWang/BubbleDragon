package com.bubble.dragon.map;

import java.util.ArrayList;
import java.util.List;

public final class GameMap {
    public double playerX = 80;
    public double playerY = 500;
    public List<TileData> tiles = new ArrayList<>();
    public List<EnemyData> enemies = new ArrayList<>();

    public static final class TileData {
        public double x, y, width, height;
        public boolean solid = true;
        public TileData() {}
    }
    public static final class EnemyData {
        public double x, y;
        public int direction = 1;
        public EnemyData() {}
    }
}
