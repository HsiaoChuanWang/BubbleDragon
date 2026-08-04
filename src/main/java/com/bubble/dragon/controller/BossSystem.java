package com.bubble.dragon.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bubble.dragon.entity.boss.Boss;
import com.bubble.dragon.entity.boss.BossBubble;
import com.bubble.dragon.entity.boss.BossState;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.map.Tile;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

import javafx.scene.media.AudioClip;

/**
 * 管理第二關 Boss 的完整生命週期
 * 
 * 狀態流程：
 * INACTIVE → SCREAM ↔ FOLD_WINGS（重複登場循環）→ PATROL
 * → DIVING_ATTACK → PATROL，最後受到第 5 次有效攻擊後進入 DEFEATED
 * 
 */
public final class BossSystem {
    private final Boss boss = new Boss();
    private final List<BossBubble> attackBubbles = new ArrayList<>();
    private final List<Tile> tiles;
    private final CollisionSystem collisions;
    private final BubbleSystem playerBubbles;
    private final Random random = new Random();

    // 登場音效只需立即播放，使用輕量的 AudioClip
    private final AudioClip screamSound;

    // 紀錄在 SCREAM 或 FOLD_WINGS 的狀態下已經過的秒數
    // 滿指定秒數後，換動作並再次歸零
    private double phaseElapsed;

    // 距離下一波四角泡泡攻擊要等待的秒數
    private double shotCooldown;

    // 已完成幾輪「SCREAM + FOLD_WINGS」登場循環
    // 達到設定輪數後進入巡邏
    private int completedIntroCycles;

    // Boss 中彈後原地透明停留的剩餘秒數
    // 倒數至 0 才移動到另一座平台
    private double relocationDelay;

    // Boss 本輪巡邏已經過的秒數
    // 達到 10 秒時觸發俯衝，其他狀態不會累加
    private double patrolElapsed;

    // 俯衝共有三條路徑(上到下，左上到右下，右上到右下)
    // 以下欄位記錄目前路徑的終點
    private int divePhase; // 現在是第幾段俯衝
    private double diveTargetX; // 目前這一段俯衝要抵達的終點 X 座標
    private double diveTargetY; // 目前這一段俯衝要抵達的終點 Y 座標

    public BossSystem(List<Tile> tiles, CollisionSystem collisions, BubbleSystem playerBubbles) {
        this.tiles = tiles;
        this.collisions = collisions;
        this.playerBubbles = playerBubbles;
        screamSound = loadScreamSound();
    }

    public void activate() {
        if (boss.isActive())
            return;

        // boss 初登場
        boss.setX((Constants.WINDOW_WIDTH - boss.getWidth()) / 2);
        boss.setY((Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT - boss.getHeight()) / 2);
        boss.setVelocityX(0);
        boss.setVelocityY(0);
        completedIntroCycles = 0;
        beginScream();
    }

    public void update(double dt, Player player) {
        if (!boss.isActive())
            return;

        boss.updateInvulnerability(dt);
        checkPlayerBubbleHits();
        if (boss.isDefeated()) {
            attackBubbles.clear();
            return;
        }

        // 中彈後先停在原地呈現透明效果，再瞬移
        if (relocationDelay > 0) {
            relocationDelay = Math.max(0, relocationDelay - dt);
            boss.setVelocityX(0);
            boss.setVelocityY(0);

            if (relocationDelay == 0)
                moveToRandomPlatform(true);
            updateAttackBubbles(dt, player);
            return;
        }

        // 每個狀態只更新自己的行為，避免巡邏碰撞與無視地形的俯衝互相干擾。
        switch (boss.getState()) {
            case SCREAM -> updateScream(dt);
            case FOLD_WINGS -> updateFoldWings(dt);
            case PATROL -> updatePatrol(dt, player);
            case DIVING_ATTACK -> updateDivingAttack(dt);
            default -> {
            }
        }

        updateAttackBubbles(dt, player);
        checkBossContact(player);
    }

    private void updateScream(double dt) {
        phaseElapsed += dt;
        shotCooldown -= dt;

        while (shotCooldown <= 0 && phaseElapsed < Constants.BOSS_SCREAM_SECONDS) {
            fireCornerBurst();
            shotCooldown += Constants.BOSS_BUBBLE_INTERVAL_SECONDS;
        }

        if (phaseElapsed >= Constants.BOSS_SCREAM_SECONDS)
            beginFoldWings();
    }

