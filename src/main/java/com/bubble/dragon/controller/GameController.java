package com.bubble.dragon.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.bubble.dragon.entity.GameObject;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.GameMap;
import com.bubble.dragon.map.LevelLoader;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

import javafx.scene.input.KeyCode;

// 負責接收鍵盤狀態、更新所有實體、處理物理與碰撞，並判斷勝敗
// 管理遊戲規則與每一幀「要做什麼」
public final class GameController {
    private final Player player;
    private final List<Tile> tiles = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bubble> bubbles = new ArrayList<>();

    // 使用 Set 保存「目前仍按住」的鍵，偵測即時遊戲的連續輸入
    private final Set<KeyCode> keys = new HashSet<>();
    
    // 勝敗發生時由 BubbleDragonApp 傳入的 callback 負責切換 Scene
    // 遊戲結束時要呼叫的函式
    // Consumer: Java 內建的「接收一個值、不回傳值」的函式 Interface，這裡用來傳遞勝敗結果
    private final Consumer<Boolean> resultHandler;

    private double shootCooldown;// 什麼時候能再射擊
    private boolean jumpHeld; // 持續按住跳躍
    private boolean finished; // 遊戲結束後不再更新，避免重複呼叫 resultHandler
    private boolean doorVisible; // 所有敵人都被消滅後才會顯示出口，玩家碰到出口就勝利

    // 創建 GameController 時只需做一次: 初始化遊戲狀態，載入地圖、玩家、敵人與磚塊
    public GameController(Consumer<Boolean> resultHandler) {
        this.resultHandler = resultHandler;

        GameMap map = new LevelLoader().load("/maps/level1.json");
        player = new Player(map.playerX, map.playerY);
        map.tiles.forEach(t -> tiles.add(new Tile(t.x, t.y, t.width, t.height, t.solid)));
        map.enemies.forEach(e -> enemies.add(new Enemy(e.x, e.y, e.direction)));
    }

    public void press(KeyCode key) { keys.add(key); }
    public void release(KeyCode key) { keys.remove(key); }

    // delta time (時間差) = 代表「上一幀到這一幀經過了多少秒」
    // update(dt) 統一更新
    //   ├─ 更新計時器
    //   ├─ 更新玩家
    //   ├─ 更新敵人
    //   ├─ 更新泡泡
    //   ├─ 檢查碰撞
    //   ├─ 清除失效物件
    //   └─ 判斷勝敗
    public void update(double dt) {
        if (finished) return;
        
        shootCooldown = Math.max(0, shootCooldown - dt); // 阻止連續射擊泡泡
        player.updateInvulnerability(dt); // 無敵時間倒數
        updatePlayer(dt); 
        updateEnemies(dt); 
        updateBubbles(dt); 
        checkContacts();

        // removeIf(...) 是 Java 內建 Collection 介面的方法
        // stream: 把 Collection 變成一個 資料流（Stream） 方便做filter, allMatch
        bubbles.removeIf(b -> !b.isActive());
        doorVisible = enemies.stream().allMatch(e -> e.getState() == EnemyState.DEFEATED);
        if (player.getHp() <= 0) finish(false);

        // OverlapChecker.overlaps: 去檢查玩家是否碰到門
        if (doorVisible && OverlapChecker.overlaps(
            player.getX(), 
            player.getY(), 
            player.getWidth(), 
            player.getHeight(), 
            Constants.DOOR_X,
            Constants.DOOR_Y,
            Constants.DOOR_WIDTH,
            Constants.DOOR_HEIGHT
        )) finish(true);
    }

    private void updatePlayer(double dt) {
        // 確認當下一針方向到底是往哪裡
        double direction = (keys.contains(KeyCode.RIGHT) ? 1 : 0) - (keys.contains(KeyCode.LEFT) ? 1 : 0);
        
        // 算出位移
        player.setVelocityX(direction * Constants.PLAYER_SPEED);

        if (direction != 0) player.setFacingRight(direction > 0);

        boolean jump = keys.contains(KeyCode.SPACE);

        // !jumpHeld: 上一幀沒有按空白鍵
        if (jump && !jumpHeld && player.isOnGround()) { 
            player.setVelocityY(-Constants.JUMP_SPEED); player.setOnGround(false); 
        }
        jumpHeld = jump;

        if (keys.contains(KeyCode.Z) && shootCooldown == 0) shoot();

        // 計算掉下來的距離
        player.setVelocityY(player.getVelocityY() + Constants.GRAVITY * dt);

        moveWithTiles(player, dt);

        if (player.getHp() <= 0) {
            player.setState(PlayerState.DEAD);
        } 
        else if (!player.isOnGround()) {
            player.setState(PlayerState.JUMPING);
        } 
        else if (direction == 0) {
            player.setState(PlayerState.IDLE);
        } 
        else {
            player.setState(PlayerState.MOVING);
        }
    }

