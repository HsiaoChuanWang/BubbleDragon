package com.bubble.dragon.controller;

import java.util.ArrayList;
import java.util.List;

import com.bubble.dragon.entity.enemy.Enemy;
import com.bubble.dragon.entity.player.Player;
import com.bubble.dragon.entity.weapon.Bubble;
import com.bubble.dragon.entity.weapon.BubbleMovementState;
import com.bubble.dragon.physics.OverlapChecker;
import com.bubble.dragon.util.Constants;

// 建立與更新泡泡，處理捕捉、漂浮、失效及玩家戳破困敵泡泡
public final class BubbleSystem {
    private final List<Bubble> bubbles = new ArrayList<>();
    private final List<Enemy> enemies;
    private final Runnable popSoundHandler;
    private double shootCooldown; // 玩家射擊泡泡的冷卻時間，若大於 0 則玩家無法射擊

    public BubbleSystem(List<Enemy> enemies) {
        this(enemies, () -> {});
    }

    public BubbleSystem(List<Enemy> enemies, Runnable popSoundHandler) {
        this.enemies = enemies;
        this.popSoundHandler = popSoundHandler;
    }

    public void updateCooldown(double dt) {
        shootCooldown = Math.max(0, shootCooldown - dt);
    }

    public boolean shoot(Player player) {
        if (shootCooldown > 0)
            return false;
        double x = player.isFacingRight()
                ? player.getRight()
                : player.getX() - Constants.BUBBLE_SIZE;
        double y = player.getTop() + Constants.BUBBLE_SIZE / 2; // 泡泡垂直置中，模擬從嘴巴射出高度
        double velocityX = player.isFacingRight() ? Constants.BUBBLE_SPEED : -Constants.BUBBLE_SPEED;
        bubbles.add(new Bubble(x, y, velocityX));
        shootCooldown = Constants.SHOOT_COOLDOWN_SECONDS;
        return true;
    }

    public void update(double dt) {
        for (Bubble bubble : bubbles) {
            bubble.updateAge(dt);
            if (bubble.hasTrappedEnemy())
                moveTrappedBubble(bubble, dt);
            else
                moveFreeBubble(bubble, dt);
            keepInsideScreen(bubble); // 若泡泡碰到螢幕邊界，反彈或停止上升
            trapEnemyWhileHorizontal(bubble); // 只有仍在水平飛行的泡泡可以捕捉敵人
            updateTrappedEnemyPosition(bubble); // 若泡泡困住敵人，更新敵人位置，使其保持在泡泡中央
            expireFreeBubble(bubble); // 普通泡泡超過壽命後失效；困敵泡泡改以敵人受困時間計算
        }
        bubbles.removeIf(bubble -> !bubble.isActive());
    }

    // 困敵泡泡與敵人一起上升，並保持敵人位置在泡泡中央
    private void moveTrappedBubble(Bubble bubble, double dt) {
        bubble.setY(bubble.getY() + bubble.getVelocityY() * dt);
        updateTrappedBubbleShake(bubble);
        centerEnemy(bubble, bubble.getTrappedEnemy());
    }

    // 敵人脫困前最後兩秒，讓泡泡以原位置為中心左右抖動 2px
    private void updateTrappedBubbleShake(Bubble bubble) {
        double remaining = bubble.getTrappedEnemy().getTrapTimer().getRemainingSeconds();
        if (remaining > Constants.TRAPPED_BUBBLE_SHAKE_SECONDS) {
            bubble.setShakeOffsetX(0);
            return;
        }

        double elapsed = Constants.TRAPPED_BUBBLE_SHAKE_SECONDS - remaining;
        double angle = elapsed * Constants.TRAPPED_BUBBLE_SHAKES_PER_SECOND * Math.PI * 2;
        bubble.setShakeOffsetX(Math.sin(angle) * Constants.TRAPPED_BUBBLE_SHAKE_DISTANCE);
    }

    // 普通泡泡先水平移動，超過水平移動時間後改為垂直上升
    private void moveFreeBubble(Bubble bubble, double dt) {
        if (bubble.getMovementState() == BubbleMovementState.HORIZONTAL
                && bubble.getAge() > Constants.BUBBLE_HORIZONTAL_TRAVEL_SECONDS)
            bubble.startRising();

        if (bubble.getMovementState() == BubbleMovementState.HORIZONTAL) {
            bubble.setX(bubble.getX() + bubble.getVelocityX() * dt);
        } else if (bubble.getMovementState() == BubbleMovementState.RISING) {
            bubble.setY(bubble.getY() + bubble.getVelocityY() * dt);
        }
    }

    // 泡泡開始上升或停在畫面頂端後，不再對敵人進行捕捉判定
    private void trapEnemyWhileHorizontal(Bubble bubble) {
        if (bubble.hasTrappedEnemy()
                || bubble.getMovementState() != BubbleMovementState.HORIZONTAL)
            return;

        for (Enemy enemy : enemies) {
            if (enemy.canBeTrapped() && OverlapChecker.overlaps(bubble, enemy)) {
                enemy.trap();
                bubble.trap(enemy);
                break;
            }
        }
    }

    private void keepInsideScreen(Bubble bubble) {
        double maxX = Constants.WINDOW_WIDTH - bubble.getWidth();
        if (bubble.getX() < 0) {
            bubble.setX(0);
            if (!bubble.hasTrappedEnemy())
                bubble.startRising();
        } else if (bubble.getX() > maxX) {
            bubble.setX(maxX);
            if (!bubble.hasTrappedEnemy())
                bubble.startRising();
        }

        if (bubble.getY() < 0) {
            bubble.setY(0);
            bubble.stopAtTop();
        }
    }

    // 困敵泡泡的敵人位置會隨著泡泡移動而更新，並保持在泡泡中央
    private void updateTrappedEnemyPosition(Bubble bubble) {
        if (bubble.hasTrappedEnemy())
            centerEnemy(bubble, bubble.getTrappedEnemy());
    }

    private void centerEnemy(Bubble bubble, Enemy enemy) {
        enemy.setX(bubble.getX() + (bubble.getWidth() - enemy.getWidth()) / 2);
        enemy.setY(bubble.getY() + (bubble.getHeight() - enemy.getHeight()) / 2);
    }

    private void expireFreeBubble(Bubble bubble) {
        if (!bubble.hasTrappedEnemy() && bubble.getAge() > Constants.BUBBLE_LIFETIME)
            deactivate(bubble);
    }

    // 玩家與困敵泡泡接觸後，敵人被擊敗，泡泡失效
    public void checkPlayerContact(Player player) {
        for (Bubble bubble : bubbles) {
            if (bubble.isActive()
                    && bubble.hasTrappedEnemy()
                    && OverlapChecker.overlaps(player, bubble)) {
                bubble.getTrappedEnemy().defeat();
                deactivate(bubble);
            }
        }
    }

    // 敵人脫困時，讓原本困住它的泡泡破掉
    public void deactivateBubblesContaining(Enemy enemy) {
        bubbles.stream()
                .filter(bubble -> bubble.getTrappedEnemy() == enemy)
                .forEach(this::deactivate);
    }

    public void deactivate(Bubble bubble) {
        if (!bubble.isActive())
            return;
        bubble.deactivate();
        popSoundHandler.run();
    }

    public void clear() {
        bubbles.clear();
    }

    public boolean isShooting() {
        return shootCooldown > 0;
    }

    public List<Bubble> getBubbles() {
        return bubbles;
    }
}