    private void beginScream() {
        boss.setState(BossState.SCREAM);
        phaseElapsed = 0;
        shotCooldown = Constants.BOSS_BUBBLE_INTERVAL_SECONDS;
        if (screamSound != null)
            screamSound.play();

        // 進入階段時立即先射一波，之後才依 cooldown 連射
        fireCornerBurst();
    }

    private void beginFoldWings() {
        boss.setState(BossState.FOLD_WINGS);
        phaseElapsed = 0;
    }

    private void updateFoldWings(double dt) {
        phaseElapsed += dt;
        if (phaseElapsed < Constants.BOSS_FOLD_WINGS_SECONDS)
            return;

        // 一次 SCREAM 加一次 FOLD_WINGS 才算完成一個登場循環
        completedIntroCycles++;

        if (completedIntroCycles < Constants.BOSS_INTRO_CYCLES)
            beginScream();
        else
            beginPatrol();
    }

    private void fireCornerBurst() {
        // bubbleX/Y 是泡泡左上角，放置泡泡
        // originX/Y 是 Boss 的正中心點，計算射擊方向
        double bubbleX = boss.getX() + (boss.getWidth() - Constants.BUBBLE_SIZE) / 2;
        double bubbleY = boss.getY() + (boss.getHeight() - Constants.BUBBLE_SIZE) / 2;
        double originX = boss.getX() + boss.getWidth() / 2;
        double originY = boss.getY() + boss.getHeight() / 2;

        // Canvas 不包含 HUD，因此下方目標必須扣除 HUD 高度
        double bottom = Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT;

        addBubbleToward(bubbleX, bubbleY, originX, originY, 0, 0);
        addBubbleToward(bubbleX, bubbleY, originX, originY, Constants.WINDOW_WIDTH, 0);
        addBubbleToward(bubbleX, bubbleY, originX, originY, 0, bottom);
        addBubbleToward(bubbleX, bubbleY, originX, originY, Constants.WINDOW_WIDTH, bottom);
    }

    // 建立一顆朝指定目標點飛行的 Boss 泡泡
    private void addBubbleToward(
            double bubbleX,
            double bubbleY,
            double originX,
            double originY,
            double targetX,
            double targetY) {

        // 和目標地點相差多少距離
        double deltaX = targetX - originX;
        double deltaY = targetY - originY;

        // Math.hypot(deltaX, deltaY) 使用畢氏定理
        // 計算直角三角形的長邊，讓每個泡泡移動的速度都相同
        double distance = Math.hypot(deltaX, deltaY);
        addBubble(
                bubbleX,
                bubbleY,
                deltaX / distance * Constants.BOSS_BUBBLE_SPEED,
                deltaY / distance * Constants.BOSS_BUBBLE_SPEED);
    }

    private void addBubble(double x, double y, double velocityX, double velocityY) {
        attackBubbles.add(new BossBubble(x, y, velocityX, velocityY));
    }

    // 登場結束或俯衝結束時都重新選平台，並重新計算下一次 10 秒絕招倒數
    private void beginPatrol() {
        moveToRandomPlatform(false);
        boss.setState(BossState.PATROL);
        patrolElapsed = 0;
    }

    private void moveToRandomPlatform(boolean excludeCurrentPlatform) {
        // 因為是隨機選平台，所以要先排除過窄無法容納完整碰撞箱的平台
        List<Tile> platforms = tiles.stream()
                .filter(Tile::isSolid)
                .filter(tile -> tile.getWidth() >= boss.getWidth())
                .toList();

        if (platforms.isEmpty())
            return;

        // 受傷換位時盡量排除目前平台，若地圖只有一座平台則允許留在原處
        Tile currentPlatform = findCurrentPlatform();

        List<Tile> destinations = excludeCurrentPlatform && platforms.size() > 1
                ? platforms.stream().filter(tile -> tile != currentPlatform).toList()
                : platforms;
        Tile platform = destinations.get(random.nextInt(destinations.size()));

        // Boss 左上角能移動的最大範圍是「平台寬度 - Boss 寬度」
        double availableWidth = platform.getWidth() - boss.getWidth();

        // 隨機找一個安全點站
        boss.setX(platform.getX() + random.nextDouble() * availableWidth);
        boss.setY(platform.getY() - boss.getHeight());
        boss.setVelocityX(0);
        boss.setVelocityY(0);

        // 隨機面向一邊
        boss.setDirection(random.nextBoolean() ? 1 : -1);
    }