    private void updateEnemies(double dt) {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.DEFEATED) continue;

            if (enemy.getState() == EnemyState.TRAPPED) {
                // 受困期間只更新倒數；到期後泡泡消失、敵人恢復巡邏
                enemy.getTrapTimer().update(dt);

                if (enemy.getTrapTimer().isFinished()) {
                    bubbles.stream().filter(b -> b.getTrappedEnemy() == enemy).forEach(Bubble::deactivate);
                    enemy.escape();
                }
                continue;
            }

            // MOVING 狀態的敵人會持續往前走，遇到地磚就反向
            enemy.setVelocityX(enemy.getDirection() * Constants.ENEMY_SPEED);
            enemy.setVelocityY(enemy.getVelocityY() + Constants.GRAVITY * dt);

            // 記錄移動前的 X 座標，若移動後仍然沒有改變，表示被地磚卡住了，需轉向
            double before = enemy.getX(); 
            moveWithTiles(enemy, dt);
            if (Math.abs(enemy.getX() - before) < .1) enemy.reverse();
        }
    }

    private void updateBubbles(double dt) {
        for (Bubble bubble : bubbles) {
            bubble.updateAge(dt);

            // step01. 先定義 bubble 中有無敵人下的座標更新方式
            if (bubble.hasTrappedEnemy()) {
                // 困敵泡泡向上飄，敵人座標跟隨泡泡中心附近。
                Enemy e = bubble.getTrappedEnemy();
                bubble.setY(bubble.getY() + bubble.getVelocityY() * dt);

                // 讓敵人在泡泡置中
                double centeredEnemyX =
                        bubble.getX() + (bubble.getWidth() - e.getWidth()) / 2;
                double centeredEnemyY =
                        bubble.getY() + (bubble.getHeight() - e.getHeight()) / 2;
                e.setX(centeredEnemyX);
                e.setY(centeredEnemyY);
            } else {
                // 普通泡泡水平前進，遇到第一名 MOVING 敵人便將其捕捉
                bubble.setX(bubble.getX() + bubble.getVelocityX() * dt);
                bubble.setY(bubble.getY() - Constants.BUBBLE_RISE_SPEED * dt);
                for (Enemy e : enemies) 
                    if (e.getState() == EnemyState.MOVING && OverlapChecker.overlaps(bubble, e)) {
                         e.trap(); 
                         bubble.trap(e); 
                         break; }
            }

            // step02. 決定泡泡是否需要消失
            if (bubble.getAge() > Constants.BUBBLE_LIFETIME
                    || bubble.getX() < -Constants.BUBBLE_HORIZONTAL_MARGIN
                    || bubble.getX() > Constants.WIDTH + Constants.BUBBLE_HORIZONTAL_MARGIN
                    || bubble.getY() < -Constants.BUBBLE_TOP_MARGIN) {
                if (bubble.hasTrappedEnemy() && bubble.getTrappedEnemy().getState() == EnemyState.TRAPPED)
                     bubble.getTrappedEnemy().escape();
                bubble.deactivate();
            }
        }
    }

   
    private void checkContacts() {
         // 玩家碰到活動敵人會受傷並被反方向彈開
        for (Enemy enemy : enemies) {
            if (enemy.getState() == EnemyState.MOVING && OverlapChecker.overlaps(player, enemy)) {
                player.damage();

                // step01. 玩家向上彈
                player.setVelocityY(-Constants.PLAYER_DAMAGE_BOUNCE_SPEED);

                // step02. 決定往哪邊彈開
                double knockbackDirection = player.getX() < enemy.getX() ? -1 : 1;

                // step03. 計算彈開後的 X 座標，避免玩家被彈到畫面外
                double knockbackX = player.getX()
                        + knockbackDirection
                        * Constants.PLAYER_HIT_KNOCKBACK_DISTANCE;
                double minX = Constants.PLAYER_MIN_X_AFTER_HIT;
                double maxX = Constants.WIDTH
                        - player.getWidth()
                        - Constants.PLAYER_MIN_X_AFTER_HIT;
                player.setX(Math.max(minX, Math.min(maxX, knockbackX)));
            }
        }

        // 玩家碰到有敵人的泡泡，就同時消滅敵人並讓泡泡破掉
        for (Bubble bubble : bubbles) {
            if (bubble.isActive() && bubble.hasTrappedEnemy() && OverlapChecker.overlaps(player, bubble)) {
                bubble.getTrappedEnemy().defeat();
                bubble.deactivate();
            }
        }
    }

    private void shoot() {
        double bubbleX = player.isFacingRight()
                ? player.getRight()
                : player.getX() - Constants.BUBBLE_SIZE;

        double bubbleY = player.getY()
          + (player.getHeight() - Constants.BUBBLE_SIZE) / 2;

        double velocityX = player.isFacingRight() ? Constants.BUBBLE_SPEED : -Constants.BUBBLE_SPEED;
                
        bubbles.add(new Bubble(
                bubbleX,
                bubbleY,
                velocityX
        ));

        shootCooldown = Constants.SHOOT_COOLDOWN_SECONDS;
    }


    /* moveWithTiles() 是整個遊戲的物理碰撞核心
        object: 代表要移動的東西
        負責：
            根據速度更新位置
            檢查是否撞到磚塊
            修正位置、避免穿牆
            重設速度
            判斷玩家是否站在地面
            掉出地圖處理
    */ 
    private void moveWithTiles(GameObject object, double dt) {
        // step01. 更新 X 座標
        object.setX(object.getX() + object.getVelocityX() * dt);
        
        // step02. 檢查 x 座標是否撞到實心地磚，如果撞到，修正 x 座標並把速度歸零 (避免下一幀繼續往牆裡穿)
        for (Tile tile : tiles) {
            if (tile.isSolid() && OverlapChecker.overlaps(object, tile)) {
                if (object.getVelocityX() > 0) {
                    object.setX(tile.getX() - object.getWidth());
                } else if (object.getVelocityX() < 0) {
                    object.setX(tile.getRight());
                }
                object.setVelocityX(0);
            }
        }

        // step03. 算出 object 可容許的 x 區間，計算出修正後的 x 座標
        double minX = 0;
        double maxX = Constants.WIDTH - object.getWidth();
        double limitedX = Math.max(
                minX,
                Math.min(maxX, object.getX())
        );
        object.setX(limitedX);

        // step04. 更新 Y 座標
        object.setY(object.getY() + object.getVelocityY() * dt);

        // step05. 預設沒有跳躍
        boolean grounded = false;

        // step06. 檢查 y 座標是否撞到實心地磚，如果撞到，修正 y 座標並把速度歸零 (避免下一幀繼續往牆裡穿)
        for (Tile tile : tiles) {
            if (tile.isSolid() && OverlapChecker.overlaps(object, tile)) {
                // > 0 表示物件正在向下掉，將其放在磚塊上
                if (object.getVelocityY() > 0) {
                    object.setY(tile.getY() - object.getHeight());
                    grounded = true;
                } else if (object.getVelocityY() < 0) {
                    object.setY(tile.getBottom());
                }
                object.setVelocityY(0);
            }
        }
        
        // step07. 如果 object 是玩家，更新玩家的 onGround 狀態
        if (object instanceof Player p) {
            p.setOnGround(grounded);
        }

        // step08. 如果物件穿過天花板，重置 y 座標並把速度歸零
        if (object.getY() > Constants.HEIGHT) {
            object.setY(Constants.RESPAWN_Y_POSITION);
            object.setVelocityY(0);

            if (object instanceof Player p) {
                p.damage();
            }
        }
    }


    private void finish(boolean victory) { if (!finished) { finished = true; resultHandler.accept(victory); } }
    public Player getPlayer() { return player; }
    public List<Tile> getTiles() { return tiles; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bubble> getBubbles() { return bubbles; }
    public int getActiveEnemyCount() { return (int) enemies.stream().filter(e -> e.getState() != EnemyState.DEFEATED).count(); }
    public boolean isDoorVisible() { return doorVisible; }
    public double getDoorX() { return Constants.DOOR_X; }
    public double getDoorY() { return Constants.DOOR_Y; }
}
