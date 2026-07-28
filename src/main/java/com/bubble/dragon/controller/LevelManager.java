package com.bubble.dragon.controller;

import java.util.ArrayList;
import java.util.List;

import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.map.GameMap;
import com.bubble.dragon.map.LevelLoader;
import com.bubble.dragon.map.Tile;

/*
 * 管理遊戲中的關卡資料
 * 遊戲開始時載入第一關和第二關的地圖
 * 並保存目前關卡使用的地磚與敵人
 * 第一關完成後，負責將地磚和敵人切換成第二關的內容
*/
public final class LevelManager {
    private final GameMap levelOne;
    private final GameMap levelTwo;
    private final List<Tile> levelOneTiles = new ArrayList<>();
    private final List<Tile> levelTwoTiles = new ArrayList<>();
    private final List<Tile> activeTiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    public LevelManager() {
        LevelLoader loader = new LevelLoader();
        levelOne = loader.load("/maps/level1.json");
        levelTwo = loader.load("/maps/level2.json");
        addTiles(levelOne, levelOneTiles);
        addTiles(levelTwo, levelTwoTiles);
        activeTiles.addAll(levelOneTiles);
        addEnemies(levelOne);
    }

    private void addTiles(GameMap map, List<Tile> destination) {
        map.tiles.forEach(data -> {
            List<String> pattern = data.texturePattern.isEmpty()
                    ? map.tileTextures
                    : data.texturePattern;
            destination.add(new Tile(data.x, data.y, data.width, data.solid, pattern));
        });
    }

    private void addEnemies(GameMap map) {
        map.enemies.forEach(data -> enemies.add(new Enemy(data.x, data.y, data.direction)));
    }

    public void switchToLevelTwo() {
        activeTiles.clear();
        activeTiles.addAll(levelTwoTiles);
        enemies.clear();
        addEnemies(levelTwo);
    }

    public double getInitialPlayerX() {
        return levelOne.playerX;
    }

    public double getInitialPlayerY() {
        return levelOne.playerY;
    }

    public List<Tile> getActiveTiles() {
        return activeTiles;
    }

    public List<Tile> getLevelOneTiles() {
        return levelOneTiles;
    }

    public List<Tile> getLevelTwoTiles() {
        return levelTwoTiles;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
