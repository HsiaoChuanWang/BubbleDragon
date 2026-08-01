package com.bubble.dragon.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.bubble.dragon.entity.boss.Boss;
import com.bubble.dragon.entity.boss.BossBubble;
import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.enemy.EnemyState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.player.PlayerState;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

import javafx.scene.input.KeyCode;

// 統籌鍵盤輸入、各遊戲系統的更新順序，以及整體勝敗流程
// 管理遊戲規則與每一幀「要做什麼」
// 確認第一關是否敵人全滅
public final class GameController {
    // 使用 Set 保存「目前仍按住」的鍵，偵測即時遊戲的連續輸入
    private final Set<KeyCode> keys = new HashSet<>();

    // 勝敗發生時由 BubbleDragonApp 傳入的 callback 負責切換 Scene
    // Consumer 是 Java 內建的「接收一個值、不回傳值」函式介面，這裡用來傳遞勝敗結果
    private final Consumer<Boolean> resultHandler;

    // 各項遊戲職責拆分給不同系統，GameController 負責協調系統並處理玩家與勝敗流程
    private final LevelManager levels;
    private final Player player;
    private final CollisionSystem collisions;
    private final BubbleSystem bubbles;
    private final EnemySystem enemies;
    private final BossSystem boss;
    private final LevelTransition transition;

    // Player 轉場後，要等幾秒開始 boss 的觸發
    private double bossAppearanceDelay;

    private boolean jumpHeld; // 記錄上一幀是否按住跳躍，避免按住空白鍵時連續起跳
    private boolean finished; // 遊戲結束後不再更新，避免重複呼叫 resultHandler
    private boolean doorVisible; // 所有敵人消滅且關卡轉場完成後才顯示出口
    private double doorX;
    private double doorY;

    // 建立控制器時只執行一次：載入關卡、建立玩家，並初始化各遊戲系統
    public GameController(Consumer<Boolean> resultHandler) {
        this.resultHandler = resultHandler;
        levels = new LevelManager();
        player = new Player(levels.getInitialPlayerX(), levels.getInitialPlayerY());
        collisions = new CollisionSystem(levels.getActiveTiles());
        bubbles = new BubbleSystem(levels.getEnemies());
        enemies = new EnemySystem(levels.getEnemies(), collisions, bubbles);
        boss = new BossSystem(levels.getActiveTiles(), collisions, bubbles);
        transition = new LevelTransition(player, levels);
    }

    public void press(KeyCode key) {
        keys.add(key);
    }

    public void release(KeyCode key) {
        keys.remove(key);
    }

    // delta time（時間差）代表「上一幀到這一幀經過了多少秒」
    // update(dt) 統一依序處理：
    // ├─ 關卡轉場
    // ├─ 泡泡冷卻與玩家無敵時間
    // ├─ 玩家、敵人及泡泡更新
    // ├─ 玩家與敵人／泡泡的接觸判定
    // └─ 關卡完成與勝敗判定
    public void update(double dt) {
        if (finished)
            return;

        // 轉場期間暫停一般遊戲邏輯，只更新場景切換動畫
        if (transition.isActive()) {
            transition.update(dt);
            return;
        }

        // transition.complete() 會先把 Player 放到第二關右下角；完成後才開始 Boss 延遲。
        if (transition.isComplete() && !boss.getBoss().isActive()) {
            bossAppearanceDelay += dt;
            if (bossAppearanceDelay >= Constants.BOSS_APPEAR_DELAY_SECONDS)
                boss.activate();
        }

        bubbles.updateCooldown(dt);
        player.updateInvulnerability(dt);
        updatePlayer(dt);
        enemies.update(dt, player);
        bubbles.update(dt);
        boss.update(dt, player);
        enemies.checkPlayerContact(player);
        bubbles.checkPlayerContact(player);

        // stream 將敵人集合轉成資料流；allMatch 用來確認是否所有敵人都已被消滅
        boolean allEnemiesDefeated = levels.getEnemies().stream()
                .allMatch(enemy -> enemy.getState() == EnemyState.DEFEATED);

        // 第一關只檢查普通敵人；第二關還必須擊敗 Boss，否則出口不會出現。
        boolean levelCleared = allEnemiesDefeated
                && (!transition.isComplete() || boss.getBoss().isDefeated());

        // 敵人全滅後開始關卡轉場；轉場開始時清除泡泡與目前保存的按鍵狀態
        if (levelCleared && !transition.isComplete()) {
            transition.start(() -> {
                bubbles.clear();
                keys.clear();
                jumpHeld = false;
            });
        }

        // 最終關卡轉場完成後顯示出口；玩家死亡為失敗，碰到出口則勝利
        boolean shouldShowDoor = levelCleared && transition.isComplete();
        if (shouldShowDoor && !doorVisible)
            placeDoorOnBottomLevelTwoTile();
        doorVisible = shouldShowDoor;
        if (player.getHp() <= 0)
            finish(false);
        if (doorVisible && playerOverlapsDoor())
            finish(true);
    }