    private Tile findCurrentPlatform() {
        return tiles.stream()
                .filter(Tile::isSolid)
                .filter(tile -> Math.abs(boss.getBottom() - tile.getY()) <= Constants.PLATFORM_SUPPORT_TOLERANCE)
                .filter(tile -> boss.getRight() > tile.getX() && boss.getX() < tile.getRight())
                .findFirst()
                .orElse(null);
    }

    private void updatePatrol(double dt, Player player) {
        patrolElapsed += dt;
        if (patrolElapsed >= Constants.BOSS_DIVE_INTERVAL_SECONDS) {
            beginDivingAttack();
            return;
        }

        // 察覺玩家後追逐
        updateAwareness(player);
        double speed = boss.isChasing()
                ? Constants.ENEMY_CHASE_SPEED
                : Constants.ENEMY_PATROL_SPEED;
        double nextX = boss.getX() + boss.getDirection() * speed * dt;

        // 非追逐狀態會在平台邊緣轉身；追逐時則和一般敵人一樣允許離開平台
        boolean currentlySupported = collisions.hasSupportAt(boss, boss.getX());
        if (!boss.isChasing() && currentlySupported && !collisions.hasSupportAt(boss, nextX))
            boss.reverse();

        boss.setVelocityX(boss.getDirection() * speed);
        boss.setVelocityY(boss.getVelocityY() + Constants.GRAVITY * dt);

        // CollisionSystem 撞牆時會把 velocityX 歸零，因此保留原速度判斷是否撞牆
        double intendedVelocityX = boss.getVelocityX();
        collisions.moveWithTiles(boss, dt);

        boolean hitWall = intendedVelocityX != 0 && boss.getVelocityX() == 0;

        // Boss 左側已碰到視窗左邊界，而且原本仍準備向左移動
        boolean hitLeftEdge = boss.getX() <= 0 && intendedVelocityX < 0;

        // Boss 右側已碰到視窗右邊界，而且原本仍準備向右移動
        boolean hitRightEdge = boss.getRight() >= Constants.WINDOW_WIDTH
                && intendedVelocityX > 0;

        // 只要碰到左邊界或右邊界，就視為碰到視窗邊緣
        boolean hitScreenEdge = hitLeftEdge || hitRightEdge;
        if (!boss.isChasing() && (hitWall || hitScreenEdge))
            boss.reverse();
    }

    private void beginDivingAttack() {
        boss.setState(BossState.DIVING_ATTACK);
        boss.setChasing(false);
        boss.setVelocityX(0);
        boss.setVelocityY(0);

        // 第 0 段：視窗上方中央直衝下方中央
        divePhase = 0;
        startDivePath(
                (Constants.WINDOW_WIDTH - boss.getWidth()) / 2,
                0,
                (Constants.WINDOW_WIDTH - boss.getWidth()) / 2,
                Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT - boss.getHeight());
    }

    // 讓 Boss 以固定速度朝目前俯衝終點移動
    private void updateDivingAttack(double dt) {
        double deltaX = diveTargetX - boss.getX();
        double deltaY = diveTargetY - boss.getY();
        double distance = Math.hypot(deltaX, deltaY);
        double movement = Constants.BOSS_DIVE_SPEED * dt;

        if (distance <= movement) {
            boss.setX(diveTargetX);
            boss.setY(diveTargetY);
            startNextDivePath();
            return;
        }

        boss.setX(boss.getX() + deltaX / distance * movement);
        boss.setY(boss.getY() + deltaY / distance * movement);
    }

    // 結束目前俯衝後，決定並啟動下一條俯衝路徑；三段都完成後回到巡邏
    private void startNextDivePath() {
        divePhase++;
        double maxX = Constants.WINDOW_WIDTH - boss.getWidth();
        double maxY = Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT - boss.getHeight();

        // 第 1 段左上到右下，第 2 段右上到左下，3 段完成後回平台
        if (divePhase == 1) {
            startDivePath(0, 0, maxX, maxY);
        } else if (divePhase == 2) {
            startDivePath(maxX, 0, 0, maxY);
        } else {
            beginPatrol();
        }
    }

    // 設定一段俯衝的起點與終點，並在該段開始時播放一次音效
    private void startDivePath(double startX, double startY, double targetX, double targetY) {
        boss.setX(startX);
        boss.setY(startY);
        diveTargetX = targetX;
        diveTargetY = targetY;
        playDiveSound();
    }