    private void updatePlayer(double dt) {
        // 右鍵為 1、左鍵為 -1；同時按下或都沒按時方向為 0
        double direction = (keys.contains(KeyCode.RIGHT) ? 1 : 0)
                - (keys.contains(KeyCode.LEFT) ? 1 : 0);

        // 根據方向計算水平速度，並更新角色面向
        player.setVelocityX(direction * Constants.PLAYER_SPEED);
        if (direction != 0)
            player.setFacingRight(direction > 0);

        boolean jump = keys.contains(KeyCode.SPACE);

        // !jumpHeld 表示上一幀沒有按空白鍵，確保每次按鍵只觸發一次跳躍
        if (jump && !jumpHeld && player.isOnGround()) {
            player.setVelocityY(-Constants.JUMP_SPEED);
            player.setOnGround(false);
        }
        jumpHeld = jump;

        if (keys.contains(KeyCode.Z))
            bubbles.shoot(player);

        // 套用重力後，交由 CollisionSystem 移動玩家並處理地磚碰撞
        player.setVelocityY(player.getVelocityY() + Constants.GRAVITY * dt);
        collisions.moveWithTiles(player, dt);
        updatePlayerState(direction);
    }

    // 依照生命值、是否在空中及移動方向，決定玩家目前的動畫狀態
    private void updatePlayerState(double direction) {
        if (player.getHp() <= 0)
            player.setState(PlayerState.DEAD);
        else if (!player.isOnGround())
            player.setState(PlayerState.JUMPING);
        else if (direction == 0)
            player.setState(PlayerState.IDLE);
        else
            player.setState(PlayerState.MOVING);
    }

    // 檢查玩家的矩形範圍是否與出口重疊
    private boolean playerOverlapsDoor() {
        return OverlapChecker.overlaps(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight(),
                doorX,
                doorY,
                Constants.DOOR_WIDTH,
                Constants.DOOR_HEIGHT);
    }

    // 將門置中放在第二關最下方的實心地磚頂面
    private void placeDoorOnBottomLevelTwoTile() {
        Tile tile = levels.getLevelTwoTiles().stream()
                .filter(Tile::isSolid)
                .filter(candidate -> candidate.getWidth() >= Constants.DOOR_WIDTH)
                .max((first, second) -> Double.compare(first.getY(), second.getY()))
                .orElse(null);
        if (tile == null)
            return;

        doorX = tile.getX() + (tile.getWidth() - Constants.DOOR_WIDTH) / 2;
        doorY = tile.getY() - Constants.DOOR_HEIGHT;
    }

    // 保證勝敗結果只回報一次，避免畫面被重複切換
    private void finish(boolean victory) {
        if (!finished) {
            finished = true;
            resultHandler.accept(victory);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public List<Tile> getTiles() {
        return levels.getActiveTiles();
    }

    public List<Tile> getLevelOneTiles() {
        return levels.getLevelOneTiles();
    }

    public List<Tile> getLevelTwoTiles() {
        return levels.getLevelTwoTiles();
    }

    public List<Enemy> getEnemies() {
        return levels.getEnemies();
    }

    public List<Bubble> getBubbles() {
        return bubbles.getBubbles();
    }

    public Boss getBoss() {
        return boss.getBoss();
    }

    public List<BossBubble> getBossBubbles() {
        return boss.getAttackBubbles();
    }

    public int getActiveEnemyCount() {
        int regularEnemies = (int) levels.getEnemies().stream()
                .filter(enemy -> enemy.getState() != EnemyState.DEFEATED)
                .count();

        int activeBoss = transition.isComplete() && !boss.getBoss().isDefeated() ? 1 : 0;
        return regularEnemies + activeBoss;
    }

    public boolean isDoorVisible() {
        return doorVisible;
    }

    public boolean isShooting() {
        return bubbles.isShooting();
    }

    public double getLevelTransitionProgress() {
        return transition.getProgress();
    }

    public double getDoorX() {
        return doorX;
    }

    public double getDoorY() {
        return doorY;
    }

}