    // 每一段俯衝開始時播放一次 Boss 音效
    private void playDiveSound() {
        if (screamSound != null)
            screamSound.play();
    }

    // 依 Player 與 Boss 的水平、垂直距離，切換 Boss 的巡邏或追逐狀態
    private void updateAwareness(Player player) {
        // 使用物件中心而非左上角計算距離，避免不同碰撞箱尺寸造成察覺偏差
        double horizontalDistance = Math.abs(centerX(player) - centerX(boss));
        double verticalDistance = Math.abs(centerY(player) - centerY(boss));

        // notice 範圍較小、forget 範圍較大，形成遲滯區避免邊界附近反覆切換狀態
        if (!boss.isChasing()
                && horizontalDistance <= Constants.ENEMY_NOTICE_DISTANCE
                && verticalDistance <= Constants.ENEMY_NOTICE_HEIGHT) {
            boss.setChasing(true);
            boss.face(centerX(player));
        } else if (boss.isChasing()
                && (horizontalDistance > Constants.ENEMY_FORGET_DISTANCE
                        || verticalDistance > Constants.ENEMY_FORGET_HEIGHT)) {
            boss.setChasing(false);
        }
    }

    // 檢查玩家的自由泡泡是否命中巡邏中的 Boss，並處理扣血、泡泡失效及延遲換位
    private void checkPlayerBubbleHits() {
        // 登場與俯衝期間不可受傷，只有正式巡邏戰鬥階段接受命中
        if (boss.getState() != BossState.PATROL)
            return;

        for (Bubble bubble : playerBubbles.getBubbles()) {
            if (!bubble.isActive()
                    || bubble.hasTrappedEnemy()
                    || !OverlapChecker.overlaps(bubble, boss))
                continue;

            // 命中的泡泡立刻失效，避免下一幀再次扣除 Boss HP
            playerBubbles.deactivate(bubble);
            if (boss.damage() && !boss.isDefeated())
                relocationDelay = Constants.BOSS_RELOCATE_DELAY_SECONDS;
            break;
        }
    }

    // 取得遊戲物件碰撞箱中心點的 X 座標，用於距離與方向計算
    private double centerX(com.bubble.dragon.entity.GameObject object) {
        return object.getX() + object.getWidth() / 2;
    }

    // 取得遊戲物件碰撞箱中心點的 Y 座標，用於距離與方向計算
    private double centerY(com.bubble.dragon.entity.GameObject object) {
        return object.getY() + object.getHeight() / 2;
    }

    // 更新所有 Boss 攻擊泡泡的位置，並移除打中 Player 或碰到視窗邊界的泡泡
    private void updateAttackBubbles(double dt, Player player) {
        for (BossBubble bubble : attackBubbles)
            bubble.update(dt);

        // Boss 泡泡可穿過磚塊，只在打中玩家或碰到視窗邊界時移除
        attackBubbles.removeIf(bubble -> {
            if (OverlapChecker.overlaps(player, bubble)) {
                player.damage();
                return true;
            }
            return touchesWindowEdge(bubble);
        });
    }

    // 判斷 Boss 泡泡是否已碰到可遊玩視窗的上、下、左、右任一邊界
    private boolean touchesWindowEdge(BossBubble bubble) {
        return bubble.getX() <= 0
                || bubble.getRight() >= Constants.WINDOW_WIDTH
                || bubble.getY() <= 0
                || bubble.getBottom() >= Constants.WINDOW_HEIGHT - Constants.HUD_HEIGHT;
    }

    // 在巡邏或俯衝狀態下檢查 Boss 是否碰到 Player，碰到時讓 Player 受傷
    private void checkBossContact(Player player) {
        // 登場動畫不造成接觸傷害；巡邏與俯衝才是可傷害玩家的戰鬥狀態
        if ((boss.getState() == BossState.PATROL
                || boss.getState() == BossState.DIVING_ATTACK)
                && OverlapChecker.overlaps(player, boss))
            player.damage();
    }

    // 載入登場 SCREAM 使用的輕量音效；載入失敗時回傳 null，讓遊戲靜音繼續
    private AudioClip loadScreamSound() {
        try {
            return new AudioClip(BossSystem.class.getResource("/sounds/boss-scream.mp3").toExternalForm());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public Boss getBoss() {
        return boss;
    }

    public List<BossBubble> getAttackBubbles() {
        return attackBubbles;
    }
}
